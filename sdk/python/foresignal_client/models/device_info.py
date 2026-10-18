from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.device_info_platform import DeviceInfoPlatform
from ..types import UNSET, Unset

T = TypeVar("T", bound="DeviceInfo")


@_attrs_define
class DeviceInfo:
    """Device metadata a client app reports for its own token (023)

    Attributes:
        device_name (str):
        platform (DeviceInfoPlatform):
        app_version (None | str | Unset):
        reported_at (datetime.datetime | None | Unset):
    """

    device_name: str
    platform: DeviceInfoPlatform
    app_version: None | str | Unset = UNSET
    reported_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        device_name = self.device_name

        platform = self.platform.value

        app_version: None | str | Unset
        if isinstance(self.app_version, Unset):
            app_version = UNSET
        else:
            app_version = self.app_version

        reported_at: None | str | Unset
        if isinstance(self.reported_at, Unset):
            reported_at = UNSET
        elif isinstance(self.reported_at, datetime.datetime):
            reported_at = self.reported_at.isoformat()
        else:
            reported_at = self.reported_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "deviceName": device_name,
                "platform": platform,
            }
        )
        if app_version is not UNSET:
            field_dict["appVersion"] = app_version
        if reported_at is not UNSET:
            field_dict["reportedAt"] = reported_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        device_name = d.pop("deviceName")

        platform = DeviceInfoPlatform(d.pop("platform"))

        def _parse_app_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        app_version = _parse_app_version(d.pop("appVersion", UNSET))

        def _parse_reported_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reported_at_type_0 = datetime.datetime.fromisoformat(data)

                return reported_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        reported_at = _parse_reported_at(d.pop("reportedAt", UNSET))

        device_info = cls(
            device_name=device_name,
            platform=platform,
            app_version=app_version,
            reported_at=reported_at,
        )

        device_info.additional_properties = d
        return device_info

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
