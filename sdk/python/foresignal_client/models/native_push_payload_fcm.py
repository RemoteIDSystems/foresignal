from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.native_push_payload_fcm_message import NativePushPayloadFcmMessage


T = TypeVar("T", bound="NativePushPayloadFcm")


@_attrs_define
class NativePushPayloadFcm:
    """FCM HTTP v1 message (data message; HIGH for warning/critical)

    Attributes:
        message (NativePushPayloadFcmMessage | Unset): FCM message
    """

    message: NativePushPayloadFcmMessage | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        message: dict[str, Any] | Unset = UNSET
        if not isinstance(self.message, Unset):
            message = self.message.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if message is not UNSET:
            field_dict["message"] = message

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.native_push_payload_fcm_message import NativePushPayloadFcmMessage

        d = dict(src_dict)
        _message = d.pop("message", UNSET)
        message: NativePushPayloadFcmMessage | Unset
        if isinstance(_message, Unset):
            message = UNSET
        else:
            message = NativePushPayloadFcmMessage.from_dict(_message)

        native_push_payload_fcm = cls(
            message=message,
        )

        native_push_payload_fcm.additional_properties = d
        return native_push_payload_fcm

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
