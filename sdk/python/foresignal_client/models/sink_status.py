from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sink_status_state import SinkStatusState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_status_clients_type_0 import SinkStatusClientsType0
    from ..models.sink_status_denied import SinkStatusDenied


T = TypeVar("T", bound="SinkStatus")


@_attrs_define
class SinkStatus:
    """Cursor, lag and delivery counters of a sink (110)

    Attributes:
        sink_id (str):
        state (SinkStatusState):
        delivered (int):
        dropped (int): Updates conflated by rate control
        denied (SinkStatusDenied): Records refused by the egress gate
        last_seq (int | None | Unset):
        watermark_seq (int | None | Unset):
        last_published_seq (int | None | Unset):
        lag_seq (int | None | Unset): lastPublishedSeq − lastSeq
        last_delivered_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
        failing_since (datetime.datetime | None | Unset):
        resync_count (int | Unset):
        clients (None | SinkStatusClientsType0 | Unset):
    """

    sink_id: str
    state: SinkStatusState
    delivered: int
    dropped: int
    denied: SinkStatusDenied
    last_seq: int | None | Unset = UNSET
    watermark_seq: int | None | Unset = UNSET
    last_published_seq: int | None | Unset = UNSET
    lag_seq: int | None | Unset = UNSET
    last_delivered_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    failing_since: datetime.datetime | None | Unset = UNSET
    resync_count: int | Unset = UNSET
    clients: None | SinkStatusClientsType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.sink_status_clients_type_0 import SinkStatusClientsType0

        sink_id = self.sink_id

        state = self.state.value

        delivered = self.delivered

        dropped = self.dropped

        denied = self.denied.to_dict()

        last_seq: int | None | Unset
        if isinstance(self.last_seq, Unset):
            last_seq = UNSET
        else:
            last_seq = self.last_seq

        watermark_seq: int | None | Unset
        if isinstance(self.watermark_seq, Unset):
            watermark_seq = UNSET
        else:
            watermark_seq = self.watermark_seq

        last_published_seq: int | None | Unset
        if isinstance(self.last_published_seq, Unset):
            last_published_seq = UNSET
        else:
            last_published_seq = self.last_published_seq

        lag_seq: int | None | Unset
        if isinstance(self.lag_seq, Unset):
            lag_seq = UNSET
        else:
            lag_seq = self.lag_seq

        last_delivered_at: None | str | Unset
        if isinstance(self.last_delivered_at, Unset):
            last_delivered_at = UNSET
        elif isinstance(self.last_delivered_at, datetime.datetime):
            last_delivered_at = self.last_delivered_at.isoformat()
        else:
            last_delivered_at = self.last_delivered_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        failing_since: None | str | Unset
        if isinstance(self.failing_since, Unset):
            failing_since = UNSET
        elif isinstance(self.failing_since, datetime.datetime):
            failing_since = self.failing_since.isoformat()
        else:
            failing_since = self.failing_since

        resync_count = self.resync_count

        clients: dict[str, Any] | None | Unset
        if isinstance(self.clients, Unset):
            clients = UNSET
        elif isinstance(self.clients, SinkStatusClientsType0):
            clients = self.clients.to_dict()
        else:
            clients = self.clients

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sinkId": sink_id,
                "state": state,
                "delivered": delivered,
                "dropped": dropped,
                "denied": denied,
            }
        )
        if last_seq is not UNSET:
            field_dict["lastSeq"] = last_seq
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq
        if last_published_seq is not UNSET:
            field_dict["lastPublishedSeq"] = last_published_seq
        if lag_seq is not UNSET:
            field_dict["lagSeq"] = lag_seq
        if last_delivered_at is not UNSET:
            field_dict["lastDeliveredAt"] = last_delivered_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if failing_since is not UNSET:
            field_dict["failingSince"] = failing_since
        if resync_count is not UNSET:
            field_dict["resyncCount"] = resync_count
        if clients is not UNSET:
            field_dict["clients"] = clients

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_status_clients_type_0 import SinkStatusClientsType0
        from ..models.sink_status_denied import SinkStatusDenied

        d = dict(src_dict)
        sink_id = d.pop("sinkId")

        state = SinkStatusState(d.pop("state"))

        delivered = d.pop("delivered")

        dropped = d.pop("dropped")

        denied = SinkStatusDenied.from_dict(d.pop("denied"))

        def _parse_last_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_seq = _parse_last_seq(d.pop("lastSeq", UNSET))

        def _parse_watermark_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        watermark_seq = _parse_watermark_seq(d.pop("watermarkSeq", UNSET))

        def _parse_last_published_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_published_seq = _parse_last_published_seq(d.pop("lastPublishedSeq", UNSET))

        def _parse_lag_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        lag_seq = _parse_lag_seq(d.pop("lagSeq", UNSET))

        def _parse_last_delivered_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_delivered_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_delivered_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_delivered_at = _parse_last_delivered_at(d.pop("lastDeliveredAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        def _parse_failing_since(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                failing_since_type_0 = datetime.datetime.fromisoformat(data)

                return failing_since_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        failing_since = _parse_failing_since(d.pop("failingSince", UNSET))

        resync_count = d.pop("resyncCount", UNSET)

        def _parse_clients(data: object) -> None | SinkStatusClientsType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                clients_type_0 = SinkStatusClientsType0.from_dict(data)

                return clients_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SinkStatusClientsType0 | Unset, data)

        clients = _parse_clients(d.pop("clients", UNSET))

        sink_status = cls(
            sink_id=sink_id,
            state=state,
            delivered=delivered,
            dropped=dropped,
            denied=denied,
            last_seq=last_seq,
            watermark_seq=watermark_seq,
            last_published_seq=last_published_seq,
            lag_seq=lag_seq,
            last_delivered_at=last_delivered_at,
            last_error=last_error,
            failing_since=failing_since,
            resync_count=resync_count,
            clients=clients,
        )

        sink_status.additional_properties = d
        return sink_status

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
