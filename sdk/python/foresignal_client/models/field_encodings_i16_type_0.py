from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FieldEncodingsI16Type0")


@_attrs_define
class FieldEncodingsI16Type0:
    """108: planar little-endian int16 u, v

    Attributes:
        url (str | Unset):
        scale (float | Unset):
        offset (float | Unset):
    """

    url: str | Unset = UNSET
    scale: float | Unset = UNSET
    offset: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        url = self.url

        scale = self.scale

        offset = self.offset

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if url is not UNSET:
            field_dict["url"] = url
        if scale is not UNSET:
            field_dict["scale"] = scale
        if offset is not UNSET:
            field_dict["offset"] = offset

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        url = d.pop("url", UNSET)

        scale = d.pop("scale", UNSET)

        offset = d.pop("offset", UNSET)

        field_encodings_i16_type_0 = cls(
            url=url,
            scale=scale,
            offset=offset,
        )

        field_encodings_i16_type_0.additional_properties = d
        return field_encodings_i16_type_0

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
