# Decoder self-test fixtures (prompt 046)

`DecoderSelfTest` decodes one file per decoding path at startup. Each is a real upstream file or a
single message cut from one, unmodified, and under 200 KB. All were captured on 2026-10-12.

| File | Path proved | Bytes | SHA-256 | Source |
|---|---|---|---|---|
| `gfs-1p00-tmp2m-f000.grib2` | GRIB2 template 5.3 (complex packing, spatial differencing) | 74,618 | `853769556bdbbec11855191feb097ca6b70634ca353f2644260a784fa2bf715b` | Message 580 (`TMP:2 m above ground:anl`) of `https://noaa-gfs-bdp-pds.s3.amazonaws.com/gfs.20261012/00/atmos/gfs.t00z.pgrb2.1p00.f000`, range-read with the offsets from the `.idx` file: bytes 34204768–34279385 (the next message starts at 34279386). |
| `mrms-hawaii-mesh-60m-20261012-000000.grib2` | GRIB2 template 5.41 (PNG) | 20,148 | `c3eae67495b57fc79554fcee3b4554ce30685724ce8a00a05edec0d7cf08f4e5` | `https://noaa-mrms-pds.s3.amazonaws.com/HAWAII/MESH_Max_60min_00.50/20261012/MRMS_MESH_Max_60min_00.50_20261012-000000.grib2.gz`, gunzipped. The Hawaii domain (2600 × 2200 at 0.005°) is used instead of CONUS (7000 × 3500) so the startup decode allocates 23 MB rather than 98 MB. |
| `ecmwf-ifs-0p25-lsm-0h.grib2` | GRIB2 template 5.42 (CCSDS/AEC, libaec) | 187,781 | `f69096d46766c15dc3100121d535074774bdaece3a15062fa6e54b43f2bd270c` | The `lsm` (land-sea mask) message of `https://data.ecmwf.int/forecasts/20261012/00z/ifs/0p25/oper/20261012000000-0h-oper-fc.grib2`, range-read with the `_offset` 60018644 and `_length` 187781 from the matching `.index` file. ECMWF open data, CC BY 4.0 (© ECMWF). |
| `goes19-abi-dmwvc-c08-20261012-0001.nc` | NetCDF4/HDF5 | 144,520 | `a9f3c0665d1b4bd4cd2b24f6c759f04e963879748f33aa8fa4cac39a8db3f39f` | `https://noaa-goes19.s3.amazonaws.com/ABI-L2-DMWVC/2026/285/00/OR_ABI-L2-DMWVC-M6C08_G19_s20262850001182_e20262850003555_c20262850034149.nc` (GOES-19 ABI derived motion winds, CONUS, band 8), renamed. The self-test reads `wind_speed` (712 values). |

Run `sha256sum src/main/resources/raster/selftest/*` to check the files.

Why these: the GFS 0.25° messages are 0.3–1 MB, so the 1° file stands in for complex packing; every
CONUS MRMS field is 7000 × 3500; the smaller ECMWF messages at step 0 are constant fields encoded
without CCSDS, and `lsm` is the smallest real CCSDS message under 200 KB; NetCDF-Java cannot write
NetCDF4 without the native library, so a real small GOES file is used rather than a made-up one.

The GOES and MRMS data are NOAA open data (US public domain). The GFS message is NOAA/NCEP (US
public domain).
