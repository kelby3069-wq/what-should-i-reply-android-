const openaiPayload = {
  model: "gpt-4.1-mini",
  input: [
    {
      role: "user",
      content: [
        {
          type: "input_text",
          text: prompt
        }
      ]
    }
  ],
  response: {
    format: {
      type: "text"
    }
  }
};
