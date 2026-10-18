from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_settings_track_record_overrides_item_reliability import (
    SocialSettingsTrackRecordOverridesItemReliability,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSettingsTrackRecordOverridesItem")


@_attrs_define
class SocialSettingsTrackRecordOverridesItem:
    """Operator override per account or channel

    Attributes:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        reliability (SocialSettingsTrackRecordOverridesItemReliability):
        account (None | str | Unset): Author key (`authorKey` of the account, as posts expose it; 136 keys overrides by
            author key, not handle)
        channel (None | str | Unset):
        note (None | str | Unset):
    """

    platform: str
    reliability: SocialSettingsTrackRecordOverridesItemReliability
    account: None | str | Unset = UNSET
    channel: None | str | Unset = UNSET
    note: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform

        reliability = self.reliability.value

        account: None | str | Unset
        if isinstance(self.account, Unset):
            account = UNSET
        else:
            account = self.account

        channel: None | str | Unset
        if isinstance(self.channel, Unset):
            channel = UNSET
        else:
            channel = self.channel

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "reliability": reliability,
            }
        )
        if account is not UNSET:
            field_dict["account"] = account
        if channel is not UNSET:
            field_dict["channel"] = channel
        if note is not UNSET:
            field_dict["note"] = note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        platform = d.pop("platform")

        reliability = SocialSettingsTrackRecordOverridesItemReliability(
            d.pop("reliability")
        )

        def _parse_account(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        account = _parse_account(d.pop("account", UNSET))

        def _parse_channel(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        channel = _parse_channel(d.pop("channel", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        social_settings_track_record_overrides_item = cls(
            platform=platform,
            reliability=reliability,
            account=account,
            channel=channel,
            note=note,
        )

        social_settings_track_record_overrides_item.additional_properties = d
        return social_settings_track_record_overrides_item

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
