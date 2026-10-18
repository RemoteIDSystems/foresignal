from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.native_push_payload_apns import NativePushPayloadApns
    from ..models.native_push_payload_fcm import NativePushPayloadFcm


T = TypeVar("T", bound="NativePushPayload")


@_attrs_define
class NativePushPayload:
    """Wire payloads of native pushes (032): visible alerts carrying ids, severity and minimal text only

    Attributes:
        apns (NativePushPayloadApns | Unset): APNs request body (≤ 4096 bytes); headers apns-push-type alert, apns-
            priority 10 (5 under quiet-hours downgrade), apns-expiration, apns-collapse-id, apns-id
        fcm (NativePushPayloadFcm | Unset): FCM HTTP v1 message (data message; HIGH for warning/critical)
        exercise (bool | Unset): Exercise; never time-sensitive or critical Default: False.
    """

    apns: NativePushPayloadApns | Unset = UNSET
    fcm: NativePushPayloadFcm | Unset = UNSET
    exercise: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        apns: dict[str, Any] | Unset = UNSET
        if not isinstance(self.apns, Unset):
            apns = self.apns.to_dict()

        fcm: dict[str, Any] | Unset = UNSET
        if not isinstance(self.fcm, Unset):
            fcm = self.fcm.to_dict()

        exercise = self.exercise

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if apns is not UNSET:
            field_dict["apns"] = apns
        if fcm is not UNSET:
            field_dict["fcm"] = fcm
        if exercise is not UNSET:
            field_dict["exercise"] = exercise

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.native_push_payload_apns import NativePushPayloadApns
        from ..models.native_push_payload_fcm import NativePushPayloadFcm

        d = dict(src_dict)
        _apns = d.pop("apns", UNSET)
        apns: NativePushPayloadApns | Unset
        if isinstance(_apns, Unset):
            apns = UNSET
        else:
            apns = NativePushPayloadApns.from_dict(_apns)

        _fcm = d.pop("fcm", UNSET)
        fcm: NativePushPayloadFcm | Unset
        if isinstance(_fcm, Unset):
            fcm = UNSET
        else:
            fcm = NativePushPayloadFcm.from_dict(_fcm)

        exercise = d.pop("exercise", UNSET)

        native_push_payload = cls(
            apns=apns,
            fcm=fcm,
            exercise=exercise,
        )

        native_push_payload.additional_properties = d
        return native_push_payload

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
