from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.workspace_caller_role import WorkspaceCallerRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.workspace_context import WorkspaceContext
    from ..models.workspace_layout import WorkspaceLayout
    from ..models.workspace_members import WorkspaceMembers


T = TypeVar("T", bound="Workspace")


@_attrs_define
class Workspace:
    """A shared investigation focus: versioned context plus opaque client layout

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        owner (str): Principal id
        members (WorkspaceMembers): Principal id → role
        context (WorkspaceContext): Shared, versioned workspace context (Client-Contract §5)
        context_version (int):
        private (bool):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        caller_role (WorkspaceCallerRole | Unset):
        layout (WorkspaceLayout | Unset): Opaque client JSON (≤ 64 KB); never interpreted by Foresignal
        saved_view_ids (list[str] | Unset):
        investigation_id (None | str | Unset):
    """

    id: str
    name: str
    owner: str
    members: WorkspaceMembers
    context: WorkspaceContext
    context_version: int
    private: bool
    created_at: datetime.datetime
    updated_at: datetime.datetime
    ref: str | Unset = UNSET
    caller_role: WorkspaceCallerRole | Unset = UNSET
    layout: WorkspaceLayout | Unset = UNSET
    saved_view_ids: list[str] | Unset = UNSET
    investigation_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        owner = self.owner

        members = self.members.to_dict()

        context = self.context.to_dict()

        context_version = self.context_version

        private = self.private

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        ref = self.ref

        caller_role: str | Unset = UNSET
        if not isinstance(self.caller_role, Unset):
            caller_role = self.caller_role.value

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "owner": owner,
                "members": members,
                "context": context,
                "contextVersion": context_version,
                "private": private,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if caller_role is not UNSET:
            field_dict["callerRole"] = caller_role
        if layout is not UNSET:
            field_dict["layout"] = layout
        if saved_view_ids is not UNSET:
            field_dict["savedViewIds"] = saved_view_ids
        if investigation_id is not UNSET:
            field_dict["investigationId"] = investigation_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.workspace_context import WorkspaceContext
        from ..models.workspace_layout import WorkspaceLayout
        from ..models.workspace_members import WorkspaceMembers

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        owner = d.pop("owner")

        members = WorkspaceMembers.from_dict(d.pop("members"))

        context = WorkspaceContext.from_dict(d.pop("context"))

        context_version = d.pop("contextVersion")

        private = d.pop("private")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        ref = d.pop("ref", UNSET)

        _caller_role = d.pop("callerRole", UNSET)
        caller_role: WorkspaceCallerRole | Unset
        if isinstance(_caller_role, Unset):
            caller_role = UNSET
        else:
            caller_role = WorkspaceCallerRole(_caller_role)

        _layout = d.pop("layout", UNSET)
        layout: WorkspaceLayout | Unset
        if isinstance(_layout, Unset):
            layout = UNSET
        else:
            layout = WorkspaceLayout.from_dict(_layout)

        saved_view_ids = cast(list[str], d.pop("savedViewIds", UNSET))

        def _parse_investigation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        investigation_id = _parse_investigation_id(d.pop("investigationId", UNSET))

        workspace = cls(
            id=id,
            name=name,
            owner=owner,
            members=members,
            context=context,
            context_version=context_version,
            private=private,
            created_at=created_at,
            updated_at=updated_at,
            ref=ref,
            caller_role=caller_role,
            layout=layout,
            saved_view_ids=saved_view_ids,
            investigation_id=investigation_id,
        )

        workspace.additional_properties = d
        return workspace

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
