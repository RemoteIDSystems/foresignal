from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="Quota")


@_attrs_define
class Quota:
    """Per-user limits so one user cannot exhaust the instance (D5/189, R2-026). In a user's quota a null member uses the
    instance default; unset there too it is unlimited. `0` allows nothing.

        Attributes:
            api_rate_limit_per_minute (int | None | Unset): Requests a minute across the user's sessions and own API tokens;
                over it is 429 RATE_LIMITED
            max_storage_bytes (int | None | Unset): Bytes of uploaded attachments plus the JSON of owned workspaces
            max_alerts (int | None | Unset): Trigger rules the user owns
            max_channels (int | None | Unset): Notification channels the user owns
            max_saved_views (int | None | Unset): Saved views the user owns
    """

    api_rate_limit_per_minute: int | None | Unset = UNSET
    max_storage_bytes: int | None | Unset = UNSET
    max_alerts: int | None | Unset = UNSET
    max_channels: int | None | Unset = UNSET
    max_saved_views: int | None | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        api_rate_limit_per_minute: int | None | Unset
        if isinstance(self.api_rate_limit_per_minute, Unset):
            api_rate_limit_per_minute = UNSET
        else:
            api_rate_limit_per_minute = self.api_rate_limit_per_minute

        max_storage_bytes: int | None | Unset
        if isinstance(self.max_storage_bytes, Unset):
            max_storage_bytes = UNSET
        else:
            max_storage_bytes = self.max_storage_bytes

        max_alerts: int | None | Unset
        if isinstance(self.max_alerts, Unset):
            max_alerts = UNSET
        else:
            max_alerts = self.max_alerts

        max_channels: int | None | Unset
        if isinstance(self.max_channels, Unset):
            max_channels = UNSET
        else:
            max_channels = self.max_channels

        max_saved_views: int | None | Unset
        if isinstance(self.max_saved_views, Unset):
            max_saved_views = UNSET
        else:
            max_saved_views = self.max_saved_views

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if api_rate_limit_per_minute is not UNSET:
            field_dict["apiRateLimitPerMinute"] = api_rate_limit_per_minute
        if max_storage_bytes is not UNSET:
            field_dict["maxStorageBytes"] = max_storage_bytes
        if max_alerts is not UNSET:
            field_dict["maxAlerts"] = max_alerts
        if max_channels is not UNSET:
            field_dict["maxChannels"] = max_channels
        if max_saved_views is not UNSET:
            field_dict["maxSavedViews"] = max_saved_views

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_api_rate_limit_per_minute(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        api_rate_limit_per_minute = _parse_api_rate_limit_per_minute(
            d.pop("apiRateLimitPerMinute", UNSET)
        )

        def _parse_max_storage_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_storage_bytes = _parse_max_storage_bytes(d.pop("maxStorageBytes", UNSET))

        def _parse_max_alerts(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_alerts = _parse_max_alerts(d.pop("maxAlerts", UNSET))

        def _parse_max_channels(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_channels = _parse_max_channels(d.pop("maxChannels", UNSET))

        def _parse_max_saved_views(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_saved_views = _parse_max_saved_views(d.pop("maxSavedViews", UNSET))

        quota = cls(
            api_rate_limit_per_minute=api_rate_limit_per_minute,
            max_storage_bytes=max_storage_bytes,
            max_alerts=max_alerts,
            max_channels=max_channels,
            max_saved_views=max_saved_views,
        )

        return quota
