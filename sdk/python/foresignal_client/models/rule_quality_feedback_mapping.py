from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.rule_quality_feedback_mapping_additional_property import (
    RuleQualityFeedbackMappingAdditionalProperty,
)

T = TypeVar("T", bound="RuleQualityFeedbackMapping")


@_attrs_define
class RuleQualityFeedbackMapping:
    additional_properties: dict[str, RuleQualityFeedbackMappingAdditionalProperty] = (
        _attrs_field(init=False, factory=dict)
    )

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        rule_quality_feedback_mapping = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = RuleQualityFeedbackMappingAdditionalProperty(
                prop_dict
            )

            additional_properties[prop_name] = additional_property

        rule_quality_feedback_mapping.additional_properties = additional_properties
        return rule_quality_feedback_mapping

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> RuleQualityFeedbackMappingAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: RuleQualityFeedbackMappingAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
