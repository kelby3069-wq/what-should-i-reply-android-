export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // Basic CORS (Android won't need it, but keeps future web easy)
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: corsHeaders(request),
      });
    }

    if (url.pathname !== "/reply") {
      return json({ error: "Not found" }, 404, request);
    }

    if (request.method !== "POST") {
      return json({ error: "Use POST" }, 405, request);
    }

    let payload;
    try {
      payload = await request.json();
    } catch {
      return json({ error: "Invalid JSON" }, 400, request);
    }

    const context = (payload.context || "").trim();
    const tone = (payload.tone || "Calm & Polite").trim();
    const goal = (payload.goal || "").trim();

    if (!context) return json({ error: "Missing context" }, 400, request);

    if (!env.OPENAI_API_KEY) {
      return json({ error: "Server missing OPENAI_API_KEY" }, 500, request);
    }

    // System-style instructions go in `instructions` for Responses API. 4
    const instructions = [
      "You generate 3 high-quality text message replies.",
      "Use the FULL_CONTEXT to understand the thread, but reply ONLY to TARGET.",
      `Tone: ${tone}.`,
      goal ? `Goal: ${goal}.` : "",
      "",
      "Rules:",
      "- Output MUST be valid JSON only.",
      '- Format: {"replies":["...","...","..."]}',
      "- Replies should be natural, not robotic.",
      "- Keep each reply under 240 characters unless needed.",
      "- No lectures. No analysis. Just replies."
    ].filter(Boolean).join("\n");

    const openaiBody = {
      model: env.OPENAI_MODEL || "gpt-4.1-mini",
      instructions,
      input: context
    };

    // Responses API endpoint 5
    const r = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.OPENAI_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(openaiBody),
    });

    const raw = await r.text();

    if (!r.ok) {
      return json({ error: "OpenAI error", status: r.status, raw: raw.slice(0, 800) }, 502, request);
    }

    // Try to extract text output
    let textOut = "";
    try {
      const j = JSON.parse(raw);
      // Responses API typically includes output text in `output_text` (best-effort extraction)
      // If absent, fall back to scanning output array.
      textOut = (j.output_text || "").trim();

      if (!textOut && Array.isArray(j.output)) {
        // best effort: concatenate any text parts
        const parts = [];
        for (const item of j.output) {
          if (item && item.content) {
            for (const c of item.content) {
              if (c && typeof c.text === "string") parts.push(c.text);
            }
          }
        }
        textOut = parts.join("").trim();
      }
    } catch {
      // if parsing fails, treat raw as text
      textOut = raw.trim();
    }

    // Now parse the model's JSON
    let parsed;
    try {
      parsed = JSON.parse(textOut);
    } catch {
      // Fallback: split lines into replies
      const lines = textOut
        .split("\n")
        .map(s => s.trim())
        .filter(Boolean)
        .slice(0, 3);
      parsed = { replies: lines.length ? lines : ["Sorry — try again."] };
    }

    const replies = Array.isArray(parsed.replies) ? parsed.replies.slice(0, 3) : [];
    if (!replies.length) replies.push("Sorry — try again.");

    return json({ replies }, 200, request);
  }
};

function json(obj, status, request) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      ...corsHeaders(request),
    },
  });
}

function corsHeaders(request) {
  const origin = request.headers.get("Origin") || "*";
  return {
    "Access-Control-Allow-Origin": origin,
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
  };
}
