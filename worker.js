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
  async fetch(request, env, ctx) {
    try {
      const url = new URL(request.url);
      const { pathname } = url;

      // CORS preflight
      if (request.method === "OPTIONS") {
        return corsResponse("", 204);
      }

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
          {
            ok: true,
            service: "reply-sense",
            hasOpenAIKey: hasKey,
            now: new Date().toISOString(),
          },
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

        // ✅ CORRECT OpenAI Responses API payload (NO `response`, NO `response_format`)
        const openaiPayload = {
          model: pickModel(body?.model),
          input: [
            {
              role: "system",
              content: [{ type: "input_text", text: system }],
            },
            {
              role: "user",
              content: [{ type: "input_text", text: user }],
            },
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
          // bubble the real OpenAI error back
          return corsJson(
            {
              ok: false,
              error: "OpenAI request failed.",
              status: resp.status,
              details: safeTruncate(rawText, 5000),
            },
            500
          );
        }

        const data = safeParseJson(rawText) ?? { _raw: rawText };

        // Extract assistant text robustly
        const assistantText = extractOutputText(data);

        // Expect JSON output; try parse
        const parsed = safeParseJson(extractJsonBlock(assistantText)) ?? null;
        if (!parsed) {
          return corsJson(
            {
              ok: false,
              error: "Model output was not valid JSON.",
              status: 500,
              details: safeTruncate(assistantText || rawText, 5000),
            },
            500
          );
        }

        // Validate shape lightly
        const replies = Array.isArray(parsed.replies) ? parsed.replies : [];
        const result = parsed.result && typeof parsed.result === "object" ? parsed.result : {};
        const notes = typeof parsed.notes === "string" ? parsed.notes : undefined;

        const responseBody = {
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
          result: {
            ...result,
            ...(notes ? { notes } : {}),
          },
          replies: replies.slice(0, normalized.variants),
        };

        return corsJson(responseBody, 200);
      }

      return corsJson({ ok: false, error: "Not found." }, 404);
    } catch (err) {
      return corsJson(
        {
          ok: false,
          error: "Server error.",
          details: safeTruncate(String(err?.stack || err?.message || err), 3000),
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
  // default (stable, cheap)
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
  // Keep this high-signal. No over-focus on “spice”.
  return `
You are ReplySense: generate text-message reply options that match the conversation's vibe and the user's style.
Goal: produce natural replies that fit context, relationship tone, and the user's writing habits.

OUTPUT RULES:
- Output MUST be valid JSON only (no markdown, no backticks).
- JSON schema:
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
- Keep replies short, like real texting.
- Avoid sounding like a bot. No therapy speak unless the user is serious.
- If the input is casual, stay casual. If it's serious, be respectful.
- "spiceLevel" is ONLY about flirtiness/romance intensity, not explicit content. If conversation is normal, spiceLevel should be 0.
- Match punctuationPreference + textQuality (clean/normal/messy) without making it unreadable.
- Never include explicit sexual content. Keep it PG-13 max.
`.trim();
}

function buildUserPrompt(r) {
  const transcript = r.conversation
    .map((m) => `${m.from.toUpperCase()}: ${m.text}`)
    .join("\n");

  return `
CONTEXT:
We are drafting replies to the latest message(s).

TRANSCRIPT:
${transcript}

PREFERENCES:
- variants: ${r.variants}
- vibe: ${r.vibe}
- tone: ${r.tone}
- writingStyle: ${r.writingStyle}
- textQuality: ${r.textQuality}
- emojiLevel: ${r.emojiLevel}
- spiceLevel: ${r.spiceLevel}
- age: ${r.age}
- punctuationPreference: ${r.punctuationPreference}

TASK:
1) Infer best-fitting values when any preference is "auto".
2) Produce ${r.variants} distinct reply options (not numbered), each as a plain string.
3) Keep replies realistic and aligned to the inferred vibe and user style.
Return JSON only.
`.trim();
}

/**
 * Extract text from OpenAI Responses API result.
 * Handles multiple shapes: output_text shortcut or output[].content[].text etc.
 */
function extractOutputText(data) {
  if (!data || typeof data !== "object") return "";
  if (typeof data.output_text === "string") return data.output_text;

  // common: data.output is an array of items, each has content array with output_text
  const out = data.output;
  if (Array.isArray(out)) {
    let buf = "";
    for (const item of out) {
      const content = item?.content;
      if (!Array.isArray(content)) continue;
      for (const c of content) {
        if (c?.type === "output_text" && typeof c?.text === "string") buf += c.text;
        if (c?.type === "output_text" && typeof c?.text === "object" && typeof c?.text?.value === "string") buf += c.text.value;
        if (typeof c?.text === "string") buf += c.text; // fallback
      }
    }
    return buf.trim();
  }

  // fallback: raw string
  return typeof data === "string" ? data : "";
}

/**
 * If model returns extra text, try to isolate JSON block.
 */
function extractJsonBlock(s) {
  const str = String(s || "").trim();
  if (!str) return str;

  const first = str.indexOf("{");
  const last = str.lastIndexOf("}");
  if (first !== -1 && last !== -1 && last > first) {
    return str.slice(first, last + 1);
  }
  return str;
}
