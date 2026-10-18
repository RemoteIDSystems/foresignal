from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="MappingValueSpecType0")


@_attrs_define
class MappingValueSpecType0:
    """
    Attributes:
        path (str):  Example: $.stationid.
    """

    path: str

    def to_dict(self) -> dict[str, Any]:
        path = self.path

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "path": path,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = d.pop("path")

        mapping_value_spec_type_0 = cls(
            path=path,
        )

        return mapping_value_spec_type_0
