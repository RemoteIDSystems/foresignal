from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_summary_layers_item_series_item import (
        ChronicleSummaryLayersItemSeriesItem,
    )


T = TypeVar("T", bound="ChronicleSummaryLayersItem")


@_attrs_define
class ChronicleSummaryLayersItem:
    """Layer counts over time

    Attributes:
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        rows (int):
        series (list[ChronicleSummaryLayersItemSeriesItem] | Unset):
    """

    layer_id: str
    name: str
    rows: int
    series: list[ChronicleSummaryLayersItemSeriesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        layer_id = self.layer_id

        name = self.name

        rows = self.rows

        series: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.series, Unset):
            series = []
            for series_item_data in self.series:
                series_item = series_item_data.to_dict()
                series.append(series_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "layerId": layer_id,
                "name": name,
                "rows": rows,
            }
        )
        if series is not UNSET:
            field_dict["series"] = series

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_summary_layers_item_series_item import (
            ChronicleSummaryLayersItemSeriesItem,
        )

        d = dict(src_dict)
        layer_id = d.pop("layerId")

        name = d.pop("name")

        rows = d.pop("rows")

        _series = d.pop("series", UNSET)
        series: list[ChronicleSummaryLayersItemSeriesItem] | Unset = UNSET
        if _series is not UNSET:
            series = []
            for series_item_data in _series:
                series_item = ChronicleSummaryLayersItemSeriesItem.from_dict(
                    series_item_data
                )

                series.append(series_item)

        chronicle_summary_layers_item = cls(
            layer_id=layer_id,
            name=name,
            rows=rows,
            series=series,
        )

        chronicle_summary_layers_item.additional_properties = d
        return chronicle_summary_layers_item

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
