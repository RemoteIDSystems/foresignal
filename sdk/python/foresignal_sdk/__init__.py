"""Foresignal Python SDK, hand-written layer (prompt 141).

``foresignal_sdk`` holds the realtime (``ChangeStream``), paging (cursor generators) and client-contract
(``ObjectRef``, envelopes, ``resolve_objects``, ``WorkspaceSync``, deep links) helpers on plain httpx. The
generated client for every operation is the sibling package ``foresignal_client``.
"""
from .errors import (
    ApiError,
    ContextValidationError,
    DeepLinkError,
    HeartbeatTimeout,
    ObjectRefError,
    ResyncRequiredError,
)
from .http import HttpTransport

__version__ = "1.4.0"

__all__ = ["ApiError", "ContextValidationError", "DeepLinkError", "HeartbeatTimeout", "ObjectRefError",
           "ResyncRequiredError", "HttpTransport", "__version__"]
