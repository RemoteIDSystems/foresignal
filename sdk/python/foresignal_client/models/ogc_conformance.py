from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="OgcConformance")


@_attrs_define
class OgcConformance:
    """Implemented conformance classes (112): Part 1 core and geojson; Part 3 filter, features-filter, queryables; CQL2
    basic-cql2, cql2-text, cql2-json, basic-spatial-functions. oas30 is not claimed

        Attributes:
            conforms_to (list[str]):
    """

    conforms_to: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        conforms_to = self.conforms_to

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "conformsTo": conforms_to,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        conforms_to = cast(list[str], d.pop("conformsTo"))

        ogc_conformance = cls(
            conforms_to=conforms_to,
        )

        ogc_conformance.additional_properties = d
        return ogc_conformance

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
