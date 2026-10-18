from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_status_clients_type_0_peers_item import (
        SinkStatusClientsType0PeersItem,
    )


T = TypeVar("T", bound="SinkStatusClientsType0")


@_attrs_define
class SinkStatusClientsType0:
    """Connected clients of a SERVE sink

    Attributes:
        connected (int):
        max_ (int):
        rejected (int | Unset): Connections closed because the peer address was not allowed or the maxClients cap was
            reached
        peers (list[SinkStatusClientsType0PeersItem] | Unset):
    """

    connected: int
    max_: int
    rejected: int | Unset = UNSET
    peers: list[SinkStatusClientsType0PeersItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        connected = self.connected

        max_ = self.max_

        rejected = self.rejected

        peers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.peers, Unset):
            peers = []
            for peers_item_data in self.peers:
                peers_item = peers_item_data.to_dict()
                peers.append(peers_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "connected": connected,
                "max": max_,
            }
        )
        if rejected is not UNSET:
            field_dict["rejected"] = rejected
        if peers is not UNSET:
            field_dict["peers"] = peers

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_status_clients_type_0_peers_item import (
            SinkStatusClientsType0PeersItem,
        )

        d = dict(src_dict)
        connected = d.pop("connected")

        max_ = d.pop("max")

        rejected = d.pop("rejected", UNSET)

        _peers = d.pop("peers", UNSET)
        peers: list[SinkStatusClientsType0PeersItem] | Unset = UNSET
        if _peers is not UNSET:
            peers = []
            for peers_item_data in _peers:
                peers_item = SinkStatusClientsType0PeersItem.from_dict(peers_item_data)

                peers.append(peers_item)

        sink_status_clients_type_0 = cls(
            connected=connected,
            max_=max_,
            rejected=rejected,
            peers=peers,
        )

        sink_status_clients_type_0.additional_properties = d
        return sink_status_clients_type_0

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
