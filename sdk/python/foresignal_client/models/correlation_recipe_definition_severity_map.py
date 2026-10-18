from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity

T = TypeVar("T", bound="CorrelationRecipeDefinitionSeverityMap")


@_attrs_define
class CorrelationRecipeDefinitionSeverityMap:
    """Rule → severity mapping"""

    additional_properties: dict[str, Severity] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        correlation_recipe_definition_severity_map = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = Severity(prop_dict)

            additional_properties[prop_name] = additional_property

        correlation_recipe_definition_severity_map.additional_properties = (
            additional_properties
        )
        return correlation_recipe_definition_severity_map

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Severity:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Severity) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
