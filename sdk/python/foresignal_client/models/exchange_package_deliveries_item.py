from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exchange_package_deliveries_item_status import (
    ExchangePackageDeliveriesItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExchangePackageDeliveriesItem")


@_attrs_define
class ExchangePackageDeliveriesItem:
    """Delivery to a peer

    Attributes:
        instance_id (str):
        status (ExchangePackageDeliveriesItemStatus):
        peer_id (None | str | Unset):
        at (datetime.datetime | None | Unset):
        error (None | str | Unset):
    """

    instance_id: str
    status: ExchangePackageDeliveriesItemStatus
    peer_id: None | str | Unset = UNSET
    at: datetime.datetime | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        status = self.status.value

        peer_id: None | str | Unset
        if isinstance(self.peer_id, Unset):
            peer_id = UNSET
        else:
            peer_id = self.peer_id

        at: None | str | Unset
        if isinstance(self.at, Unset):
            at = UNSET
        elif isinstance(self.at, datetime.datetime):
            at = self.at.isoformat()
        else:
            at = self.at

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "status": status,
            }
        )
        if peer_id is not UNSET:
            field_dict["peerId"] = peer_id
        if at is not UNSET:
            field_dict["at"] = at
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        status = ExchangePackageDeliveriesItemStatus(d.pop("status"))

        def _parse_peer_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        peer_id = _parse_peer_id(d.pop("peerId", UNSET))

        def _parse_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                at_type_0 = datetime.datetime.fromisoformat(data)

                return at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        at = _parse_at(d.pop("at", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        exchange_package_deliveries_item = cls(
            instance_id=instance_id,
            status=status,
            peer_id=peer_id,
            at=at,
            error=error,
        )

        exchange_package_deliveries_item.additional_properties = d
        return exchange_package_deliveries_item

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
