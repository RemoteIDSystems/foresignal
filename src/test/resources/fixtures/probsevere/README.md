# Fixtures: `mrms-probsevere` (prompt 047)

| File | What it is |
|---|---|
| `MRMS_PROBSEVERE_20261012_235841.json` | A real ProbSevere v3 file, untrimmed: `validTime` `20261012_235841 UTC`, 86 features, every property a string. 10 features touch the default Texas AOI `[-106.65, 25.84, -93.51, 36.50]`. |
| `listing-20261012.xml` | A real ListObjectsV2 response for `ProbSevere/20261012/` (719 keys that day), trimmed to the last 3 `Contents` entries with `KeyCount` set to 3. |

## Capture procedure

- **Date:** 2026-10-13 00:01 UTC.
- **Commands:** list the day, then download only the newest key:

  ```sh
  D=20261012
  curl -sS "https://noaa-mrms-pds.s3.amazonaws.com/?list-type=2&prefix=ProbSevere/$D/" -o listing.xml
  K=$(grep -o "<Key>[^<]*</Key>" listing.xml | tail -1 | sed 's/<[^>]*>//g')
  curl -sS "https://noaa-mrms-pds.s3.amazonaws.com/$K" -o "$(basename "$K")"
  ```

- **Response:** the file is 264,916 bytes (`Size` in the listing). Just after 00:00 UTC the new day's
  prefix lists nothing for a few minutes, which is why the adapter falls back to the previous day.

The adapter tests derive further files from this one (another `validTime`, one object removed) in code.
