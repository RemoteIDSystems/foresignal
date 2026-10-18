from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.workspace_patch_members import WorkspacePatchMembers


T = TypeVar("T", bound="WorkspacePatch")


@_attrs_define
class WorkspacePatch:
    """JSON merge patch for workspace metadata (context and layout have their own operations)

    Attributes:
        name (str | Unset):
        members (WorkspacePatchMembers | Unset): Principal id → role
        saved_view_ids (list[str] | Unset):
        investigation_id (None | str | Unset):
        private (bool | Unset):
    """

    name: str | Unset = UNSET
    members: WorkspacePatchMembers | Unset = UNSET
    saved_view_ids: list[str] | Unset = UNSET
    investigation_id: None | str | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        members: dict[str, Any] | Unset = UNSET
        if not isinstance(self.members, Unset):
            members = self.members.to_dict()

        saved_view_ids: list[str] | Unset = UNSET
        if not isinstance(self.saved_view_ids, Unset):
            saved_view_ids = self.saved_view_ids

        investigation_id: None | str | Unset
        if isinstance(self.investigation_id, Unset):
            investigation_id = UNSET
        else:
            investigation_id = self.investigation_id

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if members is not UNSET:
            field_dict["members"] = members
        if saved_view_ids is not UNSET:
            field_dict["savedViewIds"] = saved_view_ids
        if investigation_id is not UNSET:
            field_dict["investigationId"] = investigation_id
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.workspace_patch_members import WorkspacePatchMembers

        d = dict(src_dict)
        name = d.pop("name", UNSET)

        _members = d.pop("members", UNSET)
        members: WorkspacePatchMembers | Unset
        if isinstance(_members, Unset):
            members = UNSET
        else:
            members = WorkspacePatchMembers.from_dict(_members)

        saved_view_ids = cast(list[str], d.pop("savedViewIds", UNSET))

        def _parse_investigation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        investigation_id = _parse_investigation_id(d.pop("investigationId", UNSET))

        private = d.pop("private", UNSET)

        workspace_patch = cls(
            name=name,
            members=members,
            saved_view_ids=saved_view_ids,
            investigation_id=investigation_id,
            private=private,
        )

        workspace_patch.additional_properties = d
        return workspace_patch

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
