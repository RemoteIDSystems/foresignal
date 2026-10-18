from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.workspace_context import WorkspaceContext
    from ..models.workspace_input_layout import WorkspaceInputLayout
    from ..models.workspace_input_members import WorkspaceInputMembers


T = TypeVar("T", bound="WorkspaceInput")


@_attrs_define
class WorkspaceInput:
    """A new workspace; the caller becomes owner

    Attributes:
        name (str):
        members (WorkspaceInputMembers | Unset): Principal id → role
        context (WorkspaceContext | Unset): Shared, versioned workspace context (Client-Contract §5)
        layout (WorkspaceInputLayout | Unset):
        saved_view_ids (list[str] | Unset):
        investigation_id (None | str | Unset):
        private (bool | Unset):  Default: False.
    """

    name: str
    members: WorkspaceInputMembers | Unset = UNSET
    context: WorkspaceContext | Unset = UNSET
    layout: WorkspaceInputLayout | Unset = UNSET
    saved_view_ids: list[str] | Unset = UNSET
    investigation_id: None | str | Unset = UNSET
    private: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        members: dict[str, Any] | Unset = UNSET
        if not isinstance(self.members, Unset):
            members = self.members.to_dict()

        context: dict[str, Any] | Unset = UNSET
        if not isinstance(self.context, Unset):
            context = self.context.to_dict()

        layout: dict[str, Any] | Unset = UNSET
        if not isinstance(self.layout, Unset):
            layout = self.layout.to_dict()

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
        field_dict.update(
            {
                "name": name,
            }
        )
        if members is not UNSET:
            field_dict["members"] = members
        if context is not UNSET:
            field_dict["context"] = context
        if layout is not UNSET:
            field_dict["layout"] = layout
        if saved_view_ids is not UNSET:
            field_dict["savedViewIds"] = saved_view_ids
        if investigation_id is not UNSET:
            field_dict["investigationId"] = investigation_id
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.workspace_context import WorkspaceContext
        from ..models.workspace_input_layout import WorkspaceInputLayout
        from ..models.workspace_input_members import WorkspaceInputMembers

        d = dict(src_dict)
        name = d.pop("name")

        _members = d.pop("members", UNSET)
        members: WorkspaceInputMembers | Unset
        if isinstance(_members, Unset):
            members = UNSET
        else:
            members = WorkspaceInputMembers.from_dict(_members)

        _context = d.pop("context", UNSET)
        context: WorkspaceContext | Unset
        if isinstance(_context, Unset):
            context = UNSET
        else:
            context = WorkspaceContext.from_dict(_context)

        _layout = d.pop("layout", UNSET)
        layout: WorkspaceInputLayout | Unset
        if isinstance(_layout, Unset):
            layout = UNSET
        else:
            layout = WorkspaceInputLayout.from_dict(_layout)

        saved_view_ids = cast(list[str], d.pop("savedViewIds", UNSET))

        def _parse_investigation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        investigation_id = _parse_investigation_id(d.pop("investigationId", UNSET))

        private = d.pop("private", UNSET)

        workspace_input = cls(
            name=name,
            members=members,
            context=context,
            layout=layout,
            saved_view_ids=saved_view_ids,
            investigation_id=investigation_id,
            private=private,
        )

        workspace_input.additional_properties = d
        return workspace_input

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
