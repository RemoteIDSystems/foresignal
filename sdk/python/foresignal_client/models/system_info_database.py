from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_info_database_status import SystemInfoDatabaseStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoDatabase")


@_attrs_define
class SystemInfoDatabase:
    """
    Attributes:
        status (SystemInfoDatabaseStatus | Unset):
        size_bytes (int | Unset):
        postgis_version (str | Unset):
    """

    status: SystemInfoDatabaseStatus | Unset = UNSET
    size_bytes: int | Unset = UNSET
    postgis_version: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        size_bytes = self.size_bytes

        postgis_version = self.postgis_version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if status is not UNSET:
            field_dict["status"] = status
        if size_bytes is not UNSET:
            field_dict["sizeBytes"] = size_bytes
        if postgis_version is not UNSET:
            field_dict["postgisVersion"] = postgis_version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _status = d.pop("status", UNSET)
        status: SystemInfoDatabaseStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = SystemInfoDatabaseStatus(_status)

        size_bytes = d.pop("sizeBytes", UNSET)

        postgis_version = d.pop("postgisVersion", UNSET)

        system_info_database = cls(
            status=status,
            size_bytes=size_bytes,
            postgis_version=postgis_version,
        )

        system_info_database.additional_properties = d
        return system_info_database

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
