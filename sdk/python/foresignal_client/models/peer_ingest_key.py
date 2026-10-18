from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="PeerIngestKey")


@_attrs_define
class PeerIngestKey:
    """A newly issued peer ingest key, shown once (113)

    Attributes:
        peer_id (str):
        key (str):
        key_prefix (str):
        created_at (datetime.datetime):
    """

    peer_id: str
    key: str
    key_prefix: str
    created_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        peer_id = self.peer_id

        key = self.key

        key_prefix = self.key_prefix

        created_at = self.created_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "peerId": peer_id,
                "key": key,
                "keyPrefix": key_prefix,
                "createdAt": created_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        peer_id = d.pop("peerId")

        key = d.pop("key")

        key_prefix = d.pop("keyPrefix")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        peer_ingest_key = cls(
            peer_id=peer_id,
            key=key,
            key_prefix=key_prefix,
            created_at=created_at,
        )

        peer_ingest_key.additional_properties = d
        return peer_ingest_key

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
