from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_spatial_region_level_type_1 import (
    PrecursorSpatialRegionLevelType1,
)
from ..models.precursor_spatial_region_level_type_2_type_1 import (
    PrecursorSpatialRegionLevelType2Type1,
)
from ..models.precursor_spatial_region_level_type_3_type_1 import (
    PrecursorSpatialRegionLevelType3Type1,
)
from ..models.precursor_spatial_rule import PrecursorSpatialRule
from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorSpatial")


@_attrs_define
class PrecursorSpatial:
    """Spatial rule linking antecedent and consequent occurrences (103 forms)

    Attributes:
        rule (PrecursorSpatialRule):
        within_km (float | None | Unset):
        region_level (None | PrecursorSpatialRegionLevelType1 | PrecursorSpatialRegionLevelType2Type1 |
            PrecursorSpatialRegionLevelType3Type1 | Unset):
    """

    rule: PrecursorSpatialRule
    within_km: float | None | Unset = UNSET
    region_level: (
        None
        | PrecursorSpatialRegionLevelType1
        | PrecursorSpatialRegionLevelType2Type1
        | PrecursorSpatialRegionLevelType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule = self.rule.value

        within_km: float | None | Unset
        if isinstance(self.within_km, Unset):
            within_km = UNSET
        else:
            within_km = self.within_km

        region_level: None | str | Unset
        if isinstance(self.region_level, Unset):
            region_level = UNSET
        elif (
            isinstance(self.region_level, PrecursorSpatialRegionLevelType1)
            or isinstance(self.region_level, PrecursorSpatialRegionLevelType2Type1)
            or isinstance(self.region_level, PrecursorSpatialRegionLevelType3Type1)
        ):
            region_level = self.region_level.value
        else:
            region_level = self.region_level

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rule": rule,
            }
        )
        if within_km is not UNSET:
            field_dict["withinKm"] = within_km
        if region_level is not UNSET:
            field_dict["regionLevel"] = region_level

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        rule = PrecursorSpatialRule(d.pop("rule"))

        def _parse_within_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        within_km = _parse_within_km(d.pop("withinKm", UNSET))

        def _parse_region_level(
            data: object,
        ) -> (
            None
            | PrecursorSpatialRegionLevelType1
            | PrecursorSpatialRegionLevelType2Type1
            | PrecursorSpatialRegionLevelType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                region_level_type_1 = PrecursorSpatialRegionLevelType1(data)

                return region_level_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                region_level_type_2_type_1 = PrecursorSpatialRegionLevelType2Type1(data)

                return region_level_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                region_level_type_3_type_1 = PrecursorSpatialRegionLevelType3Type1(data)

                return region_level_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | PrecursorSpatialRegionLevelType1
                | PrecursorSpatialRegionLevelType2Type1
                | PrecursorSpatialRegionLevelType3Type1
                | Unset,
                data,
            )

        region_level = _parse_region_level(d.pop("regionLevel", UNSET))

        precursor_spatial = cls(
            rule=rule,
            within_km=within_km,
            region_level=region_level,
        )

        precursor_spatial.additional_properties = d
        return precursor_spatial

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
