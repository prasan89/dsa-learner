"use client";

import { useState, useRef, useEffect } from "react";
import { Bot, Send, Lightbulb, Code2, RotateCcw, Loader2, ChevronDown, ChevronUp } from "lucide-react";
import { aiApi, type MentorContext, type MentorMessage } from "@/lib/api/ai";

interface AiMentorPanelProps {
  context: MentorContext;
  onHintsUsed?: (count: number) => void;
}

const STARTER_PROMPTS = [
  "I don't know where to start",
  "Give me a hint",
  "Why is my code wrong?",
  "Explain the time complexity",
];

type MessageWithMeta = MentorMessage & { type?: string; loading?: boolean };

export default function AiMentorPanel({ context, onHintsUsed }: AiMentorPanelProps) {
  const [messages, setMessages] = useState<MessageWithMeta[]>([
    {
      role: "assistant",
      content: `Hi! I'm your AI mentor for **${context.conceptTitle}**. I'm here to guide you — not give you the answer directly. What's on your mind?`,
      type: "guidance",
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [collapsed, setCollapsed] = useState(false);
  const [hintsUsedLocal, setHintsUsedLocal] = useState(context.hintsUsed);
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function sendMessage(text: string) {
    if (!text.trim() || loading) return;
    setError("");
    setInput("");

    const userMsg: MessageWithMeta = { role: "user", content: text };
    setMessages((prev) => [...prev, userMsg, { role: "assistant", content: "", loading: true }]);
    setLoading(true);

    const apiMessages: MentorMessage[] = messages
      .filter((m) => !m.loading && m.content)
      .map((m) => ({ role: m.role, content: m.content }));

    try {
      const updatedCtx: MentorContext = {
        ...context,
        hintsUsed: hintsUsedLocal,
      };
      const res = await aiApi.mentor(updatedCtx, text, apiMessages);
      const reply: MessageWithMeta = { role: "assistant", content: res.data.message, type: res.data.type };
      setMessages((prev) => [...prev.slice(0, -1), reply]);

      if (res.data.type === "hint") {
        const next = hintsUsedLocal + 1;
        setHintsUsedLocal(next);
        onHintsUsed?.(next);
      }
    } catch (err: unknown) {
      setMessages((prev) => prev.slice(0, -1));
      const status = (err as { response?: { status?: number } }).response?.status;
      if (status === 402) {
        setError("Out of AI credits. Purchase more to continue.");
      } else {
        setError("AI mentor is temporarily unavailable. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage(input);
    }
  }

  function renderMessage(msg: MessageWithMeta, idx: number) {
    const isUser = msg.role === "user";
    if (msg.loading) {
      return (
        <div key={idx} className="flex items-start gap-2">
          <div className="w-6 h-6 rounded-full bg-brand-100 flex items-center justify-center shrink-0 mt-0.5">
            <Bot size={12} className="text-brand-600" />
          </div>
          <div className="flex items-center gap-1.5 text-sm text-gray-400 mt-1">
            <Loader2 size={13} className="animate-spin" />
            <span>Thinking...</span>
          </div>
        </div>
      );
    }

    return (
      <div key={idx} className={`flex items-start gap-2 ${isUser ? "flex-row-reverse" : ""}`}>
        {!isUser && (
          <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 mt-0.5 ${
            msg.type === "encouragement" ? "bg-green-100" : "bg-brand-100"
          }`}>
            {msg.type === "hint" ? (
              <Lightbulb size={12} className="text-amber-600" />
            ) : msg.type === "question" ? (
              <Bot size={12} className="text-brand-600" />
            ) : (
              <Bot size={12} className="text-brand-600" />
            )}
          </div>
        )}
        <div className={`max-w-[85%] rounded-2xl px-3.5 py-2.5 text-sm leading-relaxed ${
          isUser
            ? "bg-brand-600 text-white rounded-tr-sm"
            : "bg-gray-100 text-gray-800 rounded-tl-sm"
        }`}>
          {msg.content.split("\n").map((line, i) => (
            <span key={i}>
              {line.split(/(\*\*[^*]+\*\*)/g).map((part, j) =>
                part.startsWith("**") && part.endsWith("**")
                  ? <strong key={j}>{part.slice(2, -2)}</strong>
                  : part
              )}
              {i < msg.content.split("\n").length - 1 && <br />}
            </span>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col border border-gray-200 rounded-2xl bg-white overflow-hidden">
      {/* Header */}
      <button
        onClick={() => setCollapsed((v) => !v)}
        className="flex items-center justify-between px-4 py-3 bg-gray-50 border-b border-gray-100 hover:bg-gray-100 transition-colors"
      >
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-full bg-brand-600 flex items-center justify-center">
            <Bot size={13} className="text-white" />
          </div>
          <span className="text-sm font-semibold text-gray-800">AI Mentor</span>
          <span className="text-xs text-gray-400">— guides, doesn&apos;t give answers</span>
        </div>
        {collapsed ? <ChevronDown size={14} className="text-gray-400" /> : <ChevronUp size={14} className="text-gray-400" />}
      </button>

      {!collapsed && (
        <>
          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 min-h-0" style={{ maxHeight: "320px" }}>
            {messages.map((m, i) => renderMessage(m, i))}
            <div ref={bottomRef} />
          </div>

          {/* Starter prompts (only initially) */}
          {messages.length <= 1 && (
            <div className="px-4 pb-2 flex flex-wrap gap-1.5">
              {STARTER_PROMPTS.map((p) => (
                <button
                  key={p}
                  onClick={() => sendMessage(p)}
                  className="px-2.5 py-1 rounded-full border border-brand-200 bg-brand-50 text-brand-700 text-xs font-medium hover:bg-brand-100 transition-colors"
                >
                  {p}
                </button>
              ))}
            </div>
          )}

          {/* Error */}
          {error && (
            <div className="mx-4 mb-2 px-3 py-2 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
              <RotateCcw size={12} />
              {error}
            </div>
          )}

          {/* Input */}
          <div className="border-t border-gray-100 px-3 py-2.5 flex items-end gap-2">
            <textarea
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask your mentor..."
              rows={1}
              className="flex-1 resize-none text-sm bg-transparent outline-none text-gray-800 placeholder-gray-400 py-1 max-h-28 overflow-y-auto"
              style={{ minHeight: "28px" }}
            />
            <div className="flex items-center gap-1.5 shrink-0">
              <button
                onClick={() => {
                  const reviewMsg = context.currentCode
                    ? "Can you review my current code and ask me questions to help me improve it?"
                    : "Can you review my approach and guide me?";
                  sendMessage(reviewMsg);
                }}
                title="Review my code"
                disabled={loading}
                className="p-1.5 rounded-lg text-gray-400 hover:text-brand-600 hover:bg-brand-50 transition-colors disabled:opacity-50"
              >
                <Code2 size={15} />
              </button>
              <button
                onClick={() => sendMessage(input)}
                disabled={!input.trim() || loading}
                className="p-1.5 rounded-lg bg-brand-600 text-white hover:bg-brand-700 transition-colors disabled:opacity-40"
              >
                <Send size={13} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
