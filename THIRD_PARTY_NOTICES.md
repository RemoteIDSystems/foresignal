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
