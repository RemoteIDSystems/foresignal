from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="MappingValueSpecType2")


@_attrs_define
class MappingValueSpecType2:
    """
    Attributes:
        const (Any):
    """

    const: Any

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
        const = d.pop("const")

        mapping_value_spec_type_2 = cls(
            const=const,
        )

        return mapping_value_spec_type_2
