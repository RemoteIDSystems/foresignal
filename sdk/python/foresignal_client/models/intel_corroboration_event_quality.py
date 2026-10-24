from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelCorroborationEventQuality")


@_attrs_define
class IntelCorroborationEventQuality:
    """The thresholds for an intelligence event's evidence-quality label

    Attributes:
        strong_families (int | Unset): Distinct signal families a strong event needs (default 3)
        strong_groups (int | Unset): Independent evidence groups a strong event needs (default 2)
        moderate_families (int | Unset): Distinct signal families a moderate event needs (default 2)
        require_modalities (int | Unset): Distinct sensing modalities a strong event also needs, or 0 to not require any
            (default 0). A modality is sensor, imagery, official, social, media, crowd, operator or derived, derived from
            the originator class.
    """

    strong_families: int | Unset = UNSET
    strong_groups: int | Unset = UNSET
    moderate_families: int | Unset = UNSET
    require_modalities: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        strong_families = self.strong_families

        strong_groups = self.strong_groups

        moderate_families = self.moderate_families

        require_modalities = self.require_modalities

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if strong_families is not UNSET:
            field_dict["strongFamilies"] = strong_families
        if strong_groups is not UNSET:
            field_dict["strongGroups"] = strong_groups
        if moderate_families is not UNSET:
            field_dict["moderateFamilies"] = moderate_families
        if require_modalities is not UNSET:
            field_dict["requireModalities"] = require_modalities

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        strong_families = d.pop("strongFamilies", UNSET)

        strong_groups = d.pop("strongGroups", UNSET)

        moderate_families = d.pop("moderateFamilies", UNSET)

        require_modalities = d.pop("requireModalities", UNSET)

        intel_corroboration_event_quality = cls(
            strong_families=strong_families,
            strong_groups=strong_groups,
            moderate_families=moderate_families,
            require_modalities=require_modalities,
        )

        intel_corroboration_event_quality.additional_properties = d
        return intel_corroboration_event_quality

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
