/**
 * Cloudflare Worker - Reply Sense
 * Routes:
 *  GET  /        -> info + example body
 *  GET  /health  -> health check
 *  POST /reply   -> generate reply variants
 *
 * Requires: OPENAI_API_KEY (Worker secret)
 */

export default {
  async fetch(request, env, ctx) {
    try {
      const url = new URL(request.url);

      if (request.method === "GET" && url.pathname === "/") {
        return json(
          {
            ok: true,
            service: "reply-sense",
            routes: {
              'GET /': "this info",
              'GET /health': "health check",
              'POST /reply': "generate replies (send JSON body)"
            },
            exampleBody: {
              conversation: [
                { from: "them", text: "You as well!! It's too warm I hate it" }
              ],
              variants: 3,
              vibe: "auto",
              tone: "auto",
              writingStyle: "auto",
              textQuality: "auto",
              emojiLevel: "auto",
              spiceLevel: "auto",
              age: "auto",
              punctuationPreference: "auto"
            },
            notes: [
              "Use POST /reply with JSON body.",
              "Set fields to 'auto' to let the model infer from conversation.",
              "emojiLevel can be 0-3 or 'auto'.",
              "variants can be 1-5."
            ]
          },
          200
        );
      }

      if (request.method === "GET" && url.pathname === "/health") {
        const hasKey = !!env.OPENAI_API_KEY;
        return json(
          {
            ok: true,
            service: "reply-sense",
            hasOpenAIKey: hasKey,
            now: new Date().toISOString()
          },
          200
        );
      }

      if (request.method === "POST" && url.pathname === "/reply") {
        if (!env.OPENAI_API_KEY) {
          return json(
            { ok: false, error: "Missing OPENAI_API_KEY secret in Worker." },
            500
          );
        }

        const body = await safeJson(request);
        if (!body.ok) return json(body, 400);

        const payload = body.value;

        // ---- validate inputs ----
        const conversation = Array.isArray(payload.conversation) ? payload.conversation : null;
        if (!conversation || conversation.length === 0) {
          return json(
            { ok: false, error: "conversation must be a non-empty array." },
            400
          );
        }

        const variants = clampInt(payload.variants ?? 3, 1, 5);

        const vibe = normalizeAutoString(payload.vibe);
        const tone = normalizeAutoString(payload.tone);
        const writingStyle = normalizeAutoString(payload.writingStyle);
        const textQuality = normalizeAutoString(payload.textQuality);
        const spiceLevel = normalizeAutoString(payload.spiceLevel);
        const punctuationPreference = normalizeAutoString(payload.punctuationPreference);
        const age = normalizeAutoString(payload.age);

        const emojiLevel = normalizeEmojiLevel(payload.emojiLevel);

        // ---- Build prompt that enforces: "not only spice" + auto vibe ----
        // This is IMPORTANT: spice is just ONE dial. The core output is a natural reply that matches the vibe.
        const convoText = formatConversation(conversation);

        const system = [
          "You are ReplySense, an assistant that writes short message replies for texting/DMs.",
          "Primary goal: produce natural replies that match the conversation vibe, relationship context, and texting style.",
          "Spice is optional and subtle unless explicitly requested; do not over-focus on it.",
          "Never include explicit sexual content. Keep flirtiness PG-13 at most unless the user explicitly asks for more.",
          "If user style is casual, keep it casual. If formal, keep it cleaner.",
          "Output MUST be JSON only when asked; otherwise respond normally. (We will request JSON in the API.)"
        ].join("\n");

        // Auto inference block. If "auto", model decides based on convo.
        const controls = {
          vibe,
          tone,
          writingStyle,
          textQuality,
          emojiLevel,
          spiceLevel,
          age,
          punctuationPreference
        };

        const userPrompt = [
          "TASK:",
          `Given the conversation, generate ${variants} reply options the user can send back.`,
          "",
          "CONVERSATION:",
          convoText,
          "",
          "STYLE CONTROLS (some may be 'auto'):",
          JSON.stringify(controls, null, 2),
          "",
          "RULES:",
          "- Replies should be 1-2 sentences max unless the conversation clearly needs more.",
          "- Make replies feel human, not robotic, and not repetitive.",
          "- If controls are 'auto', infer the best fit from the conversation.",
          "- Respect punctuationPreference and textQuality (when not 'auto').",
          "- Do NOT mention these rules or the controls in the replies."
        ].join("\n");

        // ---- Responses API payload (CORRECT) ----
        const openaiPayload = {
          model: "gpt-4.1-mini",
          input: [
            {
              role: "system",
              content: [{ type: "input_text", text: system }]
            },
            {
              role: "user",
              content: [{ type: "input_text", text: userPrompt }]
            }
          ],
          response: {
            format: {
              type: "json_schema",
              json_schema: {
                name: "reply_sense_result",
                schema: {
                  type: "object",
                  additionalProperties: false,
                  properties: {
                    vibe: { type: "string" },
                    tone: { type: "string" },
                    writingStyle: { type: "string" },
                    textQuality: { type: "string" },
                    emojiLevel: { type: "integer" },
                    spiceLevel: { type: "integer" },
                    notes: { type: "string" },
                    replies: {
                      type: "array",
                      items: { type: "string" },
                      minItems: 1,
                      maxItems: 5
                    }
                  },
                  required: [
                    "vibe",
                    "tone",
                    "writingStyle",
                    "textQuality",
                    "emojiLevel",
                    "spiceLevel",
                    "notes",
                    "replies"
                  ]
                }
              }
            }
          }
        };

        const res = await fetch("https://api.openai.com/v1/responses", {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${env.OPENAI_API_KEY}`,
            "Content-Type": "application/json"
          },
          body: JSON.stringify(openaiPayload)
        });

        const text = await res.text();
        if (!res.ok) {
          return json(
            {
              ok: false,
              error: "OpenAI request failed.",
              status: res.status,
              details: safeTruncate(text, 2000)
            },
            500
          );
        }

        const parsed = safeParseJson(text);
        if (!parsed.ok) {
          return json(
            {
              ok: false,
              error: "OpenAI returned non-JSON response.",
              details: safeTruncate(text, 2000)
            },
            500
          );
        }

        // Responses API: content is typically in output[0].content[0].text OR output_text convenience.
        const model = parsed.value.model ?? "unknown";
        const resultObj = extractJsonResult(parsed.value);

        // Hard clamp any model-provided numeric controls to safe ranges
        const out = {
          ok: true,
          model,
          request: {
            variants,
            vibe,
            tone,
            writingStyle,
            textQuality,
            emojiLevel: emojiLevel === "auto" ? "auto" : emojiLevel,
            spiceLevel,
            age,
            punctuationPreference
          },
          result: resultObj
        };

        return json(out, 200);
      }

      return json({ ok: false, error: "Not found." }, 404);
    } catch (err) {
      return json(
        {
          ok: false,
          error: "Server error.",
          details: String(err?.message ?? err)
        },
        500
      );
    }
  }
};

/* ---------------- helpers ---------------- */

function json(obj, status = 200, headers = {}) {
  return new Response(JSON.stringify(obj, null, 2), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type,Authorization",
      ...headers
    }
  });
}

async function safeJson(request) {
  try {
    if (request.method === "OPTIONS") return { ok: true, value: {} };
    const ct = request.headers.get("content-type") || "";
    if (!ct.includes("application/json")) {
      // still try, but warn
    }
    const data = await request.json();
    return { ok: true, value: data };
  } catch (e) {
    return { ok: false, error: "Invalid JSON body." };
  }
}

function clampInt(v, min, max) {
  const n = Number(v);
  if (!Number.isFinite(n)) return min;
  const i = Math.trunc(n);
  return Math.max(min, Math.min(max, i));
}

function normalizeAutoString(v) {
  if (v === undefined || v === null) return "auto";
  if (typeof v !== "string") return "auto";
  const s = v.trim();
  if (!s) return "auto";
  return s.toLowerCase() === "auto" ? "auto" : s;
}

function normalizeEmojiLevel(v) {
  if (v === undefined || v === null) return "auto";
  if (typeof v === "string") {
    const s = v.trim().toLowerCase();
    if (s === "auto") return "auto";
    const n = Number(s);
    if (Number.isFinite(n)) return clampInt(n, 0, 3);
    return "auto";
  }
  if (typeof v === "number") return clampInt(v, 0, 3);
  return "auto";
}

function formatConversation(conversation) {
  return conversation
    .map((m, idx) => {
      const from = safeTruncate(String(m?.from ?? "unknown"), 24);
      const text = safeTruncate(String(m?.text ?? ""), 800);
      return `${idx + 1}) ${from}: ${text}`;
    })
    .join("\n");
}

function safeTruncate(s, max) {
  const str = String(s ?? "");
  return str.length > max ? str.slice(0, max) + "…" : str;
}

function safeParseJson(s) {
  try {
    return { ok: true, value: JSON.parse(s) };
  } catch {
    return { ok: false };
  }
}

/**
 * Extract the JSON schema response from the Responses API result.
 * We asked for json_schema, so the model should return valid JSON.
 */
function extractJsonResult(responsesApiObj) {
  // Newer API sometimes includes output_text (string). But we requested json_schema,
  // so we prefer parsing from output[0].content[0].text if needed.
  // Some implementations place parsed JSON in `output[0].content[0].json` or similar,
  // but safest: attempt to locate a JSON string and parse it.
  const direct = responsesApiObj?.output?.[0]?.content?.[0];

  // Common patterns:
  // - direct.text is a JSON string
  // - responsesApiObj.output_text is a JSON string
  const candidateStrings = [];

  if (typeof direct?.text === "string") candidateStrings.push(direct.text);
  if (typeof responsesApiObj?.output_text === "string") candidateStrings.push(responsesApiObj.output_text);

  for (const c of candidateStrings) {
    const p = safeParseJson(c);
    if (p.ok) return normalizeResultObject(p.value);
  }

  // If the API ever returns already-parsed JSON:
  if (direct && typeof direct === "object") {
    if (direct.json && typeof direct.json === "object") return normalizeResultObject(direct.json);
  }

  // Fallback: return something minimally useful
  return normalizeResultObject({
    vibe: "auto",
    tone: "auto",
    writingStyle: "auto",
    textQuality: "auto",
    emojiLevel: 0,
    spiceLevel: 0,
    notes: "Could not parse structured result; returning empty replies.",
    replies: []
  });
}

function normalizeResultObject(obj) {
  const vibe = typeof obj?.vibe === "string" ? obj.vibe : "auto";
  const tone = typeof obj?.tone === "string" ? obj.tone : "auto";
  const writingStyle = typeof obj?.writingStyle === "string" ? obj.writingStyle : "auto";
  const textQuality = typeof obj?.textQuality === "string" ? obj.textQuality : "auto";
  const emojiLevel = clampInt(obj?.emojiLevel ?? 0, 0, 3);
  const spiceLevel = clampInt(obj?.spiceLevel ?? 0, 0, 3);
  const notes = typeof obj?.notes === "string" ? obj.notes : "";
  const replies = Array.isArray(obj?.replies) ? obj.replies.map(String).slice(0, 5) : [];

  return { vibe, tone, writingStyle, textQuality, emojiLevel, spiceLevel, notes, replies };
}
