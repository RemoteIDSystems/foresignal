from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.presence_event_clients_item_client_kind import (
    PresenceEventClientsItemClientKind,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="PresenceEventClientsItem")


@_attrs_define
class PresenceEventClientsItem:
    """Connected client

    Attributes:
        client_id (str):
        client_kind (PresenceEventClientsItemClientKind):
        connected_at (datetime.datetime):
        principal (None | str | Unset):
    """

    client_id: str
    client_kind: PresenceEventClientsItemClientKind
    connected_at: datetime.datetime
    principal: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        client_id = self.client_id

        client_kind = self.client_kind.value

        connected_at = self.connected_at.isoformat()

        principal: None | str | Unset
        if isinstance(self.principal, Unset):
            principal = UNSET
        else:
            principal = self.principal

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "clientId": client_id,
                "clientKind": client_kind,
                "connectedAt": connected_at,
            }
        )
        if principal is not UNSET:
            field_dict["principal"] = principal

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        client_id = d.pop("clientId")

        client_kind = PresenceEventClientsItemClientKind(d.pop("clientKind"))

        connected_at = datetime.datetime.fromisoformat(d.pop("connectedAt"))

        def _parse_principal(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal = _parse_principal(d.pop("principal", UNSET))

        presence_event_clients_item = cls(
            client_id=client_id,
            client_kind=client_kind,
            connected_at=connected_at,
            principal=principal,
        )

        presence_event_clients_item.additional_properties = d
        return presence_event_clients_item

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
