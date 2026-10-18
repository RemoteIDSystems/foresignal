from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="CorsSettings")


@_attrs_define
class CorsSettings:
    """
    Attributes:
        read_origins (list[str]):
        write_origins (list[str]):
    """

    read_origins: list[str]
    write_origins: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        read_origins = self.read_origins

        write_origins = self.write_origins

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "readOrigins": read_origins,
                "writeOrigins": write_origins,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        read_origins = cast(list[str], d.pop("readOrigins"))

        write_origins = cast(list[str], d.pop("writeOrigins"))

        cors_settings = cls(
            read_origins=read_origins,
            write_origins=write_origins,
        )

        cors_settings.additional_properties = d
        return cors_settings

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
