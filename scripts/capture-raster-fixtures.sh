#!/usr/bin/env bash
# Captures one real file per raster product for the prompt 046 tests (docs/Adapter-Guide.md,
# "Raster fixtures").
#
# For each MRMS product (reflectivity, PrecipRate, PrecipFlag, MESH) and GOES-19 ABI CMIPC band 13
# it lists today's prefix anonymously over HTTPS, takes the newest key and downloads it unchanged
# (real GRIB2 and NetCDF4 files cannot be cropped without re-encoding them). PrecipRate and
# PrecipFlag are taken at the same timestamp, as the adapter pairs them. Prints each file's size and
# sha256 for the fixture README.
#
# Files under 3 MB belong in src/test/resources/fixtures/{mrms,goes}/; larger ones get a pinned
# entry in build.gradle.kts (downloadTestFixtures) instead of a commit.
#
# Usage:  scripts/capture-raster-fixtures.sh [output-dir]      (default: build/captured-fixtures)
# Needs:  curl, sha256sum (or shasum), python3. No AWS account: both buckets are public. With the AWS
#         CLI installed, `aws s3 cp --no-sign-request s3://<bucket>/<key> .` fetches the same objects.

set -euo pipefail

cd "$(dirname "$0")/.."
OUT="${1:-build/captured-fixtures}"
mkdir -p "$OUT/mrms" "$OUT/goes"

DAY="$(date -u +%Y%m%d)"
YEAR="$(date -u +%Y)"
DOY="$(date -u +%j)"
HOUR="$(date -u +%H)"

sha() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}

# Prints the keys of a public bucket listing, one per line (first page, up to 1,000 keys).
list_keys() {
  local bucket="$1" prefix="$2"
  curl -fsS "https://${bucket}.s3.amazonaws.com/?list-type=2&prefix=${prefix}" \
    | python3 -c 'import re,sys; print("\n".join(re.findall(r"<Key>([^<]+)</Key>", sys.stdin.read())))'
}

fetch() {
  local bucket="$1" key="$2" dir="$3"
  local file="$dir/$(basename "$key")"
  curl -fsS -o "$file" "https://${bucket}.s3.amazonaws.com/${key}"
  printf '%s\t%s bytes\tsha256 %s\n' "$file" "$(wc -c < "$file" | tr -d ' ')" "$(sha "$file")"
}

MRMS=noaa-mrms-pds
for product in MergedReflectivityQCComposite_00.50 MESH_Max_60min_00.50; do
  key="$(list_keys "$MRMS" "CONUS/${product}/${DAY}/" | tail -n 1)"
  [ -n "$key" ] || { echo "no ${product} key today" >&2; exit 1; }
  fetch "$MRMS" "$key" "$OUT/mrms"
done

# The newest PrecipRate timestamp that also has a PrecipFlag file.
rate_keys="$(list_keys "$MRMS" "CONUS/PrecipRate_00.00/${DAY}/")"
flag_keys="$(list_keys "$MRMS" "CONUS/PrecipFlag_00.00/${DAY}/")"
stamp=""
while read -r key; do
  s="${key##*_}"; s="${s%.grib2.gz}"
  if grep -q "_${s}.grib2.gz" <<< "$flag_keys"; then stamp="$s"; fi
done <<< "$rate_keys"
[ -n "$stamp" ] || { echo "no paired PrecipRate/PrecipFlag files today" >&2; exit 1; }
fetch "$MRMS" "CONUS/PrecipRate_00.00/${DAY}/MRMS_PrecipRate_00.00_${stamp}.grib2.gz" "$OUT/mrms"
fetch "$MRMS" "CONUS/PrecipFlag_00.00/${DAY}/MRMS_PrecipFlag_00.00_${stamp}.grib2.gz" "$OUT/mrms"

GOES=noaa-goes19
key="$(list_keys "$GOES" "ABI-L2-CMIPC/${YEAR}/${DOY}/${HOUR}/OR_ABI-L2-CMIPC-M" | grep -E 'M[0-9]C13_G19_s' | tail -n 1 || true)"
if [ -z "$key" ]; then
  PREV="$(date -u -d '1 hour ago' +%Y/%j/%H 2>/dev/null || date -u -v-1H +%Y/%j/%H)"
  key="$(list_keys "$GOES" "ABI-L2-CMIPC/${PREV}/OR_ABI-L2-CMIPC-M" | grep -E 'M[0-9]C13_G19_s' | tail -n 1)"
fi
fetch "$GOES" "$key" "$OUT/goes"

echo "Captured into $OUT. Update the fixture READMEs and, for files over 3 MB, the pins in build.gradle.kts."
