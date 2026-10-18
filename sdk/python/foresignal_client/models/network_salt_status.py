from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="NetworkSaltStatus")


@_attrs_define
class NetworkSaltStatus:
    """A network salt's public status

    Attributes:
        network_id (str):
        salt_id (str):
        created_at (datetime.datetime):
        rotates_at (datetime.datetime):
    """

    network_id: str
    salt_id: str
    created_at: datetime.datetime
    rotates_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        network_id = self.network_id

        salt_id = self.salt_id

        created_at = self.created_at.isoformat()

        rotates_at = self.rotates_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "networkId": network_id,
                "saltId": salt_id,
                "createdAt": created_at,
                "rotatesAt": rotates_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        network_id = d.pop("networkId")

        salt_id = d.pop("saltId")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        rotates_at = datetime.datetime.fromisoformat(d.pop("rotatesAt"))

        network_salt_status = cls(
            network_id=network_id,
            salt_id=salt_id,
            created_at=created_at,
            rotates_at=rotates_at,
        )

        network_salt_status.additional_properties = d
        return network_salt_status

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
