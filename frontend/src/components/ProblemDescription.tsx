"use client";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { Components } from "react-markdown";

// Normalize literal \n escape sequences (stored as backslash-n in DB)
// into real newlines so markdown renders correctly.
function normalize(text: string): string {
  return text.replace(/\\n/g, "\n");
}

const MD_COMPONENTS: Components = {
  // Inline code: green pill
  code({ children, className }) {
    // Block code fences come with a language className
    if (className) {
      return (
        <code className={`${className} block bg-gray-900 text-green-300 rounded-lg p-3 text-xs font-mono overflow-x-auto whitespace-pre`}>
          {children}
        </code>
      );
    }
    return (
      <code className="text-green-700 bg-green-50 border border-green-100 px-1.5 py-0.5 rounded text-[0.8em] font-mono">
        {children}
      </code>
    );
  },
  // Fenced code blocks
  pre({ children }) {
    return (
      <pre className="bg-gray-900 text-green-300 rounded-xl p-4 text-xs overflow-x-auto font-mono leading-relaxed my-3">
        {children}
      </pre>
    );
  },
  // Bold
  strong({ children }) {
    return <strong className="font-semibold text-gray-900">{children}</strong>;
  },
  // Paragraphs
  p({ children }) {
    return <p className="text-gray-700 leading-relaxed mb-3 last:mb-0">{children}</p>;
  },
  // Unordered list
  ul({ children }) {
    return <ul className="list-none space-y-1.5 mb-3">{children}</ul>;
  },
  // Ordered list
  ol({ children }) {
    return <ol className="list-decimal list-inside space-y-1.5 mb-3 text-gray-700">{children}</ol>;
  },
  // List items
  li({ children }) {
    return (
      <li className="text-gray-700 flex gap-2">
        <span className="text-gray-300 mt-1 shrink-0">•</span>
        <span>{children}</span>
      </li>
    );
  },
  // Headings
  h3({ children }) {
    return <h3 className="text-sm font-bold text-gray-900 mt-4 mb-1">{children}</h3>;
  },
  h4({ children }) {
    return <h4 className="text-xs font-semibold text-gray-700 mt-3 mb-1">{children}</h4>;
  },
};

// Renders problem description markdown with all normalizations applied.
export function ProblemMarkdown({ text }: { text: string }) {
  return (
    <ReactMarkdown remarkPlugins={[remarkGfm]} components={MD_COMPONENTS}>
      {normalize(text)}
    </ReactMarkdown>
  );
}

// Example type matching the JSON stored in DB
interface Example {
  input: string;
  output: string;
  explanation?: string;
}

// Renders a single example as a first-class structured block.
function ExampleBlock({ example, index }: { example: Example; index: number }) {
  return (
    <div className="rounded-xl border border-gray-200 overflow-hidden bg-white">
      {/* Header */}
      <div className="px-4 py-2 bg-gray-50 border-b border-gray-100">
        <span className="text-xs font-semibold text-gray-500">Example {index + 1}</span>
      </div>

      <div className="p-4 space-y-3">
        {/* Input */}
        <div>
          <span className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wider mb-1">Input</span>
          <code className="block bg-gray-50 border border-gray-100 rounded-lg px-3 py-2 text-xs font-mono text-gray-800 whitespace-pre-wrap">
            {example.input}
          </code>
        </div>

        {/* Output */}
        <div>
          <span className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wider mb-1">Output</span>
          <code className="block bg-green-50 border border-green-100 rounded-lg px-3 py-2 text-xs font-mono text-green-800">
            {example.output}
          </code>
        </div>

        {/* Explanation */}
        {example.explanation && (
          <div>
            <span className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wider mb-1">Explanation</span>
            <p className="text-xs text-gray-600 leading-relaxed">{example.explanation}</p>
          </div>
        )}
      </div>
    </div>
  );
}

// Renders examples JSON string as structured blocks.
export function ProblemExamples({ examples }: { examples: string }) {
  let parsed: Example[] = [];
  try {
    parsed = JSON.parse(examples);
  } catch {
    return null;
  }
  if (!parsed.length) return null;

  return (
    <div className="space-y-3">
      {parsed.map((ex, i) => (
        <ExampleBlock key={i} example={ex} index={i} />
      ))}
    </div>
  );
}

// Renders constraints string (newline-separated, may contain \n escapes)
// as a structured bullet list with inline code styling.
export function ProblemConstraints({ constraints }: { constraints: string }) {
  const items = normalize(constraints)
    .split("\n")
    .map((c) => c.trim())
    .filter(Boolean);

  if (!items.length) return null;

  return (
    <div>
      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Constraints</p>
      <ul className="space-y-1.5">
        {items.map((c, i) => (
          <li key={i} className="flex gap-2 text-xs text-gray-600">
            <span className="text-gray-300 shrink-0 mt-0.5">•</span>
            <ConstraintLine text={c} />
          </li>
        ))}
      </ul>
    </div>
  );
}

// Tokenizes a constraint line and renders backtick segments as inline code.
function ConstraintLine({ text }: { text: string }) {
  const parts = text.split(/(`[^`]+`)/g);
  return (
    <span className="font-mono leading-relaxed">
      {parts.map((part, i) =>
        part.startsWith("`") && part.endsWith("`") ? (
          <code key={i} className="text-green-700 bg-green-50 border border-green-100 px-1 py-0.5 rounded text-[0.85em]">
            {part.slice(1, -1)}
          </code>
        ) : (
          <span key={i}>{part}</span>
        )
      )}
    </span>
  );
}
