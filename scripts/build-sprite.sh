#!/usr/bin/env bash
# Regenerates the MapLibre sprite and the KML icons of prompt 058 (docs/Architecture.md, "Map styles,
# feeds and KML").
#
# Outputs, committed under src/main/resources/map/:
#   sprite/foresignal.json, sprite/foresignal.png        1x SDF sprite (spreet --sdf)
#   sprite/foresignal@2x.json, sprite/foresignal@2x.png  2x SDF sprite (spreet --sdf --retina)
#   icons/<name>.png                                      one white 32 px PNG per icon, dark outline (KML)
#
# Icons come from two pinned CC0 releases:
#   Maki   v8.0.1  https://github.com/mapbox/maki        (CC0-1.0)
#   Temaki v5.13.0 https://github.com/rapideditor/temaki (CC0-1.0)
# Temaki file names use underscores; they are renamed to kebab-case so every sprite name matches
# ^[a-z0-9-]+$ (getMapIcon). ALIASES give the names adapters use in kindDisplay.icon (for example
# "earthquake", "lightning") an image from either set; the sprite then holds the alias name as its own
# entry. A kindDisplay.icon that is not in the sprite fails startup (DescriptorValidator), so add the
# name here before an adapter uses it, and run this script again.
#
# spreet v0.13.1 (https://github.com/flother/spreet, MIT) builds the sheets. It is a development tool
# only: it is downloaded into a temporary directory and never shipped.
#
# Usage:  scripts/build-sprite.sh
# Needs:  curl, tar, ImageMagick (`magick` or `convert`, for the 32 px KML icons). Linux x86_64/arm or
#         macOS x86_64/arm64 (the platforms spreet publishes binaries for).

set -euo pipefail

cd "$(dirname "$0")/.."

MAKI_TAG="v8.0.1"
TEMAKI_TAG="v5.13.0"
SPREET_TAG="v0.13.1"

OUT="src/main/resources/map"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# Icons copied from Maki under their own names.
MAKI_ICONS=(
  marker marker-stroked circle circle-stroked triangle triangle-stroked square square-stroked star
  star-stroked airport airfield heliport ferry harbor fire-station danger caution rail rail-metro
  bicycle communications-tower volcano lighthouse hospital police information car bus road-accident
  roadblock rocket mountain water dam industry observation-tower shelter globe cross home embassy
  prison monument waterfall wetland natural warehouse construction
)

# Icons copied from Temaki (underscores become hyphens).
TEMAKI_ICONS=(
  antenna radiation power chimney fighter_jet jetplane_front planes sailboat boat buoy
  tower_communication mast_communication military checkpoint security_camera oil_well crane train
  truck snow geyser_from_ground cliff_falling_rocks ruins binoculars telescope radio
)

# alias=set:icon. The names adapters use in kindDisplay.icon that neither set has.
ALIASES=(
  "earthquake=maki:circle-stroked"
  "warning=maki:caution"
  "fire=maki:fire-station"
  "lightning=temaki:power"
  "smoke=temaki:chimney"
  "storm=maki:danger"
  "forecast=maki:information"
  "station=maki:airfield"
  "outlook=maki:square-stroked"
  "storm-report=maki:star"
  "plane=maki:airport"
  "ship=temaki:boat"
  "camera=temaki:security_camera"
)

platform() {
  case "$(uname -sm)" in
    "Linux x86_64") echo "x86_64-unknown-linux-musl" ;;
    "Linux armv7l" | "Linux armv6l") echo "arm-unknown-linux-gnueabihf" ;;
    "Darwin x86_64") echo "x86_64-apple-darwin" ;;
    "Darwin arm64") echo "aarch64-apple-darwin" ;;
    *) echo "unsupported platform $(uname -sm): install spreet ${SPREET_TAG} and put it on PATH" >&2; return 1 ;;
  esac
}

if command -v magick >/dev/null 2>&1; then IM=(magick); elif command -v convert >/dev/null 2>&1; then IM=(convert); else
  echo "ImageMagick (magick or convert) is required for the KML icons" >&2; exit 1
fi

echo "Downloading Maki ${MAKI_TAG}, Temaki ${TEMAKI_TAG}"
mkdir -p "$WORK/maki" "$WORK/temaki" "$WORK/svg" "$WORK/spreet"
curl -fsSL "https://github.com/mapbox/maki/archive/refs/tags/${MAKI_TAG}.tar.gz" | tar xz -C "$WORK/maki" --strip-components=1
curl -fsSL "https://github.com/rapideditor/temaki/archive/refs/tags/${TEMAKI_TAG}.tar.gz" | tar xz -C "$WORK/temaki" --strip-components=1

if command -v spreet >/dev/null 2>&1 && spreet --version | grep -q "${SPREET_TAG#v}"; then
  SPREET="$(command -v spreet)"
else
  echo "Downloading spreet ${SPREET_TAG} (development tool, not shipped)"
  curl -fsSL "https://github.com/flother/spreet/releases/download/${SPREET_TAG}/spreet-$(platform).tar.gz" \
    | tar xz -C "$WORK/spreet"
  SPREET="$WORK/spreet/spreet"
fi

for name in "${MAKI_ICONS[@]}"; do
  cp "$WORK/maki/icons/${name}.svg" "$WORK/svg/${name}.svg"
done
for name in "${TEMAKI_ICONS[@]}"; do
  cp "$WORK/temaki/icons/${name}.svg" "$WORK/svg/${name//_/-}.svg"
done
for entry in "${ALIASES[@]}"; do
  alias="${entry%%=*}"
  target="${entry#*=}"
  set_name="${target%%:*}"
  icon="${target#*:}"
  cp "$WORK/${set_name}/icons/${icon}.svg" "$WORK/svg/${alias}.svg"
done

mkdir -p "$OUT/sprite" "$OUT/icons"
rm -f "$OUT"/icons/*.png
"$SPREET" --sdf --minify-index-file "$WORK/svg" "$OUT/sprite/foresignal"
"$SPREET" --sdf --retina --minify-index-file "$WORK/svg" "$OUT/sprite/foresignal@2x"

# KML icons: white glyph (Google Earth tints by the IconStyle color, multiplying) with a dark outline.
for svg in "$WORK"/svg/*.svg; do
  name="$(basename "$svg" .svg)"
  "${IM[@]}" -background none -density 600 "$svg" -fill white -colorize 100 -resize 26x26 \
    -gravity center -extent 32x32 \
    \( +clone -alpha extract -morphology Dilate Disk:1.5 -background '#222222' -alpha shape \) \
    +swap -compose over -composite -strip "PNG32:$OUT/icons/${name}.png"
done

echo "Sprite: $(ls "$WORK"/svg | wc -l | tr -d ' ') icons written to $OUT/sprite and $OUT/icons"
