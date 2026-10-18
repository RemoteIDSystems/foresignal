from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_suggestion_estimate_layers_item import (
        ChronicleSuggestionEstimateLayersItem,
    )


T = TypeVar("T", bound="ChronicleSuggestionEstimate")


@_attrs_define
class ChronicleSuggestionEstimate:
    """Projected storage of the draft

    Attributes:
        total_rows_per_day (float | Unset):
        total_bytes_per_day (float | Unset):
        layers (list[ChronicleSuggestionEstimateLayersItem] | Unset):
    """

    total_rows_per_day: float | Unset = UNSET
    total_bytes_per_day: float | Unset = UNSET
    layers: list[ChronicleSuggestionEstimateLayersItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        total_rows_per_day = self.total_rows_per_day

        total_bytes_per_day = self.total_bytes_per_day

        layers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.layers, Unset):
            layers = []
            for layers_item_data in self.layers:
                layers_item = layers_item_data.to_dict()
                layers.append(layers_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if total_rows_per_day is not UNSET:
            field_dict["totalRowsPerDay"] = total_rows_per_day
        if total_bytes_per_day is not UNSET:
            field_dict["totalBytesPerDay"] = total_bytes_per_day
        if layers is not UNSET:
            field_dict["layers"] = layers

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_suggestion_estimate_layers_item import (
            ChronicleSuggestionEstimateLayersItem,
        )

        d = dict(src_dict)
        total_rows_per_day = d.pop("totalRowsPerDay", UNSET)

        total_bytes_per_day = d.pop("totalBytesPerDay", UNSET)

        _layers = d.pop("layers", UNSET)
        layers: list[ChronicleSuggestionEstimateLayersItem] | Unset = UNSET
        if _layers is not UNSET:
            layers = []
            for layers_item_data in _layers:
                layers_item = ChronicleSuggestionEstimateLayersItem.from_dict(
                    layers_item_data
                )

                layers.append(layers_item)

        chronicle_suggestion_estimate = cls(
            total_rows_per_day=total_rows_per_day,
            total_bytes_per_day=total_bytes_per_day,
            layers=layers,
        )

        chronicle_suggestion_estimate.additional_properties = d
        return chronicle_suggestion_estimate

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
