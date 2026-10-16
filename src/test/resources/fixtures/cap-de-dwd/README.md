# `cap-de-dwd-de` fixtures

Captured 2026-10-14 from the German DWD CAP feed (public domain, DWD open data):
<https://www.dwd.de/DWD/warnungen/cap-feed/de/atom.xml>.

| File | What it is |
|---|---|
| `index.xml` | The real Atom index (4 entries), captured 2026-10-14 |
| `alert.xml` | The CAP document of the first index entry (a real DWD update), captured 2026-10-14 |
| `alert2.xml` | The CAP document of the second index entry, captured 2026-10-14 |
| `chain-alert.xml` | Hand-made: the root `Alert` of a deterministic update chain |
| `chain-update.xml` | Hand-made: an `Update` that references `chain-alert.xml` |
| `chain-cancel.xml` | Hand-made: a `Cancel` that references `chain-update.xml` |
| `chain-exercise.xml` | Hand-made: a non-`Actual` (`Exercise`) message |

The `chain-*` files are synthetic so the update-chain tests are deterministic; they follow the CAP 1.2 shape of
the real captures. No key is needed.

**Recapture (prompt 143):** `curl -s https://www.dwd.de/DWD/warnungen/cap-feed/de/atom.xml -o index.xml`, then
download the `type="application/cap+xml"` links.
