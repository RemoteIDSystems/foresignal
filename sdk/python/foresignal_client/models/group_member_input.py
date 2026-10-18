from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.group_member_input_role import GroupMemberInputRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="GroupMemberInput")


@_attrs_define
class GroupMemberInput:
    """Add or update a group member

    Attributes:
        principal_id (str): Principal id or handle
        role (GroupMemberInputRole | Unset):  Default: GroupMemberInputRole.MEMBER.
    """

    principal_id: str
    role: GroupMemberInputRole | Unset = GroupMemberInputRole.MEMBER
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        principal_id = self.principal_id

        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "principalId": principal_id,
            }
        )
        if role is not UNSET:
            field_dict["role"] = role

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        principal_id = d.pop("principalId")

        _role = d.pop("role", UNSET)
        role: GroupMemberInputRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = GroupMemberInputRole(_role)

        group_member_input = cls(
            principal_id=principal_id,
            role=role,
        )

        group_member_input.additional_properties = d
        return group_member_input

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
