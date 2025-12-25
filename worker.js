/**
 * Cloudflare Worker: reply-sense
 * Routes:
 *  - GET  /        : info + example body
 *  - GET  /health  : health check
 *  - POST /reply   : generate reply variants
 *
 * Env:
 *  - OPENAI_API_KEY (secret)
 */

export default {
  async fetch(request, env) {
    try {
      const url = new URL(request.url);
      const { pathname } = url;

      // CORS preflight
      if (request.method === "OPTIONS") return corsResponse("", 204);

      if (request.method === "GET" && pathname === "/") {
        return corsJson(
          {
            ok: true,
            service: "reply-sense",
            routes: {
              "GET /": "this info",
              "GET /health": "health check",
              "POST /reply": "generate replies (send JSON body)",
            },
            exampleBody: exampleBody(),
          },
          200
        );
      }

      if (request.method === "GET" && pathname === "/health") {
        const hasKey = Boolean(env?.OPENAI_API_KEY && String(env.OPENAI_API_KEY).trim().length > 0);
        return corsJson(
          { ok: true, service: "reply-sense", hasOpenAIKey: hasKey, now: new Date().toISOString() },
          200
        );
      }

      if (request.method === "POST" && pathname === "/reply") {
        if (!env?.OPENAI_API_KEY) {
          return corsJson({ ok: false, error: "Missing OPENAI_API_KEY in Worker secrets." }, 500);
        }

        const body = await safeJson(request);
        if (!body) return corsJson({ ok: false, error: "Invalid JSON body." }, 400);

        const normalized = normalizeRequest(body);
        const system = buildSystemPrompt();
        const user = buildUserPrompt(normalized);

        // ✅ Correct OpenAI Responses payload (NO deprecated keys)
        const openaiPayload = {
          model: pickModel(body?.model),
          input: [
            { role: "system", content: [{ type: "input_text", text: system }] },
            { role: "user", content: [{ type: "input_text", text: user }] },
          ],
          temperature: 0.7,
          max_output_tokens: 700,
        };

        const resp = await fetch("https://api.openai.com/v1/responses", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${env.OPENAI_API_KEY}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(openaiPayload),
        });

        const rawText = await resp.text();
        if (!resp.ok) {
          return corsJson(
            {
              ok: false,
              error: "OpenAI request failed.",
              status: resp.status,
              details: safeTruncate(rawText, 7000),
            },
            500
          );
        }

        const data = safeParseJson(rawText) ?? { _raw: rawText };
        const assistantText = extractOutputText(data);

        // ✅ FIX: model sometimes emits multiple JSON objects; grab only the first complete one
        const firstJson = extractFirstJsonObject(assistantText);
        const parsed = firstJson ? safeParseJson(firstJson) : null;

        if (!parsed) {
          return corsJson(
            {
              ok: false,
              error: "Model output was not valid JSON.",
              status: 500,
              details: safeTruncate(assistantText || rawText, 7000),
            },
            500
          );
        }

        const replies = Array.isArray(parsed.replies) ? parsed.replies : [];
        const result = parsed.result && typeof parsed.result === "object" ? parsed.result : {};
        const notes = typeof parsed.notes === "string" ? parsed.notes : undefined;

        return corsJson(
          {
            ok: true,
            model: openaiPayload.model,
            request: {
              variants: normalized.variants,
              vibe: normalized.vibe,
              tone: normalized.tone,
              writingStyle: normalized.writingStyle,
              textQuality: normalized.textQuality,
              emojiLevel: normalized.emojiLevel,
              spiceLevel: normalized.spiceLevel,
              age: normalized.age,
              punctuationPreference: normalized.punctuationPreference,
            },
            result: { ...result, ...(notes ? { notes } : {}) },
            replies: replies.slice(0, normalized.variants),
          },
          200
        );
      }

      return corsJson({ ok: false, error: "Not found." }, 404);
    } catch (err) {
      return corsJson(
        {
          ok: false,
          error: "Server error.",
          details: safeTruncate(String(err?.stack || err?.message || err), 5000),
        },
        500
      );
    }
  },
};

/* ---------------------------- helpers ---------------------------- */

function corsResponse(body, status = 200, extraHeaders = {}) {
  return new Response(body, {
    status,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type,Authorization",
      "Access-Control-Max-Age": "86400",
      ...extraHeaders,
    },
  });
}

function corsJson(obj, status = 200) {
  return corsResponse(JSON.stringify(obj, null, 2), status, { "Content-Type": "application/json" });
}

async function safeJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

function safeParseJson(str) {
  try {
    return JSON.parse(str);
  } catch {
    return null;
  }
}

function clampInt(v, min, max) {
  const n = Number(v);
  if (!Number.isFinite(n)) return min;
  const i = Math.trunc(n);
  return Math.max(min, Math.min(max, i));
}

function safeTruncate(s, max) {
  const str = String(s ?? "");
  return str.length > max ? str.slice(0, max) + "…(truncated)" : str;
}

function pickModel(model) {
  const m = String(model || "").trim();
  return m || "gpt-4.1-mini";
}

function exampleBody() {
  return {
    conversation: [{ from: "them", text: "You as well!! It's too warm I hate it" }],
    variants: 3,
    vibe: "auto",
    tone: "auto",
    writingStyle: "auto",
    textQuality: "auto",
    emojiLevel: "auto",
    spiceLevel: "auto",
    age: "auto",
    punctuationPreference: "auto",
  };
}

function normalizeRequest(body) {
  const conversation = Array.isArray(body.conversation) ? body.conversation : [];
  const variants = clampInt(body.variants ?? 3, 1, 5);

  return {
    conversation: conversation
      .map((m) => ({
        from: String(m?.from ?? "").trim() || "them",
        text: String(m?.text ?? "").trim(),
      }))
      .filter((m) => m.text.length > 0)
      .slice(-30),
    variants,
    vibe: normAutoStr(body.vibe, "auto"),
    tone: normAutoStr(body.tone, "auto"),
    writingStyle: normAutoStr(body.writingStyle, "auto"),
    textQuality: normAutoStr(body.textQuality, "auto"),
    emojiLevel: normAutoStr(body.emojiLevel, "auto"),
    spiceLevel: normAutoStr(body.spiceLevel, "auto"),
    age: normAutoStr(body.age, "auto"),
    punctuationPreference: normAutoStr(body.punctuationPreference, "auto"),
  };
}

function normAutoStr(v, fallback = "auto") {
  if (v === undefined || v === null) return fallback;
  const s = String(v).trim();
  return s.length ? s : fallback;
}

function buildSystemPrompt() {
  return `
You are ReplySense: generate text-message reply options that match the conversation's vibe and the user's style.

OUTPUT RULES:
- Output MUST be ONE valid JSON object only. Do NOT output multiple JSON objects.
- No markdown, no backticks, no extra commentary.
- Schema:
  {
    "result": {
      "vibe": string,
      "tone": string,
      "writingStyle": string,
      "textQuality": string,
      "emojiLevel": string,
      "spiceLevel": number
    },
    "notes": string,
    "replies": string[]
  }

QUALITY RULES:
- Keep replies short like real texting.
- Avoid sounding like a bot.
- spiceLevel is flirtiness (PG-13 max). If normal convo, spiceLevel=0.
`.trim();
}

function buildUserPrompt(r) {
  const transcript = r.conversation.map((m) => `${m.from.toUpperCase()}: ${m.text}`).join("\n");
  return `
TRANSCRIPT:
${transcript}

PREFERENCES (use best judgment when "auto"):
variants=${r.variants}
vibe=${r.vibe}
tone=${r.tone}
writingStyle=${r.writingStyle}
textQuality=${r.textQuality}
emojiLevel=${r.emojiLevel}
spiceLevel=${r.spiceLevel}
age=${r.age}
punctuationPreference=${r.punctuationPreference}

Return ONE JSON object only.
`.trim();
}

/**
 * Extract text from OpenAI Responses API result.
 */
function extractOutputText(data) {
  if (!data || typeof data !== "object") return "";
  if (typeof data.output_text === "string") return data.output_text;

  const out = data.output;
  if (Array.isArray(out)) {
    let buf = "";
    for (const item of out) {
      const content = item?.content;
      if (!Array.isArray(content)) continue;
      for (const c of content) {
        if (c?.type === "output_text" && typeof c?.text === "string") buf += c.text;
        else if (typeof c?.text === "string") buf += c.text;
      }
    }
    return buf.trim();
  }

  return "";
}

/**
 * ✅ Extract the FIRST complete JSON object from a string.
 * Works even if multiple JSON objects are concatenated.
 * Handles braces inside string literals properly.
 */
function extractFirstJsonObject(s) {
  const str = String(s || "");
  const start = str.indexOf("{");
  if (start === -1) return "";

  let depth = 0;
  let inString = false;
  let escape = false;

  for (let i = start; i < str.length; i++) {
    const ch = str[i];

    if (inString) {
      if (escape) {
        escape = false;
      } else if (ch === "\\") {
        escape = true;
      } else if (ch === '"') {
        inString = false;
      }
      continue;
    } else {
      if (ch === '"') {
        inString = true;
        continue;
      }
      if (ch === "{") depth++;
      if (ch === "}") depth--;

      if (depth === 0) {
        return str.slice(start, i + 1).trim();
      }
    }
  }

  return "";
       }
