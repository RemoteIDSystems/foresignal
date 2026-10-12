# Third-party notices

Foresignal itself is distributed under the MIT licence (see [LICENSE](LICENSE)). This
file records third-party works whose code has been incorporated into Foresignal, and
the notices those works require.

Foresignal's reuse policy (Architecture §16) is deliberately narrow:

| Repository | Licence | Use in Foresignal |
|---|---|---|
| God's Eye View | MIT | Fetch and parse logic may be ported to Java, with an origin header comment in the ported file and an entry in the table below. |
| ShadowBroker | AGPL-3.0 | Reference only — endpoints, parameters and upstream quirks. No code copied. |
| Velocity | AGPL-3.0 | Reference only. No code copied. |
| World Monitor | AGPL-3.0 | Reference only. No code copied. |

Data fetched from upstream providers at runtime is **not** covered by Foresignal's
licence. Each provider's terms continue to apply; see
[`docs/Source-Catalog.md`](docs/Source-Catalog.md) for per-source details.

---

## God's Eye View

Portions of Foresignal's adapter fetch and parse logic are ported from
[God's Eye View](https://github.com/bilawalsidhu/gods-eye-view), which is
distributed under the MIT licence. Ported files carry an origin comment naming
the source file, and are listed in the table below.

Only source code is ported. The upstream `LICENSE` additionally carries a
notice covering bundled datasets and 3D models that are *not* MIT-licensed;
none of those assets are used by Foresignal.

### Licence

```
MIT License

Copyright (c) 2026 Bilawal Sidhu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Ported files

No files have been ported yet. Later prompts add a row per ported file.

| Foresignal file | GEV origin file |
|---|---|

---

## Libraries

Foresignal depends on libraries through Gradle; they are not copied into the source tree. This
section lists dependencies a prompt asked to record here.

### ZXing core

- **Artifact:** `com.google.zxing:core` 3.5.4 (Maven Central), used unmodified.
- **Use:** renders the SVG QR codes of device-pairing payloads
  (`GET /v1/pairing-codes/{id}/qr.svg`, prompt 023). The tests use its QR reader to decode them.
- **Licence:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://github.com/zxing/zxing>, Copyright the ZXing authors.

### networknt json-schema-validator

- **Artifact:** `com.networknt:json-schema-validator` 3.0.8 (Maven Central), used unmodified, with its
  runtime dependencies `com.ethlo.time:itu` and `tools.jackson.dataformat:jackson-dataformat-yaml`
  (both Apache License, Version 2.0).
- **Use:** validates EDP manifests, items and provider payload schemas (JSON Schema 2020-12) at run
  time (prompt 024); earlier only the contract tests used it (prompt 020).
- **Licence:** Apache License, Version 2.0 (<https://www.apache.org/licenses/LICENSE-2.0>).
- **Project:** <https://github.com/networknt/json-schema-validator> (its LICENSE file is the plain Apache 2.0 text).

### RE2/J

- **Artifact:** `com.google.re2j:re2j` 1.8 (Maven Central), used unmodified; no runtime dependencies.
- **Use:** evaluates the `pattern` and `patternProperties` of EDP payload schemas in linear time
  through the validator's regular-expression hook (prompt 024, EDP §7).
- **Licence:** the Go license, a BSD-3-Clause license (<https://golang.org/LICENSE>).
- **Project:** <https://github.com/google/re2j>, Copyright The Go Authors.

### MCP Java SDK

- **Artifacts:** `io.modelcontextprotocol.sdk:mcp-core` and `io.modelcontextprotocol.sdk:mcp-json-jackson3`
  2.0.1 (Maven Central), used unmodified, with their runtime dependency `io.projectreactor:reactor-core`
  (3.8.7 from the Spring Boot BOM; Apache License, Version 2.0) and its `org.reactivestreams:reactive-streams`
  1.0.4 (MIT-0).
- **Use:** the MCP server at `/mcp` (prompt 025): the servlet transports (streamable HTTP and the optional
  legacy HTTP+SSE), the session and protocol classes and the JSON binding; the tests use its client.
- **Licence:** MIT License (<https://github.com/modelcontextprotocol/java-sdk/blob/main/LICENSE>).
- **Project:** <https://github.com/modelcontextprotocol/java-sdk>, Copyright (c) 2025 the original author or
  authors.

### Anthropic Java SDK

- **Artifact:** `com.anthropic:anthropic-java` 2.71.0 (Maven Central; with `anthropic-java-core` and
  `anthropic-java-client-okhttp`), used unmodified. Runtime dependencies: OkHttp 4.12.0 and Okio 3.6.0
  (Apache License, Version 2.0), the Kotlin standard library and `kotlin-reflect` (2.3.21 from the Spring Boot
  BOM; Apache License, Version 2.0), Jackson 2 with `jackson-module-kotlin` (2.21.5 from the Spring Boot BOM;
  Apache License, Version 2.0), `com.github.victools:jsonschema-generator` and its modules 4.38.0 (Apache
  License, Version 2.0), `io.swagger.core.v3:swagger-annotations` 2.2.50 (Apache License, Version 2.0),
  `com.google.errorprone:error_prone_annotations` 2.50.0 (Apache License, Version 2.0) and
  `com.standardwebhooks:standardwebhooks` 1.2.0 (MIT).
- **Use:** LLM provider type `anthropic` (prompt 026): messages (beta path, with the server-side refusal
  fallback), streaming with its message accumulator, and the Models API. The tests point it at WireMock; no
  test calls Anthropic.
- **Licence:** MIT License (<https://github.com/anthropics/anthropic-sdk-java/blob/main/LICENSE>).
- **Project:** <https://github.com/anthropics/anthropic-sdk-java>, Copyright Anthropic.

### commonmark-java

- **Artifacts:** `org.commonmark:commonmark` and `org.commonmark:commonmark-ext-gfm-tables` 0.30.1 (Maven
  Central), used unmodified; no runtime dependencies.
- **Use:** converts briefing runs' markdown to HTML for their HTML renditions (prompt 029), with raw HTML
  escaped rather than passed through and unsafe link targets dropped.
- **License:** BSD 2-Clause License (<https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt>).
- **Project:** <https://github.com/commonmark/commonmark-java>, Copyright (c) 2015, Robin Stocker.

### OWASP Java HTML Sanitizer

- **Artifact:** `com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer` 20261011.1 (Maven
  Central), used unmodified; no runtime dependencies (its SpotBugs annotations are provided-scope).
- **Use:** the allowlist applied to every briefing HTML rendition (prompt 029): headings, lists, tables,
  links with `rel="noopener nofollow"`, code and emphasis only.
- **License:** dual Apache License, Version 2.0 or BSD 2-Clause License, at the user's choice; used under
  the Apache License, Version 2.0 (<https://github.com/OWASP/java-html-sanitizer/blob/main/COPYING>).
- **Project:** <https://github.com/OWASP/java-html-sanitizer>, Copyright (c) 2011, Mike Samuel.


### H3 (Java binding)

- **Artifact:** `com.uber:h3` 4.5.0 (Maven Central), used unmodified; no runtime dependencies. The jar bundles
  the H3 C library compiled as a JNI library per platform (linux-x64, linux-arm64, darwin-x64, darwin-arm64 and
  others), extracted to `java.io.tmpdir` on first use.
- **Use:** activity rollups and stats grids (prompt 038): the H3 cell of each version's centroid, parent cells
  for coarser grids, and cell boundaries for the GeoJSON grid.
- **License:** Apache License, Version 2.0 (<https://github.com/uber/h3-java/blob/master/LICENSE>), covering
  both the Java binding and the bundled H3 core library.
- **Project:** <https://github.com/uber/h3-java>, Copyright Uber Technologies, Inc.
