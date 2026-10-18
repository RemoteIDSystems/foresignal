from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary
    from ..models.tag import Tag


T = TypeVar("T", bound="TagAssignment")


@_attrs_define
class TagAssignment:
    """A tag assigned to an object

    Attributes:
        tag_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        object_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        assigned_at (datetime.datetime):
        tag (Tag | Unset): A personal or shared tag that can be assigned to any object reference
        assigned_by (PrincipalSummary | Unset): Public view of a principal: handle, display name, kind and avatar only
    """

    tag_id: str
    object_ref: str
    assigned_at: datetime.datetime
    tag: Tag | Unset = UNSET
    assigned_by: PrincipalSummary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tag_id = self.tag_id

        object_ref = self.object_ref

        assigned_at = self.assigned_at.isoformat()

        tag: dict[str, Any] | Unset = UNSET
        if not isinstance(self.tag, Unset):
            tag = self.tag.to_dict()

        assigned_by: dict[str, Any] | Unset = UNSET
        if not isinstance(self.assigned_by, Unset):
            assigned_by = self.assigned_by.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tagId": tag_id,
                "objectRef": object_ref,
                "assignedAt": assigned_at,
            }
        )
        if tag is not UNSET:
            field_dict["tag"] = tag
        if assigned_by is not UNSET:
            field_dict["assignedBy"] = assigned_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary
        from ..models.tag import Tag

        d = dict(src_dict)
        tag_id = d.pop("tagId")

        object_ref = d.pop("objectRef")

        assigned_at = datetime.datetime.fromisoformat(d.pop("assignedAt"))

        _tag = d.pop("tag", UNSET)
        tag: Tag | Unset
        if isinstance(_tag, Unset):
            tag = UNSET
        else:
            tag = Tag.from_dict(_tag)

        _assigned_by = d.pop("assignedBy", UNSET)
        assigned_by: PrincipalSummary | Unset
        if isinstance(_assigned_by, Unset):
            assigned_by = UNSET
        else:
            assigned_by = PrincipalSummary.from_dict(_assigned_by)

        tag_assignment = cls(
            tag_id=tag_id,
            object_ref=object_ref,
            assigned_at=assigned_at,
            tag=tag,
            assigned_by=assigned_by,
        )

        tag_assignment.additional_properties = d
        return tag_assignment

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
