from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.event_stream_filter_workspace_client_kind import (
    EventStreamFilterWorkspaceClientKind,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStreamFilterWorkspace")


@_attrs_define
class EventStreamFilterWorkspace:
    """Filter block for the `workspace` topic (129)

    Attributes:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        client_id (str | Unset): This client's id; echoed in presence and used for `originClientId`
        client_kind (EventStreamFilterWorkspaceClientKind | Unset):
    """

    workspace_id: str
    client_id: str | Unset = UNSET
    client_kind: EventStreamFilterWorkspaceClientKind | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        workspace_id = self.workspace_id

        client_id = self.client_id

        client_kind: str | Unset = UNSET
        if not isinstance(self.client_kind, Unset):
            client_kind = self.client_kind.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "workspaceId": workspace_id,
            }
        )
        if client_id is not UNSET:
            field_dict["clientId"] = client_id
        if client_kind is not UNSET:
            field_dict["clientKind"] = client_kind

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        workspace_id = d.pop("workspaceId")

        client_id = d.pop("clientId", UNSET)

        _client_kind = d.pop("clientKind", UNSET)
        client_kind: EventStreamFilterWorkspaceClientKind | Unset
        if isinstance(_client_kind, Unset):
            client_kind = UNSET
        else:
            client_kind = EventStreamFilterWorkspaceClientKind(_client_kind)

        event_stream_filter_workspace = cls(
            workspace_id=workspace_id,
            client_id=client_id,
            client_kind=client_kind,
        )

        event_stream_filter_workspace.additional_properties = d
        return event_stream_filter_workspace

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
