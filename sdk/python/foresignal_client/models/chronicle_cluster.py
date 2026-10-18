from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.chronicle_cluster_members_item import ChronicleClusterMembersItem


T = TypeVar("T", bound="ChronicleCluster")


@_attrs_define
class ChronicleCluster:
    """Connected component of medium-or-stronger chronicle links, shown as far as the caller's disclosure allows

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        members (list[ChronicleClusterMembersItem]):
        visible_members (int):
        opaque_members (int):
        updated_at (datetime.datetime):
    """

    id: str
    members: list[ChronicleClusterMembersItem]
    visible_members: int
    opaque_members: int
    updated_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        members = []
        for members_item_data in self.members:
            members_item = members_item_data.to_dict()
            members.append(members_item)

        visible_members = self.visible_members

        opaque_members = self.opaque_members

        updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "members": members,
                "visibleMembers": visible_members,
                "opaqueMembers": opaque_members,
                "updatedAt": updated_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_cluster_members_item import ChronicleClusterMembersItem

        d = dict(src_dict)
        id = d.pop("id")

        members = []
        _members = d.pop("members")
        for members_item_data in _members:
            members_item = ChronicleClusterMembersItem.from_dict(members_item_data)

            members.append(members_item)

        visible_members = d.pop("visibleMembers")

        opaque_members = d.pop("opaqueMembers")

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        chronicle_cluster = cls(
            id=id,
            members=members,
            visible_members=visible_members,
            opaque_members=opaque_members,
            updated_at=updated_at,
        )

        chronicle_cluster.additional_properties = d
        return chronicle_cluster

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
