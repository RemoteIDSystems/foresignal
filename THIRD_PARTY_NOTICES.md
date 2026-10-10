# Third-party notices

Elaro itself is distributed under the MIT licence (see [LICENSE](LICENSE)). This
file records third-party works whose code has been incorporated into Elaro, and
the notices those works require.

Elaro's reuse policy (Architecture §16) is deliberately narrow:

| Repository | Licence | Use in Elaro |
|---|---|---|
| God's Eye View | MIT | Fetch and parse logic may be ported to Java, with an origin header comment in the ported file and an entry in the table below. |
| ShadowBroker | AGPL-3.0 | Reference only — endpoints, parameters and upstream quirks. No code copied. |
| Velocity | AGPL-3.0 | Reference only. No code copied. |
| World Monitor | AGPL-3.0 | Reference only. No code copied. |

Data fetched from upstream providers at runtime is **not** covered by Elaro's
licence. Each provider's terms continue to apply; see
[`docs/Source-Catalog.md`](docs/Source-Catalog.md) for per-source details.

---

## God's Eye View

Portions of Elaro's adapter fetch and parse logic are ported from
[God's Eye View](https://github.com/bilawalsidhu/gods-eye-view), which is
distributed under the MIT licence. Ported files carry an origin comment naming
the source file, and are listed in the table below.

Only source code is ported. The upstream `LICENSE` additionally carries a
notice covering bundled datasets and 3D models that are *not* MIT-licensed;
none of those assets are used by Elaro.

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

| Elaro file | GEV origin file |
|---|---|
