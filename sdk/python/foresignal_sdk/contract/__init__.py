"""Client-contract helpers (docs/Client-Contract.md): object references, envelopes, resolve batching, workspace
contexts, deep links and ``WorkspaceSync``."""
from .context import apply_merge_patch, context_problems, strip_null_members, validate_context
from .deep_link import DecodedDeepLink, EncodedDeepLink, decode_deep_link, encode_deep_link
from .envelope import (
    CONTEXT_SCHEMA_VERSION,
    DEEP_LINK_VERSION,
    ContractInfo,
    ObjectEnvelope,
    ResolveResult,
    contract_problems,
)
from .object_ref import (
    ObjectRef,
    RecordRefParts,
    decode_external_id,
    encode_external_id,
    format_object_ref,
    is_valid_object_ref,
    object_ref_path_segment,
    parse_object_ref,
    record_ref,
    remote_ref,
)
from .resolve import MAX_RESOLVE_BATCH, batch_refs, resolve_objects
from .workspace_sync import ContextChange, WorkspaceSync

__all__ = [
    "apply_merge_patch", "context_problems", "strip_null_members", "validate_context", "DecodedDeepLink",
    "EncodedDeepLink", "decode_deep_link", "encode_deep_link", "CONTEXT_SCHEMA_VERSION", "DEEP_LINK_VERSION",
    "ContractInfo", "ObjectEnvelope", "ResolveResult", "contract_problems", "ObjectRef", "RecordRefParts",
    "decode_external_id", "encode_external_id", "format_object_ref", "is_valid_object_ref",
    "object_ref_path_segment", "parse_object_ref", "record_ref", "remote_ref", "MAX_RESOLVE_BATCH", "batch_refs",
    "resolve_objects", "ContextChange", "WorkspaceSync",
]
