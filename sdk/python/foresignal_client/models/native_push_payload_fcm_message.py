from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.native_push_payload_fcm_message_android import (
        NativePushPayloadFcmMessageAndroid,
    )
    from ..models.native_push_payload_fcm_message_apns import (
        NativePushPayloadFcmMessageApns,
    )
    from ..models.native_push_payload_fcm_message_data import (
        NativePushPayloadFcmMessageData,
    )


T = TypeVar("T", bound="NativePushPayloadFcmMessage")


@_attrs_define
class NativePushPayloadFcmMessage:
    """FCM message

    Attributes:
        token (str): The FCM registration token (the v1 API's discovery document marks `token` deprecated in favor of
            `fid` but still accepts it)
        data (NativePushPayloadFcmMessageData): String-valued data
        android (NativePushPayloadFcmMessageAndroid | Unset): Android options
        apns (NativePushPayloadFcmMessageApns | Unset): For iOS/macOS devices registered via FCM: the APNs headers and
            aps payload
    """

    token: str
    data: NativePushPayloadFcmMessageData
    android: NativePushPayloadFcmMessageAndroid | Unset = UNSET
    apns: NativePushPayloadFcmMessageApns | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        token = self.token

        data = self.data.to_dict()

        android: dict[str, Any] | Unset = UNSET
        if not isinstance(self.android, Unset):
            android = self.android.to_dict()

        apns: dict[str, Any] | Unset = UNSET
        if not isinstance(self.apns, Unset):
            apns = self.apns.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "token": token,
                "data": data,
            }
        )
        if android is not UNSET:
            field_dict["android"] = android
        if apns is not UNSET:
            field_dict["apns"] = apns

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.native_push_payload_fcm_message_android import (
            NativePushPayloadFcmMessageAndroid,
        )
        from ..models.native_push_payload_fcm_message_apns import (
            NativePushPayloadFcmMessageApns,
        )
        from ..models.native_push_payload_fcm_message_data import (
            NativePushPayloadFcmMessageData,
        )

        d = dict(src_dict)
        token = d.pop("token")

        data = NativePushPayloadFcmMessageData.from_dict(d.pop("data"))

        _android = d.pop("android", UNSET)
        android: NativePushPayloadFcmMessageAndroid | Unset
        if isinstance(_android, Unset):
            android = UNSET
        else:
            android = NativePushPayloadFcmMessageAndroid.from_dict(_android)

        _apns = d.pop("apns", UNSET)
        apns: NativePushPayloadFcmMessageApns | Unset
        if isinstance(_apns, Unset):
            apns = UNSET
        else:
            apns = NativePushPayloadFcmMessageApns.from_dict(_apns)

        native_push_payload_fcm_message = cls(
            token=token,
            data=data,
            android=android,
            apns=apns,
        )

        native_push_payload_fcm_message.additional_properties = d
        return native_push_payload_fcm_message

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
