from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RegionalIndexValueComponentsItem")


@_attrs_define
class RegionalIndexValueComponentsItem:
    """Component contribution

    Attributes:
        key (str):
        normalised (float):
        weight (float):
        contribution (float):
        raw (float | None | Unset):
        missing (bool | Unset): True when the component had no value; its weight went to the others
    """

    key: str
    normalised: float
    weight: float
    contribution: float
    raw: float | None | Unset = UNSET
    missing: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        key = self.key

        normalised = self.normalised

        weight = self.weight

        contribution = self.contribution

        raw: float | None | Unset
        if isinstance(self.raw, Unset):
            raw = UNSET
        else:
            raw = self.raw

        missing = self.missing

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "key": key,
                "normalised": normalised,
                "weight": weight,
                "contribution": contribution,
            }
        )
        if raw is not UNSET:
            field_dict["raw"] = raw
        if missing is not UNSET:
            field_dict["missing"] = missing

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        key = d.pop("key")

        normalised = d.pop("normalised")

        weight = d.pop("weight")

        contribution = d.pop("contribution")

        def _parse_raw(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        raw = _parse_raw(d.pop("raw", UNSET))

        missing = d.pop("missing", UNSET)

        regional_index_value_components_item = cls(
            key=key,
            normalised=normalised,
            weight=weight,
            contribution=contribution,
            raw=raw,
            missing=missing,
        )

        regional_index_value_components_item.additional_properties = d
        return regional_index_value_components_item

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
