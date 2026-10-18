from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.workspace_patch_members_additional_property import (
    WorkspacePatchMembersAdditionalProperty,
)

T = TypeVar("T", bound="WorkspacePatchMembers")


@_attrs_define
class WorkspacePatchMembers:
    """Principal id → role"""

    additional_properties: dict[str, WorkspacePatchMembersAdditionalProperty] = (
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
        workspace_patch_members = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = WorkspacePatchMembersAdditionalProperty(prop_dict)

            additional_properties[prop_name] = additional_property

        workspace_patch_members.additional_properties = additional_properties
        return workspace_patch_members

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> WorkspacePatchMembersAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: WorkspacePatchMembersAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
