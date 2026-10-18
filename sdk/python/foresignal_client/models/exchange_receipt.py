from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exchange_receipt_type import ExchangeReceiptType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExchangeReceipt")


@_attrs_define
class ExchangeReceipt:
    """A signed delivery or acknowledgement receipt exchanged with the sending instance

    Attributes:
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (ExchangeReceiptType):
        receiver_instance_id (str):
        at (datetime.datetime):
        principal_handle (None | str | Unset):
    """

    package_id: str
    type_: ExchangeReceiptType
    receiver_instance_id: str
    at: datetime.datetime
    principal_handle: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        package_id = self.package_id

        type_ = self.type_.value

        receiver_instance_id = self.receiver_instance_id

        at = self.at.isoformat()

        principal_handle: None | str | Unset
        if isinstance(self.principal_handle, Unset):
            principal_handle = UNSET
        else:
            principal_handle = self.principal_handle

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "packageId": package_id,
                "type": type_,
                "receiverInstanceId": receiver_instance_id,
                "at": at,
            }
        )
        if principal_handle is not UNSET:
            field_dict["principalHandle"] = principal_handle

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        package_id = d.pop("packageId")

        type_ = ExchangeReceiptType(d.pop("type"))

        receiver_instance_id = d.pop("receiverInstanceId")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_principal_handle(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal_handle = _parse_principal_handle(d.pop("principalHandle", UNSET))

        exchange_receipt = cls(
            package_id=package_id,
            type_=type_,
            receiver_instance_id=receiver_instance_id,
            at=at,
            principal_handle=principal_handle,
        )

        exchange_receipt.additional_properties = d
        return exchange_receipt

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
