# Reference data test fixtures (prompt 040)

Tiny stand-ins for the datasets of `src/main/resources/reference/manifest.json`, named like the real files so
`ReferenceFixtures` can load them through the production loader (offline directory, pinned SHA-256 computed
at test time).

- The Natural Earth files hold **synthetic squares**, not real boundaries: United States `[-110,25,-90,40]`,
  Mexico `[-110,14,-90,24.9]`, Texas `US-TX [-106,26,-94,36]`, Nuevo León `MX-NLE [-101,23,-99,24.9]`,
  Travis County `48453 [-98.2,30.0,-97.4,30.6]`, Harris County `48201 [-95.9,29.5,-94.9,30.2]`, Gulf of
  Mexico `[-90,20,-80,30]`, plus one `ISO_A2_EH = -99` feature that the loader must skip.
- The GeoNames files hold three cities (Austin, Houston, Monterrey) with their GeoNames ids, coordinates and
  populations, the two matching admin-1 names and two countryInfo rows; `cities15000.zip` is built from
  `cities15000.txt` at test time. GeoNames data is CC BY 4.0 (https://www.geonames.org/).
