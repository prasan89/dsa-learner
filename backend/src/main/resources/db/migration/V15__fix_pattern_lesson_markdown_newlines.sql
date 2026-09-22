-- V15: Fix literal \n sequences in pattern lesson_markdown fields.
--
-- V4 inserted lesson_markdown using plain SQL string literals, so the escape
-- sequences were stored verbatim as two characters (\  n) instead of a real
-- newline (chr(10)).  ReactMarkdown/CommonMark cannot render literal \n as
-- paragraph breaks; this migration replaces them with actual newlines.
--
-- Only the 10 DSA patterns are affected (category = 'DSA').
-- System Design patterns (V8/V9) used E'...' syntax and are already correct.
-- replace() treats the search string as a literal, not a regex pattern.

UPDATE patterns
SET lesson_markdown = replace(lesson_markdown, '\n', E'\n')
WHERE category = 'DSA'
  AND lesson_markdown IS NOT NULL
  AND lesson_markdown LIKE '%\n%';
