from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="KindDisplay")


@_attrs_define
class KindDisplay:
    """Client display hints for one kind (descriptor `kindDisplay`); `titleTemplate` is never used server-side

    Attributes:
        title_template (None | str | Unset): Client title template with `{field}` placeholders Example: M {mag} -
            {place}.
        icon (None | str | Unset): Icon name Example: earthquake.
        colour (None | str | Unset): Colour `#RRGGBB` Example: #D9480F.
        sidc (None | str | Unset): Optional MIL-STD-2525 symbol identification code (15 or 20 characters)
        rotate_field (None | str | Unset): Payload field rotating the icon (e.g. `track`); must be in tileProperties
        size_field (None | str | Unset): Payload field scaling circle radius (e.g. `frp`, `mag`); must be in
            tileProperties
        color_field (None | str | Unset): Payload field driving a data-driven colour match (default `severity`); must be
            in tileProperties
    """

    title_template: None | str | Unset = UNSET
    icon: None | str | Unset = UNSET
    colour: None | str | Unset = UNSET
    sidc: None | str | Unset = UNSET
    rotate_field: None | str | Unset = UNSET
    size_field: None | str | Unset = UNSET
    color_field: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title_template: None | str | Unset
        if isinstance(self.title_template, Unset):
            title_template = UNSET
        else:
            title_template = self.title_template

        icon: None | str | Unset
        if isinstance(self.icon, Unset):
            icon = UNSET
        else:
            icon = self.icon

        colour: None | str | Unset
        if isinstance(self.colour, Unset):
            colour = UNSET
        else:
            colour = self.colour

        sidc: None | str | Unset
        if isinstance(self.sidc, Unset):
            sidc = UNSET
        else:
            sidc = self.sidc

        rotate_field: None | str | Unset
        if isinstance(self.rotate_field, Unset):
            rotate_field = UNSET
        else:
            rotate_field = self.rotate_field

        size_field: None | str | Unset
        if isinstance(self.size_field, Unset):
            size_field = UNSET
        else:
            size_field = self.size_field

        color_field: None | str | Unset
        if isinstance(self.color_field, Unset):
            color_field = UNSET
        else:
            color_field = self.color_field

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title_template is not UNSET:
            field_dict["titleTemplate"] = title_template
        if icon is not UNSET:
            field_dict["icon"] = icon
        if colour is not UNSET:
            field_dict["colour"] = colour
        if sidc is not UNSET:
            field_dict["sidc"] = sidc
        if rotate_field is not UNSET:
            field_dict["rotateField"] = rotate_field
        if size_field is not UNSET:
            field_dict["sizeField"] = size_field
        if color_field is not UNSET:
            field_dict["colorField"] = color_field

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_title_template(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title_template = _parse_title_template(d.pop("titleTemplate", UNSET))

        def _parse_icon(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        icon = _parse_icon(d.pop("icon", UNSET))

        def _parse_colour(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        colour = _parse_colour(d.pop("colour", UNSET))

        def _parse_sidc(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        sidc = _parse_sidc(d.pop("sidc", UNSET))

        def _parse_rotate_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rotate_field = _parse_rotate_field(d.pop("rotateField", UNSET))

        def _parse_size_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        size_field = _parse_size_field(d.pop("sizeField", UNSET))

        def _parse_color_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        color_field = _parse_color_field(d.pop("colorField", UNSET))

        kind_display = cls(
            title_template=title_template,
            icon=icon,
            colour=colour,
            sidc=sidc,
            rotate_field=rotate_field,
            size_field=size_field,
            color_field=color_field,
        )

        kind_display.additional_properties = d
        return kind_display

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
