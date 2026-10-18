from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.native_push_payload_apns_aps import NativePushPayloadApnsAps
    from ..models.native_push_payload_apns_e import NativePushPayloadApnsE


T = TypeVar("T", bound="NativePushPayloadApns")


@_attrs_define
class NativePushPayloadApns:
    """APNs request body (≤ 4096 bytes); headers apns-push-type alert, apns-priority 10 (5 under quiet-hours downgrade),
    apns-expiration, apns-collapse-id, apns-id

        Attributes:
            aps (NativePushPayloadApnsAps | Unset): APNs aps dictionary
            e (NativePushPayloadApnsE | Unset): Foresignal ids
    """

    aps: NativePushPayloadApnsAps | Unset = UNSET
    e: NativePushPayloadApnsE | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        aps: dict[str, Any] | Unset = UNSET
        if not isinstance(self.aps, Unset):
            aps = self.aps.to_dict()

        e: dict[str, Any] | Unset = UNSET
        if not isinstance(self.e, Unset):
            e = self.e.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if aps is not UNSET:
            field_dict["aps"] = aps
        if e is not UNSET:
            field_dict["e"] = e

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.native_push_payload_apns_aps import NativePushPayloadApnsAps
        from ..models.native_push_payload_apns_e import NativePushPayloadApnsE

        d = dict(src_dict)
        _aps = d.pop("aps", UNSET)
        aps: NativePushPayloadApnsAps | Unset
        if isinstance(_aps, Unset):
            aps = UNSET
        else:
            aps = NativePushPayloadApnsAps.from_dict(_aps)

        _e = d.pop("e", UNSET)
        e: NativePushPayloadApnsE | Unset
        if isinstance(_e, Unset):
            e = UNSET
        else:
            e = NativePushPayloadApnsE.from_dict(_e)

        native_push_payload_apns = cls(
            aps=aps,
            e=e,
        )

        native_push_payload_apns.additional_properties = d
        return native_push_payload_apns

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
