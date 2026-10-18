from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_event_member_input_member_type import (
    IntelEventMemberInputMemberType,
)
from ..models.intel_event_member_input_role import IntelEventMemberInputRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelEventMemberInput")


@_attrs_define
class IntelEventMemberInput:
    """A member to add to an intelligence event; the referenced item must exist (records are verified by seq)

    Attributes:
        member_type (IntelEventMemberInputMemberType):
        role (IntelEventMemberInputRole | Unset):  Default: IntelEventMemberInputRole.SUPPORTING.
        source_id (str | Unset):
        external_id (str | Unset):
        seq (int | Unset): Required for records
        alert_id (int | Unset):
        anomaly_id (int | Unset):
        event_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        signal_family (str | Unset): Ignored: the server derives a member's signal family from the recipe registry
            (invariant INT-3). Accepted for compatibility only. Example: ais-behaviour.
        weight (float | Unset): Ignored: the server derives a member's weight from the recipe registry (invariant
            INT-3). Accepted for compatibility only.
    """

    member_type: IntelEventMemberInputMemberType
    role: IntelEventMemberInputRole | Unset = IntelEventMemberInputRole.SUPPORTING
    source_id: str | Unset = UNSET
    external_id: str | Unset = UNSET
    seq: int | Unset = UNSET
    alert_id: int | Unset = UNSET
    anomaly_id: int | Unset = UNSET
    event_id: str | Unset = UNSET
    signal_family: str | Unset = UNSET
    weight: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        member_type = self.member_type.value

        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        source_id = self.source_id

        external_id = self.external_id

        seq = self.seq

        alert_id = self.alert_id

        anomaly_id = self.anomaly_id

        event_id = self.event_id

        signal_family = self.signal_family

        weight = self.weight

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "memberType": member_type,
            }
        )
        if role is not UNSET:
            field_dict["role"] = role
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if seq is not UNSET:
            field_dict["seq"] = seq
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if anomaly_id is not UNSET:
            field_dict["anomalyId"] = anomaly_id
        if event_id is not UNSET:
            field_dict["eventId"] = event_id
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if weight is not UNSET:
            field_dict["weight"] = weight

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        member_type = IntelEventMemberInputMemberType(d.pop("memberType"))

        _role = d.pop("role", UNSET)
        role: IntelEventMemberInputRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = IntelEventMemberInputRole(_role)

        source_id = d.pop("sourceId", UNSET)

        external_id = d.pop("externalId", UNSET)

        seq = d.pop("seq", UNSET)

        alert_id = d.pop("alertId", UNSET)

        anomaly_id = d.pop("anomalyId", UNSET)

        event_id = d.pop("eventId", UNSET)

        signal_family = d.pop("signalFamily", UNSET)

        weight = d.pop("weight", UNSET)

        intel_event_member_input = cls(
            member_type=member_type,
            role=role,
            source_id=source_id,
            external_id=external_id,
            seq=seq,
            alert_id=alert_id,
            anomaly_id=anomaly_id,
            event_id=event_id,
            signal_family=signal_family,
            weight=weight,
        )

        intel_event_member_input.additional_properties = d
        return intel_event_member_input

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
