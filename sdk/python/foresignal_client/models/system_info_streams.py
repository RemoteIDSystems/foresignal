from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoStreams")


@_attrs_define
class SystemInfoStreams:
    """
    Attributes:
        clients (int | Unset): Open SSE clients
        max_clients (int | Unset):
        change_subscribers (int | Unset):
        alert_subscribers (int | Unset):
        event_subscribers (int | Unset):
    """

    clients: int | Unset = UNSET
    max_clients: int | Unset = UNSET
    change_subscribers: int | Unset = UNSET
    alert_subscribers: int | Unset = UNSET
    event_subscribers: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        clients = self.clients

        max_clients = self.max_clients

        change_subscribers = self.change_subscribers

        alert_subscribers = self.alert_subscribers

        event_subscribers = self.event_subscribers

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if clients is not UNSET:
            field_dict["clients"] = clients
        if max_clients is not UNSET:
            field_dict["maxClients"] = max_clients
        if change_subscribers is not UNSET:
            field_dict["changeSubscribers"] = change_subscribers
        if alert_subscribers is not UNSET:
            field_dict["alertSubscribers"] = alert_subscribers
        if event_subscribers is not UNSET:
            field_dict["eventSubscribers"] = event_subscribers

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        clients = d.pop("clients", UNSET)

        max_clients = d.pop("maxClients", UNSET)

        change_subscribers = d.pop("changeSubscribers", UNSET)

        alert_subscribers = d.pop("alertSubscribers", UNSET)

        event_subscribers = d.pop("eventSubscribers", UNSET)

        system_info_streams = cls(
            clients=clients,
            max_clients=max_clients,
            change_subscribers=change_subscribers,
            alert_subscribers=alert_subscribers,
            event_subscribers=event_subscribers,
        )

        system_info_streams.additional_properties = d
        return system_info_streams

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
