from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PushProviderInputSecrets")


@_attrs_define
class PushProviderInputSecrets:
    """Write-only secrets

    Attributes:
        p8 (str | Unset): APNs .p8 PEM (EC P-256 PKCS#8)
        service_account_json (str | Unset): FCM service-account JSON, as a string or an object; only private_key is
            stored, encrypted
    """

    p8: str | Unset = UNSET
    service_account_json: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p8 = self.p8

        service_account_json = self.service_account_json

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if p8 is not UNSET:
            field_dict["p8"] = p8
        if service_account_json is not UNSET:
            field_dict["serviceAccountJson"] = service_account_json

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        p8 = d.pop("p8", UNSET)

        service_account_json = d.pop("serviceAccountJson", UNSET)

        push_provider_input_secrets = cls(
            p8=p8,
            service_account_json=service_account_json,
        )

        push_provider_input_secrets.additional_properties = d
        return push_provider_input_secrets

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
