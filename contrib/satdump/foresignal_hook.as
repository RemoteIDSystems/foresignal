/*
 * Foresignal SatDump 2.0 hook (prompt 107) — MIT License
 * Copyright (c) 2026 Foresignal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * -----------------------------------------------------------------------------------------------
 * STATUS: EXPERIMENTAL and UNVERIFIED against a SatDump 2.0 build (2026-10: 2.0 exists only as
 * alpha/nightly; 1.2.2 is the latest stable). It is a separate, operator-supplied file: Foresignal
 * does NOT bundle it, does not link it, and its images do not contain it. It copies no SatDump code,
 * headers or structs.
 *
 * It runs when a product finishes and writes, beside the product files:
 *   - equirectangular GeoTIFF/PNG composites, and
 *   - foresignal-manifest.json, matching the schema Foresignal's ForesignalManifest reader expects:
 *       { "schema": 1, "satellite": "...", "norad": 57166, "timestamp": "<ISO-8601>",
 *         "products": [ { "name": "rgb", "instrument": "MSU-MR", "file": "MSU-MR/rgb.tif",
 *                         "projection": "equirec", "bounds": [-180,-90,180,180], "composite": "rgb_MSU-MR" } ] }
 *
 * Requires SatDump built with PLUGIN_SCRIPTING (off by default). The AngelScript API below is the
 * intended shape; verify it against the installed 2.0 build before enabling.
 *
 * See README.md for installation and the manifest specification.
 */

// --- Intended hook shape (verify against the installed SatDump 2.0 build) -----------------------

void foresignal_on_product_finish(const string &in product_name, const string &in instrument,
                                  const string &in output_dir, const json &in dataset)
{
    // 1. Build an equirectangular composite of the product bands.
    //    (SatDump: save a "project" composite with type=equirec; exact call names vary by build.)
    string composite = product_name + "_" + instrument;
    string file = instrument + "/" + composite + ".tif";
    // satdump_project_equirec(product_name, output_dir + "/" + file);  // <-- verify name

    // 2. Write the manifest Foresignal reads.
    json manifest;
    manifest["schema"] = 1;
    manifest["satellite"] = dataset["satellite"];
    if (dataset.has("norad")) { manifest["norad"] = dataset["norad"]; }
    manifest["timestamp"] = dataset["timestamp"];
    manifest["products"] = json::array();
    json entry;
    entry["name"] = product_name;
    entry["instrument"] = instrument;
    entry["file"] = file;
    entry["projection"] = "equirec";
    entry["bounds"] = json::array({ -180.0, -90.0, 180.0, 90.0 });
    entry["composite"] = composite;
    manifest["products"].push_back(entry);
    // satdump_write_json(output_dir + "/foresignal-manifest.json", manifest);  // <-- verify name
}
