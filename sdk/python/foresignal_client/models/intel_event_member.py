from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_event_member_member_type import IntelEventMemberMemberType
from ..models.intel_event_member_role import IntelEventMemberRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_event_member_join_reason import IntelEventMemberJoinReason
    from ..models.intel_event_member_snapshot_type_0 import (
        IntelEventMemberSnapshotType0,
    )


T = TypeVar("T", bound="IntelEventMember")


@_attrs_define
class IntelEventMember:
    """An evidence member of an intelligence event, pinned to an exact version with the join reason that admitted it

    Attributes:
        id (str): Member id
        role (IntelEventMemberRole):
        member_type (IntelEventMemberMemberType):
        added_at (datetime.datetime):
        added_by (str): Token id or `recipe:<id>`
        source_id (None | str | Unset):
        external_id (None | str | Unset):
        seq (int | None | Unset): Pinned version seq
        version (int | None | Unset):
        alert_id (int | None | Unset):
        anomaly_id (int | None | Unset):
        event_id (None | str | Unset):
        kind (None | str | Unset):
        observed_at (datetime.datetime | None | Unset):
        join_reason (IntelEventMemberJoinReason | Unset): Why the member joined
        signal_family (str | Unset): Signal family from the shared registry (`intel/signal-families.json`, 119), e.g.
            `ais-behaviour`, `nav-warning`, `internet-measurement` Example: ais-behaviour.
        weight (float | None | Unset):
        snapshot (IntelEventMemberSnapshotType0 | None | Unset): Payload snapshot (≤ 16 KB) so the evidence survives
            retention
    """

    id: str
    role: IntelEventMemberRole
    member_type: IntelEventMemberMemberType
    added_at: datetime.datetime
    added_by: str
    source_id: None | str | Unset = UNSET
    external_id: None | str | Unset = UNSET
    seq: int | None | Unset = UNSET
    version: int | None | Unset = UNSET
    alert_id: int | None | Unset = UNSET
    anomaly_id: int | None | Unset = UNSET
    event_id: None | str | Unset = UNSET
    kind: None | str | Unset = UNSET
    observed_at: datetime.datetime | None | Unset = UNSET
    join_reason: IntelEventMemberJoinReason | Unset = UNSET
    signal_family: str | Unset = UNSET
    weight: float | None | Unset = UNSET
    snapshot: IntelEventMemberSnapshotType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.intel_event_member_snapshot_type_0 import (
            IntelEventMemberSnapshotType0,
        )

        id = self.id

        role = self.role.value

        member_type = self.member_type.value

        added_at = self.added_at.isoformat()

        added_by = self.added_by

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        external_id: None | str | Unset
        if isinstance(self.external_id, Unset):
            external_id = UNSET
        else:
            external_id = self.external_id

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        version: int | None | Unset
        if isinstance(self.version, Unset):
            version = UNSET
        else:
            version = self.version

        alert_id: int | None | Unset
        if isinstance(self.alert_id, Unset):
            alert_id = UNSET
        else:
            alert_id = self.alert_id

        anomaly_id: int | None | Unset
        if isinstance(self.anomaly_id, Unset):
            anomaly_id = UNSET
        else:
            anomaly_id = self.anomaly_id

        event_id: None | str | Unset
        if isinstance(self.event_id, Unset):
            event_id = UNSET
        else:
            event_id = self.event_id

        kind: None | str | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        else:
            kind = self.kind

        observed_at: None | str | Unset
        if isinstance(self.observed_at, Unset):
            observed_at = UNSET
        elif isinstance(self.observed_at, datetime.datetime):
            observed_at = self.observed_at.isoformat()
        else:
            observed_at = self.observed_at

        join_reason: dict[str, Any] | Unset = UNSET
        if not isinstance(self.join_reason, Unset):
            join_reason = self.join_reason.to_dict()

        signal_family = self.signal_family

        weight: float | None | Unset
        if isinstance(self.weight, Unset):
            weight = UNSET
        else:
            weight = self.weight

        snapshot: dict[str, Any] | None | Unset
        if isinstance(self.snapshot, Unset):
            snapshot = UNSET
        elif isinstance(self.snapshot, IntelEventMemberSnapshotType0):
            snapshot = self.snapshot.to_dict()
        else:
            snapshot = self.snapshot

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "role": role,
                "memberType": member_type,
                "addedAt": added_at,
                "addedBy": added_by,
            }
        )
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if seq is not UNSET:
            field_dict["seq"] = seq
        if version is not UNSET:
            field_dict["version"] = version
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if anomaly_id is not UNSET:
            field_dict["anomalyId"] = anomaly_id
        if event_id is not UNSET:
            field_dict["eventId"] = event_id
        if kind is not UNSET:
            field_dict["kind"] = kind
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if join_reason is not UNSET:
            field_dict["joinReason"] = join_reason
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if weight is not UNSET:
            field_dict["weight"] = weight
        if snapshot is not UNSET:
            field_dict["snapshot"] = snapshot

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_event_member_join_reason import IntelEventMemberJoinReason
        from ..models.intel_event_member_snapshot_type_0 import (
            IntelEventMemberSnapshotType0,
        )

        d = dict(src_dict)
        id = d.pop("id")

        role = IntelEventMemberRole(d.pop("role"))

        member_type = IntelEventMemberMemberType(d.pop("memberType"))

        added_at = datetime.datetime.fromisoformat(d.pop("addedAt"))

        added_by = d.pop("addedBy")

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        def _parse_external_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        external_id = _parse_external_id(d.pop("externalId", UNSET))

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_version(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        version = _parse_version(d.pop("version", UNSET))

        def _parse_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        alert_id = _parse_alert_id(d.pop("alertId", UNSET))

        def _parse_anomaly_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        anomaly_id = _parse_anomaly_id(d.pop("anomalyId", UNSET))

        def _parse_event_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        event_id = _parse_event_id(d.pop("eventId", UNSET))

        def _parse_kind(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind = _parse_kind(d.pop("kind", UNSET))

        def _parse_observed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                observed_at_type_0 = datetime.datetime.fromisoformat(data)

                return observed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        observed_at = _parse_observed_at(d.pop("observedAt", UNSET))

        _join_reason = d.pop("joinReason", UNSET)
        join_reason: IntelEventMemberJoinReason | Unset
        if isinstance(_join_reason, Unset):
            join_reason = UNSET
        else:
            join_reason = IntelEventMemberJoinReason.from_dict(_join_reason)

        signal_family = d.pop("signalFamily", UNSET)

        def _parse_weight(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        weight = _parse_weight(d.pop("weight", UNSET))

        def _parse_snapshot(
            data: object,
        ) -> IntelEventMemberSnapshotType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                snapshot_type_0 = IntelEventMemberSnapshotType0.from_dict(data)

                return snapshot_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IntelEventMemberSnapshotType0 | None | Unset, data)

        snapshot = _parse_snapshot(d.pop("snapshot", UNSET))

        intel_event_member = cls(
            id=id,
            role=role,
            member_type=member_type,
            added_at=added_at,
            added_by=added_by,
            source_id=source_id,
            external_id=external_id,
            seq=seq,
            version=version,
            alert_id=alert_id,
            anomaly_id=anomaly_id,
            event_id=event_id,
            kind=kind,
            observed_at=observed_at,
            join_reason=join_reason,
            signal_family=signal_family,
            weight=weight,
            snapshot=snapshot,
        )

        intel_event_member.additional_properties = d
        return intel_event_member

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
