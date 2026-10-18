from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

T = TypeVar("T", bound="RecordMappingGeometryType2")


@_attrs_define
class RecordMappingGeometryType2:
    """
    Attributes:
        const (list[float]):
    """

    const: list[float]

    def to_dict(self) -> dict[str, Any]:
        const = self.const

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "const": const,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        const = cast(list[float], d.pop("const"))

        record_mapping_geometry_type_2 = cls(
            const=const,
        )

        return record_mapping_geometry_type_2
