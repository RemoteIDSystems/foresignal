from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.group_member_role import GroupMemberRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="GroupMember")


@_attrs_define
class GroupMember:
    """A principal's membership of a group

    Attributes:
        principal (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        role (GroupMemberRole):
        added_at (datetime.datetime | Unset):
        added_by (None | PrincipalSummary | Unset):
    """

    principal: PrincipalSummary
    role: GroupMemberRole
    added_at: datetime.datetime | Unset = UNSET
    added_by: None | PrincipalSummary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.principal_summary import PrincipalSummary

        principal = self.principal.to_dict()

        role = self.role.value

        added_at: str | Unset = UNSET
        if not isinstance(self.added_at, Unset):
            added_at = self.added_at.isoformat()

        added_by: dict[str, Any] | None | Unset
        if isinstance(self.added_by, Unset):
            added_by = UNSET
        elif isinstance(self.added_by, PrincipalSummary):
            added_by = self.added_by.to_dict()
        else:
            added_by = self.added_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "principal": principal,
                "role": role,
            }
        )
        if added_at is not UNSET:
            field_dict["addedAt"] = added_at
        if added_by is not UNSET:
            field_dict["addedBy"] = added_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        principal = PrincipalSummary.from_dict(d.pop("principal"))

        role = GroupMemberRole(d.pop("role"))

        _added_at = d.pop("addedAt", UNSET)
        added_at: datetime.datetime | Unset
        if isinstance(_added_at, Unset):
            added_at = UNSET
        else:
            added_at = datetime.datetime.fromisoformat(_added_at)

        def _parse_added_by(data: object) -> None | PrincipalSummary | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                added_by_type_0 = PrincipalSummary.from_dict(data)

                return added_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrincipalSummary | Unset, data)

        added_by = _parse_added_by(d.pop("addedBy", UNSET))

        group_member = cls(
            principal=principal,
            role=role,
            added_at=added_at,
            added_by=added_by,
        )

        group_member.additional_properties = d
        return group_member

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
