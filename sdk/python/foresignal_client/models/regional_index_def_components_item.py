from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.regional_index_def_components_item_direction import (
    RegionalIndexDefComponentsItemDirection,
)
from ..models.regional_index_def_components_item_transform import (
    RegionalIndexDefComponentsItemTransform,
)
from ..models.regional_index_def_components_item_window import (
    RegionalIndexDefComponentsItemWindow,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="RegionalIndexDefComponentsItem")


@_attrs_define
class RegionalIndexDefComponentsItem:
    """Index component

    Attributes:
        key (str):
        ref (str): SeriesRef template with `{region}` Example: rollup:conflict/{region}.events.
        transform (RegionalIndexDefComponentsItemTransform):
        window (RegionalIndexDefComponentsItemWindow):
        direction (RegionalIndexDefComponentsItemDirection):
        floor (float | None | Unset):
    """

    key: str
    ref: str
    transform: RegionalIndexDefComponentsItemTransform
    window: RegionalIndexDefComponentsItemWindow
    direction: RegionalIndexDefComponentsItemDirection
    floor: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        key = self.key

        ref = self.ref

        transform = self.transform.value

        window = self.window.value

        direction = self.direction.value

        floor: float | None | Unset
        if isinstance(self.floor, Unset):
            floor = UNSET
        else:
            floor = self.floor

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "key": key,
                "ref": ref,
                "transform": transform,
                "window": window,
                "direction": direction,
            }
        )
        if floor is not UNSET:
            field_dict["floor"] = floor

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        key = d.pop("key")

        ref = d.pop("ref")

        transform = RegionalIndexDefComponentsItemTransform(d.pop("transform"))

        window = RegionalIndexDefComponentsItemWindow(d.pop("window"))

        direction = RegionalIndexDefComponentsItemDirection(d.pop("direction"))

        def _parse_floor(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        floor = _parse_floor(d.pop("floor", UNSET))

        regional_index_def_components_item = cls(
            key=key,
            ref=ref,
            transform=transform,
            window=window,
            direction=direction,
            floor=floor,
        )

        regional_index_def_components_item.additional_properties = d
        return regional_index_def_components_item

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
