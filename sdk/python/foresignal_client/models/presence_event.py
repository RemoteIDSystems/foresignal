from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.presence_event_change_type_0 import PresenceEventChangeType0
    from ..models.presence_event_clients_item import PresenceEventClientsItem


T = TypeVar("T", bound="PresenceEvent")


@_attrs_define
class PresenceEvent:
    """SSE payload for `workspace.presence` (topic `workspace`): connected clients

    Attributes:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        clients (list[PresenceEventClientsItem]):
        at (datetime.datetime):
        change (None | PresenceEventChangeType0 | Unset):
    """

    workspace_id: str
    clients: list[PresenceEventClientsItem]
    at: datetime.datetime
    change: None | PresenceEventChangeType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.presence_event_change_type_0 import PresenceEventChangeType0

        workspace_id = self.workspace_id

        clients = []
        for clients_item_data in self.clients:
            clients_item = clients_item_data.to_dict()
            clients.append(clients_item)

        at = self.at.isoformat()

        change: dict[str, Any] | None | Unset
        if isinstance(self.change, Unset):
            change = UNSET
        elif isinstance(self.change, PresenceEventChangeType0):
            change = self.change.to_dict()
        else:
            change = self.change

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "workspaceId": workspace_id,
                "clients": clients,
                "at": at,
            }
        )
        if change is not UNSET:
            field_dict["change"] = change

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.presence_event_change_type_0 import PresenceEventChangeType0
        from ..models.presence_event_clients_item import PresenceEventClientsItem

        d = dict(src_dict)
        workspace_id = d.pop("workspaceId")

        clients = []
        _clients = d.pop("clients")
        for clients_item_data in _clients:
            clients_item = PresenceEventClientsItem.from_dict(clients_item_data)

            clients.append(clients_item)

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_change(data: object) -> None | PresenceEventChangeType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                change_type_0 = PresenceEventChangeType0.from_dict(data)

                return change_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PresenceEventChangeType0 | Unset, data)

        change = _parse_change(d.pop("change", UNSET))

        presence_event = cls(
            workspace_id=workspace_id,
            clients=clients,
            at=at,
            change=change,
        )

        presence_event.additional_properties = d
        return presence_event

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
