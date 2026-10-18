from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSettingsCuesTriggers")


@_attrs_define
class SocialSettingsCuesTriggers:
    """Physical triggers

    Attributes:
        situation_activated (bool | Unset):  Default: True.
        situation_escalated (bool | Unset):  Default: True.
        finding_types (list[str] | Unset): `significant` finding types, e.g. change points in outage, vessel, flight and
            fire series
        anomalies (bool | Unset):  Default: True.
        critical_alerts (bool | Unset):  Default: True.
        canonical_families (list[str] | Unset): Canonical-event families with ≥ minEvidenceGroups
        min_evidence_groups (int | Unset):  Default: 2.
    """

    situation_activated: bool | Unset = True
    situation_escalated: bool | Unset = True
    finding_types: list[str] | Unset = UNSET
    anomalies: bool | Unset = True
    critical_alerts: bool | Unset = True
    canonical_families: list[str] | Unset = UNSET
    min_evidence_groups: int | Unset = 2
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situation_activated = self.situation_activated

        situation_escalated = self.situation_escalated

        finding_types: list[str] | Unset = UNSET
        if not isinstance(self.finding_types, Unset):
            finding_types = self.finding_types

        anomalies = self.anomalies

        critical_alerts = self.critical_alerts

        canonical_families: list[str] | Unset = UNSET
        if not isinstance(self.canonical_families, Unset):
            canonical_families = self.canonical_families

        min_evidence_groups = self.min_evidence_groups

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if situation_activated is not UNSET:
            field_dict["situationActivated"] = situation_activated
        if situation_escalated is not UNSET:
            field_dict["situationEscalated"] = situation_escalated
        if finding_types is not UNSET:
            field_dict["findingTypes"] = finding_types
        if anomalies is not UNSET:
            field_dict["anomalies"] = anomalies
        if critical_alerts is not UNSET:
            field_dict["criticalAlerts"] = critical_alerts
        if canonical_families is not UNSET:
            field_dict["canonicalFamilies"] = canonical_families
        if min_evidence_groups is not UNSET:
            field_dict["minEvidenceGroups"] = min_evidence_groups

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        situation_activated = d.pop("situationActivated", UNSET)

        situation_escalated = d.pop("situationEscalated", UNSET)

        finding_types = cast(list[str], d.pop("findingTypes", UNSET))

        anomalies = d.pop("anomalies", UNSET)

        critical_alerts = d.pop("criticalAlerts", UNSET)

        canonical_families = cast(list[str], d.pop("canonicalFamilies", UNSET))

        min_evidence_groups = d.pop("minEvidenceGroups", UNSET)

        social_settings_cues_triggers = cls(
            situation_activated=situation_activated,
            situation_escalated=situation_escalated,
            finding_types=finding_types,
            anomalies=anomalies,
            critical_alerts=critical_alerts,
            canonical_families=canonical_families,
            min_evidence_groups=min_evidence_groups,
        )

        social_settings_cues_triggers.additional_properties = d
        return social_settings_cues_triggers

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
