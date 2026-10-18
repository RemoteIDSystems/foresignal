from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="MappingTimeSpecType0")


@_attrs_define
class MappingTimeSpecType0:
    """
    Attributes:
        path (str):  Example: $.dateutc.
        format_ (str): `iso`, `epochS`, `epochMs` or `pattern:<java DateTimeFormatter>` Example: pattern:yyyy-MM-dd
            HH:mm:ss.
        zone (str | Unset): Zone for zone-less inputs Example: UTC.
    """

    path: str
    format_: str
    zone: str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        path = self.path

        format_ = self.format_

        zone = self.zone

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "path": path,
                "format": format_,
            }
        )
        if zone is not UNSET:
            field_dict["zone"] = zone

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = d.pop("path")

        format_ = d.pop("format")

        zone = d.pop("zone", UNSET)

        mapping_time_spec_type_0 = cls(
            path=path,
            format_=format_,
            zone=zone,
        )

        return mapping_time_spec_type_0
