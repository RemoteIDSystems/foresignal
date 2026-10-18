"""``WorkspaceSync`` keeps a local copy of a workspace context in step with Foresignal (docs/Client-Contract.md
section 5), with the same semantics as the Dart and TypeScript SDKs:

- it reads the workspace, then follows the ``workspace`` topic of ``/v1/events/stream`` (on a background thread)
  with this client's id and kind;
- it applies remote ``workspace.context`` patches in version order and re-reads the workspace on a version gap
  or a reconnect (workspace events are id-less and not replayed);
- it ignores the content of events carrying its own ``originClientId`` (the PATCH response already gave it the
  new state), using them only to advance the version;
- it sends local changes as JSON merge patches with ``If-Match: <contextVersion>`` and ``Foresignal-Client-Id``,
  and on ``412 WORKSPACE_CONTEXT_STALE`` re-reads the workspace, re-applies the intent and retries;
- it exposes presence from ``workspace.presence`` events.
"""
from __future__ import annotations

import copy
import secrets
import threading
from collections.abc import Callable, Mapping
from dataclasses import dataclass, field
from typing import Any
from urllib.parse import quote

from ..errors import ApiError, api_error_from
from ..http import HttpTransport
from ..realtime.backoff import Backoff
from ..realtime.change_stream import ChangeStream
from .context import apply_merge_patch

#: Client kinds accepted by the ``workspace`` topic.
CLIENT_KINDS = ("globe", "map", "analyst", "satellite", "rf", "mobile", "agent", "other")

#: A local change: a merge patch, or a function computing one from the current context (None for no change).
ContextIntent = Mapping[str, Any] | Callable[[dict[str, Any]], Mapping[str, Any] | None]


@dataclass(frozen=True)
class ContextChange:
    """A change of the local context copy; ``origin`` is remote, local or refresh."""

    context: dict[str, Any]
    version: int
    origin: str
    event: dict[str, Any] | None = None


@dataclass
class _State:
    context: dict[str, Any]
    version: int
    pending: dict[int, dict[str, Any]] = field(default_factory=dict)


def generate_client_id() -> str:
    """A client id: ``c-`` and 16 random hex characters."""
    return "c-" + secrets.token_hex(8)


class WorkspaceSync:
    """Keeps one workspace context synchronized."""

    def __init__(self, base_url: str | None = None, token: str | None = None, *, workspace_id: str,
                 client_id: str | None = None, client_kind: str = "other", max_attempts: int = 5,
                 heartbeat_interval: float = 15.0, backoff: Backoff | None = None, use_ticket: bool = False,
                 transport: HttpTransport | None = None, headers: Mapping[str, str] | None = None,
                 sleep: Callable[[float], None] | None = None) -> None:
        """Creates the helper; call :meth:`start` to follow the workspace.

        :param workspace_id: the workspace (a ULID)
        :param client_id: this client's id (generated when absent): presence and ``originClientId``
        :param client_kind: one of :data:`CLIENT_KINDS`
        :param max_attempts: attempts per local change when the version is stale
        """
        if client_kind not in CLIENT_KINDS:
            raise ValueError(f"client_kind must be one of {', '.join(CLIENT_KINDS)}")
        if transport is None:
            if base_url is None:
                raise ValueError("pass base_url or transport")
            transport = HttpTransport(base_url, token, headers=headers)
        self.transport = transport
        self.workspace_id = workspace_id
        self.client_id = client_id or generate_client_id()
        self.client_kind = client_kind
        self.max_attempts = max(1, max_attempts)
        self._heartbeat_interval = heartbeat_interval
        self._backoff = backoff
        self._use_ticket = use_ticket
        self._sleep = sleep
        self._lock = threading.RLock()
        self._state: _State | None = None
        self._presence: list[dict[str, Any]] = []
        self._context_listeners: list[Callable[[ContextChange], None]] = []
        self._presence_listeners: list[Callable[[dict[str, Any]], None]] = []
        self._error_listeners: list[Callable[[BaseException], None]] = []
        self._stream: ChangeStream | None = None
        self._thread: threading.Thread | None = None

    # -- state --------------------------------------------------------------------------------------------

    @property
    def context(self) -> dict[str, Any] | None:
        """The current context (a copy)."""
        with self._lock:
            return copy.deepcopy(self._state.context) if self._state else None

    @property
    def version(self) -> int | None:
        """The current context version."""
        with self._lock:
            return self._state.version if self._state else None

    @property
    def presence(self) -> list[dict[str, Any]]:
        """The clients connected to the workspace, from the latest presence event."""
        with self._lock:
            return list(self._presence)

    def on_context(self, listener: Callable[[ContextChange], None]) -> Callable[[], None]:
        """Subscribes to context changes; returns the unsubscribe function."""
        self._context_listeners.append(listener)
        return lambda: self._context_listeners.remove(listener)

    def on_presence(self, listener: Callable[[dict[str, Any]], None]) -> Callable[[], None]:
        """Subscribes to presence events; returns the unsubscribe function."""
        self._presence_listeners.append(listener)
        return lambda: self._presence_listeners.remove(listener)

    def on_error(self, listener: Callable[[BaseException], None]) -> Callable[[], None]:
        """Subscribes to background errors; returns the unsubscribe function."""
        self._error_listeners.append(listener)
        return lambda: self._error_listeners.remove(listener)

    # -- lifecycle ----------------------------------------------------------------------------------------

    def start(self) -> None:
        """Reads the workspace and starts following its events on a daemon thread."""
        if self._stream is not None:
            return
        self.refresh()
        self._stream = ChangeStream(
            endpoint="events", transport=self.transport, snapshot=False, use_ticket=self._use_ticket,
            heartbeat_interval=self._heartbeat_interval, backoff=self._backoff, sleep=self._sleep,
            filters={"topics": "workspace", "workspace.workspaceId": self.workspace_id,
                     "workspace.clientId": self.client_id, "workspace.clientKind": self.client_kind})
        self._thread = threading.Thread(target=self._follow, args=(self._stream,), name="foresignal-workspace-sync",
                                        daemon=True)
        self._thread.start()

    def stop(self, timeout: float = 5.0) -> None:
        """Stops following the workspace."""
        if self._stream is not None:
            self._stream.close()
        if self._thread is not None:
            self._thread.join(timeout)
        self._stream = None
        self._thread = None

    def refresh(self) -> None:
        """Re-reads the workspace (``GET /v1/workspaces/{id}``) and replaces the local copy."""
        data, _ = self.transport.get_json(f"/v1/workspaces/{quote(self.workspace_id, safe='')}")
        data = data or {}
        self._accept(data.get("context") or {}, int(data.get("contextVersion", 0)), "refresh")

    def update(self, intent: ContextIntent) -> dict[str, Any]:
        """Sends a local change; a callable intent is re-evaluated against the fresh context after a 412.

        :returns: the new ``WorkspaceContextState`` (or the current one when the intent produced no patch)
        :raises ApiError: when the server refuses the change, or still answers 412 after ``max_attempts``
        """
        if self._state is None:
            self.refresh()
        last: ApiError | None = None
        for _ in range(self.max_attempts):
            with self._lock:
                assert self._state is not None
                current_context = copy.deepcopy(self._state.context)
                current_version = self._state.version
            patch = intent(current_context) if callable(intent) else intent
            if patch is None:
                return {"workspaceId": self.workspace_id, "contextVersion": current_version,
                        "context": current_context}
            response = self.transport.send(
                "PATCH", f"/v1/workspaces/{quote(self.workspace_id, safe='')}/context", json=dict(patch),
                content_type="application/merge-patch+json",
                headers={"Accept": "application/json", "If-Match": str(current_version),
                         "Foresignal-Client-Id": self.client_id})
            if 200 <= response.status_code < 300:
                state = response.json()
                self._accept(state.get("context") or {}, int(state["contextVersion"]), "local")
                return state
            error = api_error_from(response)
            if error.status != 412:
                raise error
            last = error
            self.refresh()
        assert last is not None
        raise last

    # -- events -------------------------------------------------------------------------------------------

    def handle_context_event(self, event: Mapping[str, Any]) -> None:
        """Handles a ``workspace.context`` event (called by the follow thread; public for custom transports)."""
        gap = False
        with self._lock:
            if not isinstance(event, Mapping) or event.get("workspaceId") != self.workspace_id or not self._state:
                return
            version = event.get("version")
            if not isinstance(version, int) or version <= self._state.version:
                return
            self._state.pending[version] = dict(event)
            self._drain()
            gap = bool(self._state.pending)
        if gap:
            self.refresh()

    def handle_presence_event(self, event: Mapping[str, Any]) -> None:
        """Handles a ``workspace.presence`` event."""
        if not isinstance(event, Mapping) or event.get("workspaceId") != self.workspace_id:
            return
        with self._lock:
            self._presence = list(event.get("clients") or [])
        for listener in list(self._presence_listeners):
            listener(dict(event))

    def _follow(self, stream: ChangeStream) -> None:
        opened = 0
        try:
            for signal in stream:
                try:
                    if signal.type == "open":
                        opened += 1
                        if opened > 1:
                            self.refresh()
                    elif signal.type == "event" and signal.event == "workspace.context":
                        self.handle_context_event(signal.data)
                    elif signal.type == "event" and signal.event == "workspace.presence":
                        self.handle_presence_event(signal.data)
                except Exception as error:  # noqa: BLE001 - reported to listeners, the stream goes on
                    self._emit_error(error)
        except Exception as error:  # noqa: BLE001 - the stream failed for good
            if not stream.closed:
                self._emit_error(error)

    def _drain(self) -> None:
        state = self._state
        if state is None:
            return
        for version in [v for v in state.pending if v <= state.version]:
            del state.pending[version]
        while state.version + 1 in state.pending:
            event = state.pending.pop(state.version + 1)
            state.version = event["version"]
            if event.get("originClientId") == self.client_id:
                continue  # our own change: the PATCH response carries the state
            state.context = apply_merge_patch(state.context, event.get("patch") or {})
            self._emit_context(ContextChange(copy.deepcopy(state.context), state.version, "remote", event))

    def _accept(self, context: dict[str, Any], version: int, origin: str) -> None:
        with self._lock:
            if self._state is not None and version < self._state.version:
                return
            pending = self._state.pending if self._state is not None else {}
            self._state = _State(copy.deepcopy(context), version, pending)
            self._drain()
            change = ContextChange(copy.deepcopy(self._state.context), self._state.version, origin)
        self._emit_context(change)

    def _emit_context(self, change: ContextChange) -> None:
        for listener in list(self._context_listeners):
            listener(change)

    def _emit_error(self, error: BaseException) -> None:
        for listener in list(self._error_listeners):
            listener(error)
