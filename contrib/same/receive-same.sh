#!/bin/sh
# SPDX-License-Identifier: MIT
# Copyright (c) 2026 Adam Allard
#
# NOAA Weather Radio SAME receiver recipe (prompt 086): an RTL-SDR tuned to your NWR transmitter, demodulated by rtl_fm
# and decoded by multimon-ng's EAS decoder, posted to Foresignal by foresignal-post.
#
#     NWR_FREQ=162.550M FORESIGNAL_URL=... FORESIGNAL_SOURCE=custom-nwr FORESIGNAL_INGEST_KEY=eik_... ./receive-same.sh
#
# NWR frequencies are 162.400 to 162.550 MHz (https://www.weather.gov/nwr/). multimon-ng's EAS decoder reads 22,050 Hz
# signed 16-bit raw audio. Requires rtl-sdr (rtl_fm) and multimon-ng.
set -eu
: "${NWR_FREQ:?set NWR_FREQ, for example 162.550M}"
DIR=$(dirname "$0")
rtl_fm -f "${NWR_FREQ}" -M fm -s 22050 -E dc - \
  | multimon-ng -a EAS -t raw - \
  | "${DIR}/foresignal-post"
