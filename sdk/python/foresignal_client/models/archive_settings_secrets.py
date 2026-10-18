from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ArchiveSettingsSecrets")


@_attrs_define
class ArchiveSettingsSecrets:
    """Write-only S3 credentials

    Attributes:
        access_key (str | Unset):
        secret_key (str | Unset):
    """

    access_key: str | Unset = UNSET
    secret_key: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        access_key = self.access_key

        secret_key = self.secret_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if access_key is not UNSET:
            field_dict["accessKey"] = access_key
        if secret_key is not UNSET:
            field_dict["secretKey"] = secret_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        access_key = d.pop("accessKey", UNSET)

        secret_key = d.pop("secretKey", UNSET)

        archive_settings_secrets = cls(
            access_key=access_key,
            secret_key=secret_key,
        )

        archive_settings_secrets.additional_properties = d
        return archive_settings_secrets

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
