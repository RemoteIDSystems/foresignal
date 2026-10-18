from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleSuggestionEstimateLayersItem")


@_attrs_define
class ChronicleSuggestionEstimateLayersItem:
    """Per-layer projection

    Attributes:
        name (str):
        rows_per_day (float | Unset):
        bytes_per_day (float | Unset):
    """

    name: str
    rows_per_day: float | Unset = UNSET
    bytes_per_day: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        rows_per_day = self.rows_per_day

        bytes_per_day = self.bytes_per_day

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
            }
        )
        if rows_per_day is not UNSET:
            field_dict["rowsPerDay"] = rows_per_day
        if bytes_per_day is not UNSET:
            field_dict["bytesPerDay"] = bytes_per_day

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        rows_per_day = d.pop("rowsPerDay", UNSET)

        bytes_per_day = d.pop("bytesPerDay", UNSET)

        chronicle_suggestion_estimate_layers_item = cls(
            name=name,
            rows_per_day=rows_per_day,
            bytes_per_day=bytes_per_day,
        )

        chronicle_suggestion_estimate_layers_item.additional_properties = d
        return chronicle_suggestion_estimate_layers_item

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
