from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpPresentation")


@_attrs_define
class EdpPresentation:
    """Layer hints for clients and styles

    Attributes:
        color (None | str | Unset):
        icon (None | str | Unset):
        label_field (None | str | Unset):
        min_zoom (int | None | Unset):
        max_zoom (int | None | Unset):
        tile_properties (list[str] | Unset):
    """

    color: None | str | Unset = UNSET
    icon: None | str | Unset = UNSET
    label_field: None | str | Unset = UNSET
    min_zoom: int | None | Unset = UNSET
    max_zoom: int | None | Unset = UNSET
    tile_properties: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        color: None | str | Unset
        if isinstance(self.color, Unset):
            color = UNSET
        else:
            color = self.color

        icon: None | str | Unset
        if isinstance(self.icon, Unset):
            icon = UNSET
        else:
            icon = self.icon

        label_field: None | str | Unset
        if isinstance(self.label_field, Unset):
            label_field = UNSET
        else:
            label_field = self.label_field

        min_zoom: int | None | Unset
        if isinstance(self.min_zoom, Unset):
            min_zoom = UNSET
        else:
            min_zoom = self.min_zoom

        max_zoom: int | None | Unset
        if isinstance(self.max_zoom, Unset):
            max_zoom = UNSET
        else:
            max_zoom = self.max_zoom

        tile_properties: list[str] | Unset = UNSET
        if not isinstance(self.tile_properties, Unset):
            tile_properties = self.tile_properties

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if color is not UNSET:
            field_dict["color"] = color
        if icon is not UNSET:
            field_dict["icon"] = icon
        if label_field is not UNSET:
            field_dict["labelField"] = label_field
        if min_zoom is not UNSET:
            field_dict["minZoom"] = min_zoom
        if max_zoom is not UNSET:
            field_dict["maxZoom"] = max_zoom
        if tile_properties is not UNSET:
            field_dict["tileProperties"] = tile_properties

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_color(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        color = _parse_color(d.pop("color", UNSET))

        def _parse_icon(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        icon = _parse_icon(d.pop("icon", UNSET))

        def _parse_label_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label_field = _parse_label_field(d.pop("labelField", UNSET))

        def _parse_min_zoom(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        min_zoom = _parse_min_zoom(d.pop("minZoom", UNSET))

        def _parse_max_zoom(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_zoom = _parse_max_zoom(d.pop("maxZoom", UNSET))

        tile_properties = cast(list[str], d.pop("tileProperties", UNSET))

        edp_presentation = cls(
            color=color,
            icon=icon,
            label_field=label_field,
            min_zoom=min_zoom,
            max_zoom=max_zoom,
            tile_properties=tile_properties,
        )

        edp_presentation.additional_properties = d
        return edp_presentation

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
