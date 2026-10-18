from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.peer_health_reason_type_1 import PeerHealthReasonType1
from ..models.peer_health_reason_type_2_type_1 import PeerHealthReasonType2Type1
from ..models.peer_health_reason_type_3_type_1 import PeerHealthReasonType3Type1
from ..models.peer_mode import PeerMode
from ..models.peer_reliability import PeerReliability
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.peer_selection import PeerSelection


T = TypeVar("T", bound="Peer")


@_attrs_define
class Peer:
    """A peer instance and its replication state (113)

    Attributes:
        id (str):
        name (str):
        base_url (str):
        mode (PeerMode):
        enabled (bool):
        state (str): Runtime state: connecting, streaming, resyncing, failing or stopped for PULL peers; receiving for
            RECEIVE peers
        selection (PeerSelection): Selective replication from a peer; remote filters are passed as query parameters and
            re-checked locally (113)
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        health_reason (None | PeerHealthReasonType1 | PeerHealthReasonType2Type1 | PeerHealthReasonType3Type1 | Unset):
        last_seq (int | None | Unset):
        lag_seq (int | None | Unset):
        last_error (None | str | Unset):
        pinned_kid (None | str | Unset):
        remote_instance_id (None | str | Unset):
        same_operator (bool | Unset):
        reliability (PeerReliability | Unset): Admiralty source reliability: A completely reliable, B usually reliable,
            C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
        token_set (bool | Unset):
        ingest_key_prefix (None | str | Unset): Prefix of the current fsp_ ingest key (RECEIVE peers)
        source_ids (list[str] | Unset): Dynamic sources: peer-{id}, peer-{id}-trk, peer-{id}-events
        exchange_key_set (bool | Unset): An exchange key is stored (132)
    """

    id: str
    name: str
    base_url: str
    mode: PeerMode
    enabled: bool
    state: str
    selection: PeerSelection
    created_at: datetime.datetime
    updated_at: datetime.datetime
    health_reason: (
        None
        | PeerHealthReasonType1
        | PeerHealthReasonType2Type1
        | PeerHealthReasonType3Type1
        | Unset
    ) = UNSET
    last_seq: int | None | Unset = UNSET
    lag_seq: int | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    pinned_kid: None | str | Unset = UNSET
    remote_instance_id: None | str | Unset = UNSET
    same_operator: bool | Unset = UNSET
    reliability: PeerReliability | Unset = UNSET
    token_set: bool | Unset = UNSET
    ingest_key_prefix: None | str | Unset = UNSET
    source_ids: list[str] | Unset = UNSET
    exchange_key_set: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        base_url = self.base_url

        mode = self.mode.value

        enabled = self.enabled

        state = self.state

        selection = self.selection.to_dict()

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        health_reason: None | str | Unset
        if isinstance(self.health_reason, Unset):
            health_reason = UNSET
        elif (
            isinstance(self.health_reason, PeerHealthReasonType1)
            or isinstance(self.health_reason, PeerHealthReasonType2Type1)
            or isinstance(self.health_reason, PeerHealthReasonType3Type1)
        ):
            health_reason = self.health_reason.value
        else:
            health_reason = self.health_reason

        last_seq: int | None | Unset
        if isinstance(self.last_seq, Unset):
            last_seq = UNSET
        else:
            last_seq = self.last_seq

        lag_seq: int | None | Unset
        if isinstance(self.lag_seq, Unset):
            lag_seq = UNSET
        else:
            lag_seq = self.lag_seq

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        pinned_kid: None | str | Unset
        if isinstance(self.pinned_kid, Unset):
            pinned_kid = UNSET
        else:
            pinned_kid = self.pinned_kid

        remote_instance_id: None | str | Unset
        if isinstance(self.remote_instance_id, Unset):
            remote_instance_id = UNSET
        else:
            remote_instance_id = self.remote_instance_id

        same_operator = self.same_operator

        reliability: str | Unset = UNSET
        if not isinstance(self.reliability, Unset):
            reliability = self.reliability.value

        token_set = self.token_set

        ingest_key_prefix: None | str | Unset
        if isinstance(self.ingest_key_prefix, Unset):
            ingest_key_prefix = UNSET
        else:
            ingest_key_prefix = self.ingest_key_prefix

        source_ids: list[str] | Unset = UNSET
        if not isinstance(self.source_ids, Unset):
            source_ids = self.source_ids

        exchange_key_set = self.exchange_key_set

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "baseUrl": base_url,
                "mode": mode,
                "enabled": enabled,
                "state": state,
                "selection": selection,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if health_reason is not UNSET:
            field_dict["healthReason"] = health_reason
        if last_seq is not UNSET:
            field_dict["lastSeq"] = last_seq
        if lag_seq is not UNSET:
            field_dict["lagSeq"] = lag_seq
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if pinned_kid is not UNSET:
            field_dict["pinnedKid"] = pinned_kid
        if remote_instance_id is not UNSET:
            field_dict["remoteInstanceId"] = remote_instance_id
        if same_operator is not UNSET:
            field_dict["sameOperator"] = same_operator
        if reliability is not UNSET:
            field_dict["reliability"] = reliability
        if token_set is not UNSET:
            field_dict["tokenSet"] = token_set
        if ingest_key_prefix is not UNSET:
            field_dict["ingestKeyPrefix"] = ingest_key_prefix
        if source_ids is not UNSET:
            field_dict["sourceIds"] = source_ids
        if exchange_key_set is not UNSET:
            field_dict["exchangeKeySet"] = exchange_key_set

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.peer_selection import PeerSelection

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        base_url = d.pop("baseUrl")

        mode = PeerMode(d.pop("mode"))

        enabled = d.pop("enabled")

        state = d.pop("state")

        selection = PeerSelection.from_dict(d.pop("selection"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        def _parse_health_reason(
            data: object,
        ) -> (
            None
            | PeerHealthReasonType1
            | PeerHealthReasonType2Type1
            | PeerHealthReasonType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                health_reason_type_1 = PeerHealthReasonType1(data)

                return health_reason_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                health_reason_type_2_type_1 = PeerHealthReasonType2Type1(data)

                return health_reason_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                health_reason_type_3_type_1 = PeerHealthReasonType3Type1(data)

                return health_reason_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | PeerHealthReasonType1
                | PeerHealthReasonType2Type1
                | PeerHealthReasonType3Type1
                | Unset,
                data,
            )

        health_reason = _parse_health_reason(d.pop("healthReason", UNSET))

        def _parse_last_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_seq = _parse_last_seq(d.pop("lastSeq", UNSET))

        def _parse_lag_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        lag_seq = _parse_lag_seq(d.pop("lagSeq", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        def _parse_pinned_kid(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        pinned_kid = _parse_pinned_kid(d.pop("pinnedKid", UNSET))

        def _parse_remote_instance_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        remote_instance_id = _parse_remote_instance_id(d.pop("remoteInstanceId", UNSET))

        same_operator = d.pop("sameOperator", UNSET)

        _reliability = d.pop("reliability", UNSET)
        reliability: PeerReliability | Unset
        if isinstance(_reliability, Unset):
            reliability = UNSET
        else:
            reliability = PeerReliability(_reliability)

        token_set = d.pop("tokenSet", UNSET)

        def _parse_ingest_key_prefix(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ingest_key_prefix = _parse_ingest_key_prefix(d.pop("ingestKeyPrefix", UNSET))

        source_ids = cast(list[str], d.pop("sourceIds", UNSET))

        exchange_key_set = d.pop("exchangeKeySet", UNSET)

        peer = cls(
            id=id,
            name=name,
            base_url=base_url,
            mode=mode,
            enabled=enabled,
            state=state,
            selection=selection,
            created_at=created_at,
            updated_at=updated_at,
            health_reason=health_reason,
            last_seq=last_seq,
            lag_seq=lag_seq,
            last_error=last_error,
            pinned_kid=pinned_kid,
            remote_instance_id=remote_instance_id,
            same_operator=same_operator,
            reliability=reliability,
            token_set=token_set,
            ingest_key_prefix=ingest_key_prefix,
            source_ids=source_ids,
            exchange_key_set=exchange_key_set,
        )

        peer.additional_properties = d
        return peer

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
