from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="MappingValueSpecType1")


@_attrs_define
class MappingValueSpecType1:
    """
    Attributes:
        template (str):  Example: airrohr-{{$.esp8266id}}.
    """

    template: str

    def to_dict(self) -> dict[str, Any]:
        template = self.template

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "template": template,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        template = d.pop("template")

        mapping_value_spec_type_1 = cls(
            template=template,
        )

        return mapping_value_spec_type_1
