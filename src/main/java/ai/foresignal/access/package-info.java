/**
 * Map-client access (prompt 050; Weather-Imagery-Design §6.7): tile keys, the {@code fsk_} path-carrier
 * variant of prompt 035's feed keys. They are stored as {@code api_token} rows with
 * {@code type = tile_key}, resolved by 035's {@code PathKeyFilter} through {@link
 * ai.foresignal.access.TileKeyService} (its {@code PathKeyResolver} for {@code fsk_}), and limited by
 * 035's single restriction enforcement point ({@code RestrictionClamp}). There is no second token
 * table, hasher or enforcement path.
 */
package ai.foresignal.access;
