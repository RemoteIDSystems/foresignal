from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationChangesArea")


@_attrs_define
class SituationChangesArea:
    """Area of the situation geometry

    Attributes:
        before_km_2 (float | None | Unset):
        after_km_2 (float | None | Unset):
        delta_km_2 (float | None | Unset):
    """

    before_km_2: float | None | Unset = UNSET
    after_km_2: float | None | Unset = UNSET
    delta_km_2: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        before_km_2: float | None | Unset
        if isinstance(self.before_km_2, Unset):
            before_km_2 = UNSET
        else:
            before_km_2 = self.before_km_2

        after_km_2: float | None | Unset
        if isinstance(self.after_km_2, Unset):
            after_km_2 = UNSET
        else:
            after_km_2 = self.after_km_2

        delta_km_2: float | None | Unset
        if isinstance(self.delta_km_2, Unset):
            delta_km_2 = UNSET
        else:
            delta_km_2 = self.delta_km_2

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if before_km_2 is not UNSET:
            field_dict["beforeKm2"] = before_km_2
        if after_km_2 is not UNSET:
            field_dict["afterKm2"] = after_km_2
        if delta_km_2 is not UNSET:
            field_dict["deltaKm2"] = delta_km_2

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_before_km_2(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        before_km_2 = _parse_before_km_2(d.pop("beforeKm2", UNSET))

        def _parse_after_km_2(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        after_km_2 = _parse_after_km_2(d.pop("afterKm2", UNSET))

        def _parse_delta_km_2(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        delta_km_2 = _parse_delta_km_2(d.pop("deltaKm2", UNSET))

        situation_changes_area = cls(
            before_km_2=before_km_2,
            after_km_2=after_km_2,
            delta_km_2=delta_km_2,
        )

        situation_changes_area.additional_properties = d
        return situation_changes_area

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
