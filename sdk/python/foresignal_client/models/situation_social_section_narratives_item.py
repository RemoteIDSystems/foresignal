from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_social_section_narratives_item_found_by import (
    SituationSocialSectionNarrativesItemFoundBy,
)
from ..models.situation_social_section_narratives_item_status import (
    SituationSocialSectionNarrativesItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationSocialSectionNarrativesItem")


@_attrs_define
class SituationSocialSectionNarrativesItem:
    """Top narrative

    Attributes:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        status (SituationSocialSectionNarrativesItemStatus):
        evidence_group_count (int):
        velocity (float | None | Unset):
        origin_concentration (float | Unset):
        lead_over_physical_seconds (float | None | Unset): Observed lead time (single case)
        found_by (SituationSocialSectionNarrativesItemFoundBy | Unset):
    """

    narrative_id: str
    title: str
    status: SituationSocialSectionNarrativesItemStatus
    evidence_group_count: int
    velocity: float | None | Unset = UNSET
    origin_concentration: float | Unset = UNSET
    lead_over_physical_seconds: float | None | Unset = UNSET
    found_by: SituationSocialSectionNarrativesItemFoundBy | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        narrative_id = self.narrative_id

        title = self.title

        status = self.status.value

        evidence_group_count = self.evidence_group_count

        velocity: float | None | Unset
        if isinstance(self.velocity, Unset):
            velocity = UNSET
        else:
            velocity = self.velocity

        origin_concentration = self.origin_concentration

        lead_over_physical_seconds: float | None | Unset
        if isinstance(self.lead_over_physical_seconds, Unset):
            lead_over_physical_seconds = UNSET
        else:
            lead_over_physical_seconds = self.lead_over_physical_seconds

        found_by: str | Unset = UNSET
        if not isinstance(self.found_by, Unset):
            found_by = self.found_by.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narrativeId": narrative_id,
                "title": title,
                "status": status,
                "evidenceGroupCount": evidence_group_count,
            }
        )
        if velocity is not UNSET:
            field_dict["velocity"] = velocity
        if origin_concentration is not UNSET:
            field_dict["originConcentration"] = origin_concentration
        if lead_over_physical_seconds is not UNSET:
            field_dict["leadOverPhysicalSeconds"] = lead_over_physical_seconds
        if found_by is not UNSET:
            field_dict["foundBy"] = found_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        narrative_id = d.pop("narrativeId")

        title = d.pop("title")

        status = SituationSocialSectionNarrativesItemStatus(d.pop("status"))

        evidence_group_count = d.pop("evidenceGroupCount")

        def _parse_velocity(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        velocity = _parse_velocity(d.pop("velocity", UNSET))

        origin_concentration = d.pop("originConcentration", UNSET)

        def _parse_lead_over_physical_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lead_over_physical_seconds = _parse_lead_over_physical_seconds(
            d.pop("leadOverPhysicalSeconds", UNSET)
        )

        _found_by = d.pop("foundBy", UNSET)
        found_by: SituationSocialSectionNarrativesItemFoundBy | Unset
        if isinstance(_found_by, Unset):
            found_by = UNSET
        else:
            found_by = SituationSocialSectionNarrativesItemFoundBy(_found_by)

        situation_social_section_narratives_item = cls(
            narrative_id=narrative_id,
            title=title,
            status=status,
            evidence_group_count=evidence_group_count,
            velocity=velocity,
            origin_concentration=origin_concentration,
            lead_over_physical_seconds=lead_over_physical_seconds,
            found_by=found_by,
        )

        situation_social_section_narratives_item.additional_properties = d
        return situation_social_section_narratives_item

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
