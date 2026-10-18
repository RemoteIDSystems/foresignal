from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_access_event_status import ChronicleAccessEventStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleAccessEvent")


@_attrs_define
class ChronicleAccessEvent:
    """SSE `chronicle.access.requested`, `chronicle.access.granted` and `chronicle.access.declined` on the `me` topic

    Attributes:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ChronicleAccessEventStatus):
        at (datetime.datetime):
        requester_handle (None | str | Unset): Only on `requested`
        message (None | str | Unset): Only on `requested`
        chronicle_id (None | str | Unset):
    """

    request_id: str
    link_id: str
    status: ChronicleAccessEventStatus
    at: datetime.datetime
    requester_handle: None | str | Unset = UNSET
    message: None | str | Unset = UNSET
    chronicle_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        request_id = self.request_id

        link_id = self.link_id

        status = self.status.value

        at = self.at.isoformat()

        requester_handle: None | str | Unset
        if isinstance(self.requester_handle, Unset):
            requester_handle = UNSET
        else:
            requester_handle = self.requester_handle

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        chronicle_id: None | str | Unset
        if isinstance(self.chronicle_id, Unset):
            chronicle_id = UNSET
        else:
            chronicle_id = self.chronicle_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "requestId": request_id,
                "linkId": link_id,
                "status": status,
                "at": at,
            }
        )
        if requester_handle is not UNSET:
            field_dict["requesterHandle"] = requester_handle
        if message is not UNSET:
            field_dict["message"] = message
        if chronicle_id is not UNSET:
            field_dict["chronicleId"] = chronicle_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        request_id = d.pop("requestId")

        link_id = d.pop("linkId")

        status = ChronicleAccessEventStatus(d.pop("status"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_requester_handle(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        requester_handle = _parse_requester_handle(d.pop("requesterHandle", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_chronicle_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        chronicle_id = _parse_chronicle_id(d.pop("chronicleId", UNSET))

        chronicle_access_event = cls(
            request_id=request_id,
            link_id=link_id,
            status=status,
            at=at,
            requester_handle=requester_handle,
            message=message,
            chronicle_id=chronicle_id,
        )

        chronicle_access_event.additional_properties = d
        return chronicle_access_event

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
