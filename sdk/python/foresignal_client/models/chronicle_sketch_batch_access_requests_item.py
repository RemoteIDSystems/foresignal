from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleSketchBatchAccessRequestsItem")


@_attrs_define
class ChronicleSketchBatchAccessRequestsItem:
    """A relayed access request

    Attributes:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        sketch_id (str): The receiving instance's sketch id
        requester_handle (str):
        link_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        message (None | str | Unset):
        offer_share (bool | Unset): The requester offers to share a chronicle of its own
    """

    request_id: str
    sketch_id: str
    requester_handle: str
    link_id: str | Unset = UNSET
    message: None | str | Unset = UNSET
    offer_share: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        request_id = self.request_id

        sketch_id = self.sketch_id

        requester_handle = self.requester_handle

        link_id = self.link_id

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        offer_share = self.offer_share

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "requestId": request_id,
                "sketchId": sketch_id,
                "requesterHandle": requester_handle,
            }
        )
        if link_id is not UNSET:
            field_dict["linkId"] = link_id
        if message is not UNSET:
            field_dict["message"] = message
        if offer_share is not UNSET:
            field_dict["offerShare"] = offer_share

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        request_id = d.pop("requestId")

        sketch_id = d.pop("sketchId")

        requester_handle = d.pop("requesterHandle")

        link_id = d.pop("linkId", UNSET)

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        offer_share = d.pop("offerShare", UNSET)

        chronicle_sketch_batch_access_requests_item = cls(
            request_id=request_id,
            sketch_id=sketch_id,
            requester_handle=requester_handle,
            link_id=link_id,
            message=message,
            offer_share=offer_share,
        )

        chronicle_sketch_batch_access_requests_item.additional_properties = d
        return chronicle_sketch_batch_access_requests_item

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
