from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.budget import Budget
    from ..models.social_platform_price import SocialPlatformPrice
    from ..models.social_platform_settings_options import SocialPlatformSettingsOptions
    from ..models.social_platform_settings_post_retention import (
        SocialPlatformSettingsPostRetention,
    )


T = TypeVar("T", bound="SocialPlatformSettings")


@_attrs_define
class SocialPlatformSettings:
    """Operator settings of one social platform adapter; replace semantics (135). Credentials use the existing source
    credential endpoints (013)

        Attributes:
            enabled (bool):
            mode (None | str | Unset): Adapter mode: x `counts` | `search` | `counts+search`; others null Example: counts.
            channels (list[str] | Unset): Channels polled regardless of topics: subreddits, Mastodon instances or hashtags,
                YouTube channels
            deletion_sync (bool | Unset): Default on for X, Bluesky and Threads
            post_retention (SocialPlatformSettingsPostRetention | Unset): Retention of this platform's posts
                (`social.postRetention`, default 30d)
            budget (Budget | None | Unset): Call budget (005)
            monthly_cap_usd (float | None | Unset): Money cap for paid platforms (X default 25)
            prices (list[SocialPlatformPrice] | Unset): X `social.x.prices`, seeded from the pricing page with its date
            prices_as_of (datetime.date | None | Unset):
            options (SocialPlatformSettingsOptions | Unset): Platform-specific options
    """

    enabled: bool
    mode: None | str | Unset = UNSET
    channels: list[str] | Unset = UNSET
    deletion_sync: bool | Unset = UNSET
    post_retention: SocialPlatformSettingsPostRetention | Unset = UNSET
    budget: Budget | None | Unset = UNSET
    monthly_cap_usd: float | None | Unset = UNSET
    prices: list[SocialPlatformPrice] | Unset = UNSET
    prices_as_of: datetime.date | None | Unset = UNSET
    options: SocialPlatformSettingsOptions | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.budget import Budget

        enabled = self.enabled

        mode: None | str | Unset
        if isinstance(self.mode, Unset):
            mode = UNSET
        else:
            mode = self.mode

        channels: list[str] | Unset = UNSET
        if not isinstance(self.channels, Unset):
            channels = self.channels

        deletion_sync = self.deletion_sync

        post_retention: dict[str, Any] | Unset = UNSET
        if not isinstance(self.post_retention, Unset):
            post_retention = self.post_retention.to_dict()

        budget: dict[str, Any] | None | Unset
        if isinstance(self.budget, Unset):
            budget = UNSET
        elif isinstance(self.budget, Budget):
            budget = self.budget.to_dict()
        else:
            budget = self.budget

        monthly_cap_usd: float | None | Unset
        if isinstance(self.monthly_cap_usd, Unset):
            monthly_cap_usd = UNSET
        else:
            monthly_cap_usd = self.monthly_cap_usd

        prices: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.prices, Unset):
            prices = []
            for prices_item_data in self.prices:
                prices_item = prices_item_data.to_dict()
                prices.append(prices_item)

        prices_as_of: None | str | Unset
        if isinstance(self.prices_as_of, Unset):
            prices_as_of = UNSET
        elif isinstance(self.prices_as_of, datetime.date):
            prices_as_of = self.prices_as_of.isoformat()
        else:
            prices_as_of = self.prices_as_of

        options: dict[str, Any] | Unset = UNSET
        if not isinstance(self.options, Unset):
            options = self.options.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
            }
        )
        if mode is not UNSET:
            field_dict["mode"] = mode
        if channels is not UNSET:
            field_dict["channels"] = channels
        if deletion_sync is not UNSET:
            field_dict["deletionSync"] = deletion_sync
        if post_retention is not UNSET:
            field_dict["postRetention"] = post_retention
        if budget is not UNSET:
            field_dict["budget"] = budget
        if monthly_cap_usd is not UNSET:
            field_dict["monthlyCapUsd"] = monthly_cap_usd
        if prices is not UNSET:
            field_dict["prices"] = prices
        if prices_as_of is not UNSET:
            field_dict["pricesAsOf"] = prices_as_of
        if options is not UNSET:
            field_dict["options"] = options

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.budget import Budget
        from ..models.social_platform_price import SocialPlatformPrice
        from ..models.social_platform_settings_options import (
            SocialPlatformSettingsOptions,
        )
        from ..models.social_platform_settings_post_retention import (
            SocialPlatformSettingsPostRetention,
        )

        d = dict(src_dict)
        enabled = d.pop("enabled")

        def _parse_mode(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        mode = _parse_mode(d.pop("mode", UNSET))

        channels = cast(list[str], d.pop("channels", UNSET))

        deletion_sync = d.pop("deletionSync", UNSET)

        _post_retention = d.pop("postRetention", UNSET)
        post_retention: SocialPlatformSettingsPostRetention | Unset
        if isinstance(_post_retention, Unset):
            post_retention = UNSET
        else:
            post_retention = SocialPlatformSettingsPostRetention.from_dict(
                _post_retention
            )

        def _parse_budget(data: object) -> Budget | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                budget_type_0 = Budget.from_dict(data)

                return budget_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Budget | None | Unset, data)

        budget = _parse_budget(d.pop("budget", UNSET))

        def _parse_monthly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        monthly_cap_usd = _parse_monthly_cap_usd(d.pop("monthlyCapUsd", UNSET))

        _prices = d.pop("prices", UNSET)
        prices: list[SocialPlatformPrice] | Unset = UNSET
        if _prices is not UNSET:
            prices = []
            for prices_item_data in _prices:
                prices_item = SocialPlatformPrice.from_dict(prices_item_data)

                prices.append(prices_item)

        def _parse_prices_as_of(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                prices_as_of_type_0 = datetime.date.fromisoformat(data)

                return prices_as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        prices_as_of = _parse_prices_as_of(d.pop("pricesAsOf", UNSET))

        _options = d.pop("options", UNSET)
        options: SocialPlatformSettingsOptions | Unset
        if isinstance(_options, Unset):
            options = UNSET
        else:
            options = SocialPlatformSettingsOptions.from_dict(_options)

        social_platform_settings = cls(
            enabled=enabled,
            mode=mode,
            channels=channels,
            deletion_sync=deletion_sync,
            post_retention=post_retention,
            budget=budget,
            monthly_cap_usd=monthly_cap_usd,
            prices=prices,
            prices_as_of=prices_as_of,
            options=options,
        )

        social_platform_settings.additional_properties = d
        return social_platform_settings

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
