from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.workspace_context import WorkspaceContext


T = TypeVar("T", bound="WorkspaceContextState")


@_attrs_define
class WorkspaceContextState:
    """A workspace's context with its version (Client-Contract §5)

    Attributes:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        context_version (int):
        context (WorkspaceContext): Shared, versioned workspace context (Client-Contract §5)
    """

    workspace_id: str
    context_version: int
    context: WorkspaceContext
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        workspace_id = self.workspace_id

        context_version = self.context_version

        context = self.context.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "workspaceId": workspace_id,
                "contextVersion": context_version,
                "context": context,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.workspace_context import WorkspaceContext

        d = dict(src_dict)
        workspace_id = d.pop("workspaceId")

        context_version = d.pop("contextVersion")

        context = WorkspaceContext.from_dict(d.pop("context"))

        workspace_context_state = cls(
            workspace_id=workspace_id,
            context_version=context_version,
            context=context,
        )

        workspace_context_state.additional_properties = d
        return workspace_context_state

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
