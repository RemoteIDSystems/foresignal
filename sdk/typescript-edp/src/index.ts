/**
 * Foresignal EDP provider library: build, validate, push and serve External Data Protocol 1.0 data.
 * Independent of the Foresignal API client, so a provider project needs only this package (and Ajv).
 *
 * @packageDocumentation
 */
export * from "./errors.ts";
export * from "./geometry.ts";
export * from "./health.ts";
export {
  author, batch, deleteItem, deleteItem as delete, document, event, feature, forecast, media, newSnapshotId,
  observation, origin, quantiles, question, snapshot, status,
} from "./items.ts";
export type {
  Batch, CommonOptions, DeleteItem, DocumentItem, EventItem, FeatureItem, ForecastItem, Item, ObservationItem, Origin,
  Quantiles, Question, Snapshot, StatusItem,
} from "./items.ts";
export * from "./manifest.ts";
export * from "./ndjson.ts";
export * from "./payloadSchema.ts";
export * from "./periods.ts";
export * from "./protocol.ts";
export * from "./pull.ts";
export * from "./push.ts";
export { createAjv, loadSchema, schemaValidator, shapeErrors, toSchemaErrors } from "./schemas.ts";
export * from "./times.ts";
export * from "./validation.ts";
