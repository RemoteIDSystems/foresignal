-- The pre-1.0 bootstrap SQL that ran AFTER Hibernate's ddl-auto=update, preserved for the test-only pre-cutover
-- schema generator (FlywaySchemaParityIT, prompt 144). Sources, all deleted or reduced in 144:
-- SpatialIndexInitializer.STATEMENTS, InfraSchemaInitializer.STATEMENTS, EmbeddingSchemaManager.BASE_STATEMENTS and
-- DeliveryStatusConstraint.DROP. Statements are separated by a line holding only "--;".
DROP INDEX IF EXISTS record_head_geom_gist
--;
CREATE INDEX IF NOT EXISTS record_live_geom_gist ON record_live USING GIST (geom)
--;
ALTER TABLE record_live SET (fillfactor = 70, autovacuum_vacuum_scale_factor = 0.02, autovacuum_vacuum_cost_limit = 2000)
--;
CREATE INDEX IF NOT EXISTS trigger_rule_geofence_gist ON trigger_rule USING GIST (geofence)
--;
CREATE INDEX IF NOT EXISTS place_area_gist ON place USING GIST (area)
--;
CREATE INDEX IF NOT EXISTS saved_view_place_ids_idx ON saved_view USING GIN (place_ids)
--;
CREATE SEQUENCE IF NOT EXISTS alert_rev INCREMENT BY 1
--;
CREATE INDEX IF NOT EXISTS alert_open_idx ON alert (rule_id, source_id, external_id) WHERE state <> 'resolved'
--;
CREATE INDEX IF NOT EXISTS ref_country_geom_gist ON ref_country USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS ref_country_geom_simple_gist ON ref_country USING GIST (geom_simple)
--;
CREATE INDEX IF NOT EXISTS ref_admin1_geom_gist ON ref_admin1 USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS ref_admin1_geom_simple_gist ON ref_admin1 USING GIST (geom_simple)
--;
CREATE INDEX IF NOT EXISTS ref_admin1_gn_code_idx ON ref_admin1 (gn_code)
--;
CREATE INDEX IF NOT EXISTS ref_admin2_geom_gist ON ref_admin2 USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS ref_sea_geom_gist ON ref_sea USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS gazetteer_place_geog_gist ON gazetteer_place USING GIST (geog)
--;
CREATE INDEX IF NOT EXISTS record_region_iso2_idx ON record_region (iso2)
--;
CREATE INDEX IF NOT EXISTS record_region_iso2_admin1_idx ON record_region (iso2, admin1)
--;
CREATE INDEX IF NOT EXISTS raster_frame_product_valid_idx ON raster_frame (product_id, valid_time DESC)
--;
CREATE INDEX IF NOT EXISTS raster_frame_footprint_gist ON raster_frame USING GIST (footprint)
--;
CREATE INDEX IF NOT EXISTS record_head_series_point_idx ON record_head (source_id, (payload->>'series'), event_time) WHERE kind = 'series-point'
--;
CREATE UNIQUE INDEX IF NOT EXISTS entity_key_open_idx ON entity_key (key_type, key_value) WHERE valid_to IS NULL
--;
CREATE UNIQUE INDEX IF NOT EXISTS entity_link_open_idx ON entity_link (source_id, external_id) WHERE valid_to IS NULL
--;
CREATE INDEX IF NOT EXISTS entity_alias_lower_idx ON entity_alias (lower(alias) text_pattern_ops)
--;
CREATE INDEX IF NOT EXISTS entity_link_entity_open_idx ON entity_link (entity_id) WHERE valid_to IS NULL
--;
CREATE INDEX IF NOT EXISTS trip_corridor_gist ON trip USING GIST (corridor)
--;
CREATE INDEX IF NOT EXISTS situation_geom_gist ON situation USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS situation_region_codes_gin ON situation USING GIN (region_codes)
--;
CREATE INDEX IF NOT EXISTS analytic_finding_geom_gist ON analytic_finding USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS analytic_finding_region_codes_gin ON analytic_finding USING GIN (region_codes)
--;
CREATE INDEX IF NOT EXISTS infra_node_geom_gist ON infra_node USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS infra_node_point_gist ON infra_node USING GIST (point)
--;
CREATE INDEX IF NOT EXISTS infra_node_type_idx ON infra_node (facility_type)
--;
CREATE INDEX IF NOT EXISTS infra_node_build_type_idx ON infra_node (build_id, facility_type, id)
--;
CREATE UNIQUE INDEX IF NOT EXISTS infra_node_build_record_idx ON infra_node (build_id, source_id, external_id)
--;
CREATE UNIQUE INDEX IF NOT EXISTS infra_edge_unique_idx ON infra_edge (from_node, to_node, relation, rule_id)
--;
CREATE INDEX IF NOT EXISTS infra_edge_build_from_idx ON infra_edge (build_id, from_node)
--;
CREATE INDEX IF NOT EXISTS infra_edge_build_to_idx ON infra_edge (build_id, to_node)
--;
ALTER TABLE embedding_document ADD COLUMN IF NOT EXISTS tsv tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(text, ''))) STORED
--;
CREATE INDEX IF NOT EXISTS embedding_document_tsv_gin ON embedding_document USING GIN (tsv)
--;
CREATE INDEX IF NOT EXISTS embedding_document_geom_gist ON embedding_document USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS embedding_document_source_time_idx ON embedding_document (source_id, effective_time)
--;
alter table if exists notification_delivery drop constraint if exists notification_delivery_status_check
--;
