from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.package_received_event_signature_status import (
    PackageReceivedEventSignatureStatus,
)
from ..models.package_received_event_type import PackageReceivedEventType
from ..models.tlp import Tlp
from ..types import UNSET, Unset

T = TypeVar("T", bound="PackageReceivedEvent")


@_attrs_define
class PackageReceivedEvent:
    """SSE `package.received` on the `me` topic

    Attributes:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (PackageReceivedEventType):
        sender_instance_id (str):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        at (datetime.datetime):
        title (str | Unset):
        signature_status (PackageReceivedEventSignatureStatus | Unset):
    """

    received_id: str
    package_id: str
    type_: PackageReceivedEventType
    sender_instance_id: str
    tlp: Tlp
    at: datetime.datetime
    title: str | Unset = UNSET
    signature_status: PackageReceivedEventSignatureStatus | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        received_id = self.received_id

        package_id = self.package_id

        type_ = self.type_.value

        sender_instance_id = self.sender_instance_id

        tlp = self.tlp.value

        at = self.at.isoformat()

        title = self.title

        signature_status: str | Unset = UNSET
        if not isinstance(self.signature_status, Unset):
            signature_status = self.signature_status.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "receivedId": received_id,
                "packageId": package_id,
                "type": type_,
                "senderInstanceId": sender_instance_id,
                "tlp": tlp,
                "at": at,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if signature_status is not UNSET:
            field_dict["signatureStatus"] = signature_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        received_id = d.pop("receivedId")

        package_id = d.pop("packageId")

        type_ = PackageReceivedEventType(d.pop("type"))

        sender_instance_id = d.pop("senderInstanceId")

        tlp = Tlp(d.pop("tlp"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        title = d.pop("title", UNSET)

        _signature_status = d.pop("signatureStatus", UNSET)
        signature_status: PackageReceivedEventSignatureStatus | Unset
        if isinstance(_signature_status, Unset):
            signature_status = UNSET
        else:
            signature_status = PackageReceivedEventSignatureStatus(_signature_status)

        package_received_event = cls(
            received_id=received_id,
            package_id=package_id,
            type_=type_,
            sender_instance_id=sender_instance_id,
            tlp=tlp,
            at=at,
            title=title,
            signature_status=signature_status,
        )

        package_received_event.additional_properties = d
        return package_received_event

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
