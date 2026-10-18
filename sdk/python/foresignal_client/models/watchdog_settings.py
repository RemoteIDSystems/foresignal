from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.watchdog_settings_method import WatchdogSettingsMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.watchdog_settings_canary import WatchdogSettingsCanary


T = TypeVar("T", bound="WatchdogSettings")


@_attrs_define
class WatchdogSettings:
    """Off-box dead-man watchdog (`app_setting['watchdog']`) and optional ingest canary

    Attributes:
        heartbeat_url (None | str | Unset): Pinged every interval while readiness is UP (Healthchecks.io, Uptime Kuma
            push)
        fail_url (None | str | Unset): Pinged instead while not ready (optional)
        interval (str | Unset): Ping interval (≥ 30s). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
            Default: '60s'. Example: 60s.
        method (WatchdogSettingsMethod | Unset):  Default: WatchdogSettingsMethod.GET.
        canary (WatchdogSettingsCanary | Unset):
    """

    heartbeat_url: None | str | Unset = UNSET
    fail_url: None | str | Unset = UNSET
    interval: str | Unset = "60s"
    method: WatchdogSettingsMethod | Unset = WatchdogSettingsMethod.GET
    canary: WatchdogSettingsCanary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        heartbeat_url: None | str | Unset
        if isinstance(self.heartbeat_url, Unset):
            heartbeat_url = UNSET
        else:
            heartbeat_url = self.heartbeat_url

        fail_url: None | str | Unset
        if isinstance(self.fail_url, Unset):
            fail_url = UNSET
        else:
            fail_url = self.fail_url

        interval = self.interval

        method: str | Unset = UNSET
        if not isinstance(self.method, Unset):
            method = self.method.value

        canary: dict[str, Any] | Unset = UNSET
        if not isinstance(self.canary, Unset):
            canary = self.canary.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if heartbeat_url is not UNSET:
            field_dict["heartbeatUrl"] = heartbeat_url
        if fail_url is not UNSET:
            field_dict["failUrl"] = fail_url
        if interval is not UNSET:
            field_dict["interval"] = interval
        if method is not UNSET:
            field_dict["method"] = method
        if canary is not UNSET:
            field_dict["canary"] = canary

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.watchdog_settings_canary import WatchdogSettingsCanary

        d = dict(src_dict)

        def _parse_heartbeat_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        heartbeat_url = _parse_heartbeat_url(d.pop("heartbeatUrl", UNSET))

        def _parse_fail_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        fail_url = _parse_fail_url(d.pop("failUrl", UNSET))

        interval = d.pop("interval", UNSET)

        _method = d.pop("method", UNSET)
        method: WatchdogSettingsMethod | Unset
        if isinstance(_method, Unset):
            method = UNSET
        else:
            method = WatchdogSettingsMethod(_method)

        _canary = d.pop("canary", UNSET)
        canary: WatchdogSettingsCanary | Unset
        if isinstance(_canary, Unset):
            canary = UNSET
        else:
            canary = WatchdogSettingsCanary.from_dict(_canary)

        watchdog_settings = cls(
            heartbeat_url=heartbeat_url,
            fail_url=fail_url,
            interval=interval,
            method=method,
            canary=canary,
        )

        watchdog_settings.additional_properties = d
        return watchdog_settings

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
