/**
 * reply-sense Cloudflare Worker (single-file deploy)
 * Route: GET /, GET /health, POST /reply
 *
 * Env vars required:
 * - OPENAI_API_KEY
 * Optional:
 * - OPENAI_BASE_URL (default https://api.openai.com)
 * - OPENAI_MODEL (default gpt-4.1-mini-2025-04-14)
 * - CORS_ORIGIN (default *)
 */

export default {
  async fetch(request, env, ctx) {
    try {
      const url = new URL(request.url);
      const path = url.pathname;

      // CORS preflight
      if (request.method === "OPTIONS") {
        return new Response(null, { status: 204, headers: corsHeaders(env) });
      }

      if (request.method === "GET" && (path === "/" || path === "")) {
        return json(
          {
            ok: true,
            service: "reply-sense",
            routes: {
              "GET /": "this info",
              "GET /health": "health check",
              "POST /reply": "generate replies (send JSON body)",
            },
            exampleBody: exampleRequestBody(),
          },
          200,
          env
        );
      }

      if (request.method === "GET" && path === "/health") {
        return json({ ok: true }, 200, env);
      }

      if (request.method === "POST" && path === "/reply") {
        const body = await request.json().catch(() => null);
        if (!body || typeof body !== "object") {
          return json({ ok: false, error: "Invalid JSON body." }, 400, env);
        }

        const {
          conversation = [],
          variants = 3,
          vibe = "auto",
          tone = "auto",
          writingStyle = "auto",
          textQuality = "auto",
          emojiLevel = "auto",
          spiceLevel = "auto",
          age = "auto",
          punctuationPreference = "auto",
        } = body;

        // Minimal validation
        if (!Array.isArray(conversation) || conversation.length === 0) {
          return json(
            { ok: false, error: "conversation must be a non-empty array." },
            400,
            env
          );
        }

        const v = clampInt(variants, 1, 5);

        const model =
          (env.OPENAI_MODEL && String(env.OPENAI_MODEL)) ||
          "gpt-4.1-mini-2025-04-14";

        const sys = buildSystemPrompt();

        // Use Responses API format (fixes your earlier error)
        const userPayload = {
          conversation,
          variants: v,
          vibe,
          tone,
          writingStyle,
          textQuality,
          emojiLevel,
          spiceLevel,
          age,
          punctuationPreference,
        };

        const prompt = buildUserPrompt(userPayload);

        const result = await callOpenAIResponses(env, {
          model,
          system: sys,
          prompt,
        });

        return json(
          {
            ok: true,
            model,
            request: userPayload,
            result: result.meta,
            replies: result.replies,
          },
          200,
          env
        );
      }

      return json({ ok: false, error: "Not found." }, 404, env);
    } catch (err) {
      return json(
        { ok: false, error: "Server error.", details: String(err?.message || err) },
        500,
        env
      );
    }
  },
};

function corsHeaders(env) {
  const origin = (env.CORS_ORIGIN && String(env.CORS_ORIGIN)) || "*";
  return {
    "Access-Control-Allow-Origin": origin,
    "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
    "Access-Control-Max-Age": "86400",
  };
}

function json(obj, status, env) {
  return new Response(JSON.stringify(obj, null, 2), {
    status,
    headers: {
      ...corsHeaders(env),
      "Content-Type": "application/json; charset=utf-8",
    },
  });
}

function clampInt(v, min, max) {
  const n = Number(v);
  if (!Number.isFinite(n)) return min;
  const i = Math.trunc(n);
  return Math.max(min, Math.min(max, i));
}

function exampleRequestBody() {
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

/**
 * Core: we DO NOT over-focus on spice.
 * Spice is just one dimension, only used if it fits the conversation.
 * "Auto vibe" = infer tone, formality, emoji usage, directness, and pacing from convo.
 */
function buildSystemPrompt() {
  return [
    "You are Reply-Sense, an assistant that drafts short, natural text-message replies.",
    "Primary goal: produce replies that match the conversation vibe and the user's likely texting style.",
    "",
    "DO NOT over-focus on flirt/spice. Treat 'spice' as optional seasoning, not the main dish.",
    "If the conversation is neutral, keep it neutral.",
    "",
    "Auto-vibe rules:",
    "- Infer tone (friendly/neutral/serious/playful), directness, and pacing from the conversation.",
    "- Match typical texting patterns: contractions, sentence length, and casual phrasing.",
    "- If emojiLevel is 'auto', use emojis only if the conversation already uses them; otherwise avoid.",
    "- If punctuationPreference is 'auto', use clean readable punctuation without sounding formal.",
    "",
    "TextQuality levels:",
    "- If 'auto', default to clear, casual, polished, easy to understand.",
    "- If asked for lower quality, include minor imperfections but keep meaning clear.",
    "",
    "Safety:",
    "- Avoid explicit sexual content. If asked, keep it PG-13 and non-graphic.",
    "- No hate, harassment, or illegal instructions.",
    "",
    "Output format:",
    "- Return exactly N options (N = variants).",
    "- Each option is a single message the user can send.",
    "- Keep each option concise (generally 1–2 sentences).",
  ].join("\n");
}

function buildUserPrompt(payload) {
  const {
    conversation,
    variants,
    vibe,
    tone,
    writingStyle,
    textQuality,
    emojiLevel,
    spiceLevel,
    age,
    punctuationPreference,
  } = payload;

  const convo = conversation
    .map((m) => `${String(m.from || "them").trim()}: ${String(m.text || "").trim()}`)
    .join("\n");

  return [
    `Conversation:\n${convo}`,
    "",
    "Preferences (some may be 'auto'):",
    `- variants: ${variants}`,
    `- vibe: ${stringify(vibe)}`,
    `- tone: ${stringify(tone)}`,
    `- writingStyle: ${stringify(writingStyle)}`,
    `- textQuality: ${stringify(textQuality)}`,
    `- emojiLevel: ${stringify(emojiLevel)}`,
    `- spiceLevel: ${stringify(spiceLevel)}`,
    `- age: ${stringify(age)}`,
    `- punctuationPreference: ${stringify(punctuationPreference)}`,
    "",
    "Task: Write the reply options. Keep them natural, casual, and easy to understand.",
  ].join("\n");
}

function stringify(v) {
  if (v === undefined) return "auto";
  if (v === null) return "null";
  if (typeof v === "string") return v;
  return JSON.stringify(v);
}

async function callOpenAIResponses(env, { model, system, prompt }) {
  const apiKey = env.OPENAI_API_KEY;
  if (!apiKey) throw new Error("Missing OPENAI_API_KEY in Worker environment variables.");

  const baseUrl = (env.OPENAI_BASE_URL && String(env.OPENAI_BASE_URL)) || "https://api.openai.com";
  const url = `${baseUrl}/v1/responses`;

  const body = {
    model,
    input: [
      { role: "system", content: [{ type: "input_text", text: system }] },
      { role: "user", content: [{ type: "input_text", text: prompt }] },
    ],
    // NOTE: new parameter name (your screenshot error):
    text: { format: "text" },
  };

  const res = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const msg =
      data?.error?.message ||
      data?.message ||
      `OpenAI request failed with status ${res.status}`;
    throw new Error(msg);
  }

  // Extract text from Responses API
  const outText = extractOutputText(data);

  // We expect the model to output N lines/options. We'll split cleanly.
  const replies = normalizeReplies(outText);

  return {
    meta: {
      // lightweight insight; keep stable
      notes: "Auto vibe enabled. Spice treated as optional.",
    },
    replies,
  };
}

function extractOutputText(data) {
  // Responses API commonly returns output array with content parts.
  // We'll gather any output_text parts.
  const output = data?.output;
  if (!Array.isArray(output)) return String(data?.output_text || "").trim();

  const chunks = [];
  for (const item of output) {
    const content = item?.content;
    if (!Array.isArray(content)) continue;
    for (const part of content) {
      if (part?.type === "output_text" && typeof part?.text === "string") {
        chunks.push(part.text);
      }
    }
  }
  const joined = chunks.join("\n").trim();
  return joined || String(data?.output_text || "").trim();
}

function normalizeReplies(text) {
  const t = String(text || "").trim();
  if (!t) return [];

  // Split by lines that look like enumerations or plain newlines
  // Examples: "1) ...", "1. ...", "- ...", or just separate lines.
  const lines = t
    .split("\n")
    .map((s) => s.trim())
    .filter(Boolean)
    .map((s) => s.replace(/^(\d+[\).\]]|\-|\•)\s*/, "").trim())
    .filter(Boolean);

  // If model returned a single paragraph with separators, fallback split.
  if (lines.length <= 1 && t.includes("||")) {
    return t
      .split("||")
      .map((s) => s.trim())
      .filter(Boolean);
  }

  return lines.slice(0, 5);
}
