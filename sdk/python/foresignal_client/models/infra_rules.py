from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_rules_rules_item import InfraRulesRulesItem


T = TypeVar("T", bound="InfraRules")


@_attrs_define
class InfraRules:
    """Dependency rules (versioned resource) with operator overrides stored in `app_setting['infra.rules']`

    Attributes:
        rules (list[InfraRulesRulesItem]):
        version (str | Unset):
    """

    rules: list[InfraRulesRulesItem]
    version: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rules = []
        for rules_item_data in self.rules:
            rules_item = rules_item_data.to_dict()
            rules.append(rules_item)

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rules": rules,
            }
        )
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_rules_rules_item import InfraRulesRulesItem

        d = dict(src_dict)
        rules = []
        _rules = d.pop("rules")
        for rules_item_data in _rules:
            rules_item = InfraRulesRulesItem.from_dict(rules_item_data)

            rules.append(rules_item)

        version = d.pop("version", UNSET)

        infra_rules = cls(
            rules=rules,
            version=version,
        )

        infra_rules.additional_properties = d
        return infra_rules

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
