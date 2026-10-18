/**
 * The object envelope every reference resolves to (docs/Client-Contract.md §3, schema `ObjectEnvelope`). Written
 * by hand so the contract helpers do not depend on the generated types; members are optional where the server
 * may leave them out.
 */

/** An action the calling token may take on an object. */
export interface ObjectAction {
  id: string;
  label?: string;
  method?: string;
  href?: string;
  scope?: string;
  [member: string]: unknown;
}

/** A GeoJSON geometry (simplified to at most 500 vertices in envelopes). */
export interface EnvelopeGeometry {
  type: string;
  coordinates?: unknown;
  geometries?: EnvelopeGeometry[];
}

/** The uniform summary of any Foresignal object. */
export interface ObjectEnvelope {
  ref: string;
  type: string;
  title: string;
  summary?: string | null;
  textOrigin?: "deterministic" | "llm";
  status?: string | null;
  severity?: string | null;
  time?: { start?: string | null; end?: string | null; observedAt?: string | null };
  geometry?: EnvelopeGeometry | null;
  bbox?: [number, number, number, number] | null;
  private?: boolean;
  tlp?: string | null;
  tags?: string[];
  provenance?: { sourceIds?: string[]; manifestId?: string | null; asOfSeq?: number | null; [member: string]: unknown };
  links?: { self?: string; related?: string; [member: string]: unknown };
  actions?: ObjectAction[];
  [member: string]: unknown;
}

/** The result of resolving references: envelopes, and the refs that are missing or invisible. */
export interface ResolveResult {
  items: ObjectEnvelope[];
  missing: string[];
  asOf?: string | null;
}

/** The versions in force (`GET /v1/contract`). */
export interface ContractInfo {
  contractVersion: string;
  objectTypes: string[];
  relationTypes: string[];
  contextSchemaVersion: number;
  deepLinkVersion: number;
}

/** The context schema version this SDK writes and validates. */
export const CONTEXT_SCHEMA_VERSION = 1;

/** The deep link payload version this SDK encodes and decodes. */
export const DEEP_LINK_VERSION = 1;

/** The contract major version this SDK was built for (within `/v1` changes are additive). */
export const CONTRACT_MAJOR_VERSION = 1;

/**
 * Checks that a server's contract is one this SDK understands: the same contract major version and the same
 * context schema and deep link versions. Returns the problems found (empty when compatible).
 */
export function contractProblems(contract: ContractInfo): string[] {
  const problems: string[] = [];
  const major = Number(String(contract.contractVersion).split(".")[0]);
  if (major !== CONTRACT_MAJOR_VERSION) {
    problems.push(`contract version ${contract.contractVersion} (SDK supports ${CONTRACT_MAJOR_VERSION}.x)`);
  }
  if (contract.contextSchemaVersion !== CONTEXT_SCHEMA_VERSION) {
    problems.push(`context schema version ${contract.contextSchemaVersion} (SDK supports ${CONTEXT_SCHEMA_VERSION})`);
  }
  if (contract.deepLinkVersion !== DEEP_LINK_VERSION) {
    problems.push(`deep link version ${contract.deepLinkVersion} (SDK supports ${DEEP_LINK_VERSION})`);
  }
  return problems;
}
