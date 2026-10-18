from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="Contract")


@_attrs_define
class Contract:
    """Client contract versions in force (Client-Contract §16)

    Attributes:
        contract_version (str):  Example: 1.5.
        object_types (list[str]):
        relation_types (list[str]):
        context_schema_version (int):
        deep_link_version (int):
    """

    contract_version: str
    object_types: list[str]
    relation_types: list[str]
    context_schema_version: int
    deep_link_version: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        contract_version = self.contract_version

        object_types = self.object_types

        relation_types = self.relation_types

        context_schema_version = self.context_schema_version

        deep_link_version = self.deep_link_version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "contractVersion": contract_version,
                "objectTypes": object_types,
                "relationTypes": relation_types,
                "contextSchemaVersion": context_schema_version,
                "deepLinkVersion": deep_link_version,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        contract_version = d.pop("contractVersion")

        object_types = cast(list[str], d.pop("objectTypes"))

        relation_types = cast(list[str], d.pop("relationTypes"))

        context_schema_version = d.pop("contextSchemaVersion")

        deep_link_version = d.pop("deepLinkVersion")

        contract = cls(
            contract_version=contract_version,
            object_types=object_types,
            relation_types=relation_types,
            context_schema_version=context_schema_version,
            deep_link_version=deep_link_version,
        )

        contract.additional_properties = d
        return contract

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
