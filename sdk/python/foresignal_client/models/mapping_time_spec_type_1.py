from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="MappingTimeSpecType1")


@_attrs_define
class MappingTimeSpecType1:
    """
    Attributes:
        received (bool):
    """

    received: bool

    def to_dict(self) -> dict[str, Any]:
        received = self.received

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "received": received,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        received = d.pop("received")

        mapping_time_spec_type_1 = cls(
            received=received,
        )

        return mapping_time_spec_type_1
