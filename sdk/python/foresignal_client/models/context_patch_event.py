from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.context_patch_event_patch import ContextPatchEventPatch


T = TypeVar("T", bound="ContextPatchEvent")


@_attrs_define
class ContextPatchEvent:
    """SSE payload for `workspace.context` (topic `workspace`)

    Attributes:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        version (int):
        patch (ContextPatchEventPatch): JSON merge patch applied
        at (datetime.datetime):
        origin_client_id (None | str | Unset):
        principal (None | str | Unset):
    """

    workspace_id: str
    version: int
    patch: ContextPatchEventPatch
    at: datetime.datetime
    origin_client_id: None | str | Unset = UNSET
    principal: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        workspace_id = self.workspace_id

        version = self.version

        patch = self.patch.to_dict()

        at = self.at.isoformat()

        origin_client_id: None | str | Unset
        if isinstance(self.origin_client_id, Unset):
            origin_client_id = UNSET
        else:
            origin_client_id = self.origin_client_id

        principal: None | str | Unset
        if isinstance(self.principal, Unset):
            principal = UNSET
        else:
            principal = self.principal

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "workspaceId": workspace_id,
                "version": version,
                "patch": patch,
                "at": at,
            }
        )
        if origin_client_id is not UNSET:
            field_dict["originClientId"] = origin_client_id
        if principal is not UNSET:
            field_dict["principal"] = principal

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.context_patch_event_patch import ContextPatchEventPatch

        d = dict(src_dict)
        workspace_id = d.pop("workspaceId")

        version = d.pop("version")

        patch = ContextPatchEventPatch.from_dict(d.pop("patch"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_origin_client_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        origin_client_id = _parse_origin_client_id(d.pop("originClientId", UNSET))

        def _parse_principal(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal = _parse_principal(d.pop("principal", UNSET))

        context_patch_event = cls(
            workspace_id=workspace_id,
            version=version,
            patch=patch,
            at=at,
            origin_client_id=origin_client_id,
            principal=principal,
        )

        context_patch_event.additional_properties = d
        return context_patch_event

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
