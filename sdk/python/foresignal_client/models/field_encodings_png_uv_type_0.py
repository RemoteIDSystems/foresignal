from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FieldEncodingsPngUvType0")


@_attrs_define
class FieldEncodingsPngUvType0:
    """8-bit RGB PNG: R = u, G = v scaled linearly; B = 0

    Attributes:
        url (str | Unset):
        u_min (float | Unset):
        u_max (float | Unset):
        v_min (float | Unset):
        v_max (float | Unset):
        width (int | Unset):
        height (int | Unset):
        bounds (list[float] | Unset):
    """

    url: str | Unset = UNSET
    u_min: float | Unset = UNSET
    u_max: float | Unset = UNSET
    v_min: float | Unset = UNSET
    v_max: float | Unset = UNSET
    width: int | Unset = UNSET
    height: int | Unset = UNSET
    bounds: list[float] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        url = self.url

        u_min = self.u_min

        u_max = self.u_max

        v_min = self.v_min

        v_max = self.v_max

        width = self.width

        height = self.height

        bounds: list[float] | Unset = UNSET
        if not isinstance(self.bounds, Unset):
            bounds = self.bounds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if url is not UNSET:
            field_dict["url"] = url
        if u_min is not UNSET:
            field_dict["uMin"] = u_min
        if u_max is not UNSET:
            field_dict["uMax"] = u_max
        if v_min is not UNSET:
            field_dict["vMin"] = v_min
        if v_max is not UNSET:
            field_dict["vMax"] = v_max
        if width is not UNSET:
            field_dict["width"] = width
        if height is not UNSET:
            field_dict["height"] = height
        if bounds is not UNSET:
            field_dict["bounds"] = bounds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        url = d.pop("url", UNSET)

        u_min = d.pop("uMin", UNSET)

        u_max = d.pop("uMax", UNSET)

        v_min = d.pop("vMin", UNSET)

        v_max = d.pop("vMax", UNSET)

        width = d.pop("width", UNSET)

        height = d.pop("height", UNSET)

        bounds = cast(list[float], d.pop("bounds", UNSET))

        field_encodings_png_uv_type_0 = cls(
            url=url,
            u_min=u_min,
            u_max=u_max,
            v_min=v_min,
            v_max=v_max,
            width=width,
            height=height,
            bounds=bounds,
        )

        field_encodings_png_uv_type_0.additional_properties = d
        return field_encodings_png_uv_type_0

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
