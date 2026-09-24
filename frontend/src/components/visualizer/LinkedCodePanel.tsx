"use client";

import dynamic from "next/dynamic";
import { useEffect, useRef } from "react";
import type { editor as MonacoEditor } from "monaco-editor";

const MonacoEditorComponent = dynamic(() => import("@monaco-editor/react"), { ssr: false });

// ─── Props ────────────────────────────────────────────────────────────────────

export interface LinkedCodePanelProps {
  /** The source code to display. */
  source: string;
  /** Monaco language identifier. */
  language?: string;
  /** 0-based line index to highlight. undefined = no highlight. */
  highlightLine?: number;
  /** Pixel height of the panel. */
  height?: number;
}

// ─── Component ────────────────────────────────────────────────────────────────

export default function LinkedCodePanel({
  source,
  language = "java",
  highlightLine,
  height = 220,
}: LinkedCodePanelProps) {
  const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
  // Keep a ref to the current decoration IDs so we can replace them
  const decorationsRef = useRef<string[]>([]);

  function handleEditorMount(editor: MonacoEditor.IStandaloneCodeEditor) {
    editorRef.current = editor;
    decorationsRef.current = applyHighlight(editor, decorationsRef.current, highlightLine);
  }

  // Re-apply highlight whenever highlightLine changes
  useEffect(() => {
    const editor = editorRef.current;
    if (!editor) return;
    decorationsRef.current = applyHighlight(editor, decorationsRef.current, highlightLine);
  }, [highlightLine]);

  return (
    <div
      className="rounded-xl overflow-hidden border border-gray-200 bg-[#1e1e1e]"
      style={{ height }}
      aria-label="Java source code"
    >
      <MonacoEditorComponent
        height={height}
        language={language}
        theme="vs-dark"
        value={source}
        onMount={handleEditorMount}
        options={{
          readOnly: true,
          fontSize: 12,
          minimap: { enabled: false },
          scrollBeyondLastLine: false,
          fontFamily: "JetBrains Mono, Fira Code, monospace",
          lineNumbers: "on",
          renderLineHighlight: "none",
          automaticLayout: true,
          padding: { top: 10, bottom: 10 },
          scrollbar: { verticalScrollbarSize: 4, horizontal: "hidden" },
          overviewRulerLanes: 0,
          folding: false,
          glyphMargin: false,
          contextmenu: false,
          // Suppress the "read only" tooltip
          domReadOnly: true,
        }}
      />
    </div>
  );
}

// ─── Decoration helper ────────────────────────────────────────────────────────

function applyHighlight(
  editor: MonacoEditor.IStandaloneCodeEditor,
  decorationsIds: string[],
  lineIndex: number | undefined
): string[] {
  // Monaco lines are 1-based
  const monacoLine = lineIndex !== undefined ? lineIndex + 1 : null;

  const newDecorations: MonacoEditor.IModelDeltaDecoration[] =
    monacoLine !== null
      ? [
          {
            range: {
              startLineNumber: monacoLine,
              startColumn: 1,
              endLineNumber: monacoLine,
              endColumn: 1,
            },
            options: {
              isWholeLine: true,
              className: "linked-code-highlight",
              linesDecorationsClassName: "linked-code-gutter",
            },
          },
        ]
      : [];

  const nextIds = editor.deltaDecorations(decorationsIds, newDecorations);

  if (monacoLine !== null) {
    editor.revealLineInCenterIfOutsideViewport(monacoLine);
  }

  return nextIds;
}
