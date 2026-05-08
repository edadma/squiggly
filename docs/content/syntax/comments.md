---
title: Comments
summary: How to leave notes for future-you without affecting the rendered output.
weight: 40
---

Squiggly recognizes a single comment form, started inside an action with `//`:

```
{{ // This won't appear in the output. }}
```

Everything from `//` to the closing `}}` is dropped at parse time. Useful for marking why a partial is being called or why a block is empty:

```
{{ partial 'sidebar' . }}
{{ // sidebar is intentionally rendered before the main column so its
   // search input gets keyboard focus first. }}
{{ block main . }} … {{ end }}
```

There is no `/* … */` style. If you want a multi-line comment, use multiple single-line ones, or wrap in `{{ if false }} … {{ end }}` (the cost is the parser still walks the unused branch — fine for prose, expensive only if you nest very large templates).
