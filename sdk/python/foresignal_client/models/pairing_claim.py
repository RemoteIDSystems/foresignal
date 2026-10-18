from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.device_info import DeviceInfo


T = TypeVar("T", bound="PairingClaim")


@_attrs_define
class PairingClaim:
    """Claim a pairing code from a device (023)

    Attributes:
        code (str): XXXX-XXXX (the dash is optional; case-insensitive)
        device (DeviceInfo): Device metadata a client app reports for its own token (023)
    """

    code: str
    device: DeviceInfo
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        code = self.code

        device = self.device.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "code": code,
                "device": device,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.device_info import DeviceInfo

        d = dict(src_dict)
        code = d.pop("code")

        device = DeviceInfo.from_dict(d.pop("device"))

        pairing_claim = cls(
            code=code,
            device=device,
        )

        pairing_claim.additional_properties = d
        return pairing_claim

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
