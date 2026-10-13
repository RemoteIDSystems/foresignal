/**
 * NOAA MRMS sources from the {@code noaa-mrms-pds} bucket: the {@code mrms} product source (prompt
 * 046: reflectivity, precipitation rate with its PrecipFlag band, and 60-minute MESH, every 2-minute
 * frame; prompt 053 adds the 60-minute rotation track, QPE 1 h and 24 h and the flash-flood guidance ratio at
 * fixed {@code takeEvery}) and the {@code mrms-probsevere} record source (prompt 047: ProbSevere v3 storm objects inside
 * the places AOI).
 */
package ai.foresignal.source.adapters.mrms;
