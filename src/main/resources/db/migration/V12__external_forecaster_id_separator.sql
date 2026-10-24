-- External forecaster ids change from edp:{sourceId}/{streamId} to edp:{sourceId}:{streamId}, so an id is one path
-- segment matching the contract's forecaster id pattern ^[a-z0-9][a-z0-9._:-]{0,127}$ (updateForecaster could not
-- route the slash form). Source and stream ids are slugs ([a-z0-9-]), so the rewrite below touches only the
-- separator of an external forecaster id. The composite ids (forecast_score, forecast_calibration) and the JSON that
-- names forecasters (an ensemble's members and weights, a cycle's forecaster providers, the forecast log) are
-- rewritten too, so every reference stays consistent. No foreign key references forecaster.id.

update forecaster set id = regexp_replace(id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g')
    where id ~ 'edp:[a-z0-9-]+/';
update forecast set forecaster_id = regexp_replace(forecaster_id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g')
    where forecaster_id ~ 'edp:[a-z0-9-]+/';
update forecast set inputs = regexp_replace(inputs::text, '"edp:([a-z0-9]+(-[a-z0-9]+)*)/', '"edp:\1:', 'g')::jsonb
    where inputs::text ~ '"edp:[a-z0-9-]+/';
update forecast_score set forecaster_id = regexp_replace(forecaster_id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g'),
    id = regexp_replace(id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g')
    where forecaster_id ~ 'edp:[a-z0-9-]+/';
update forecast_calibration set forecaster_id = regexp_replace(forecaster_id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g'),
    id = regexp_replace(id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g')
    where forecaster_id ~ 'edp:[a-z0-9-]+/';
update external_question_link set forecaster_id = regexp_replace(forecaster_id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g')
    where forecaster_id ~ 'edp:[a-z0-9-]+/';
update analysis_correlation set forecaster_id = regexp_replace(forecaster_id, 'edp:([a-z0-9]+(-[a-z0-9]+)*)/', 'edp:\1:', 'g')
    where forecaster_id ~ 'edp:[a-z0-9-]+/';
update analysis_cycle set forecaster_providers = regexp_replace(forecaster_providers::text, '"edp:([a-z0-9]+(-[a-z0-9]+)*)/', '"edp:\1:', 'g')::jsonb
    where forecaster_providers::text ~ '"edp:[a-z0-9-]+/';
update forecast_log set payload = regexp_replace(payload::text, '"edp:([a-z0-9]+(-[a-z0-9]+)*)/', '"edp:\1:', 'g')::jsonb
    where payload::text ~ '"edp:[a-z0-9-]+/';
