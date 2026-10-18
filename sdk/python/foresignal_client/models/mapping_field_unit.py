from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mapping_field_unit_from import MappingFieldUnitFrom
from ..models.mapping_field_unit_to import MappingFieldUnitTo

T = TypeVar("T", bound="MappingFieldUnit")


@_attrs_define
class MappingFieldUnit:
    """Unit conversion from the built-in UCUM table; unknown pairs fail validation

    Attributes:
        from_ (MappingFieldUnitFrom):
        to (MappingFieldUnitTo):
    """

    from_: MappingFieldUnitFrom
    to: MappingFieldUnitTo
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_.value

        to = self.to.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = MappingFieldUnitFrom(d.pop("from"))

        to = MappingFieldUnitTo(d.pop("to"))

        mapping_field_unit = cls(
            from_=from_,
            to=to,
        )

        mapping_field_unit.additional_properties = d
        return mapping_field_unit

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
