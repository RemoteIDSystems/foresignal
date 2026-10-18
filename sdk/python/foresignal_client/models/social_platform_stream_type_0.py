from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_platform_stream_type_0_last_gap_type_0 import (
        SocialPlatformStreamType0LastGapType0,
    )


T = TypeVar("T", bound="SocialPlatformStreamType0")


@_attrs_define
class SocialPlatformStreamType0:
    """Streaming state

    Attributes:
        lag_seconds (float | None | Unset):
        cursor (None | str | Unset): Jetstream `time_us` or platform stream cursor
        connected_since (datetime.datetime | None | Unset):
        last_gap (None | SocialPlatformStreamType0LastGapType0 | Unset):
    """

    lag_seconds: float | None | Unset = UNSET
    cursor: None | str | Unset = UNSET
    connected_since: datetime.datetime | None | Unset = UNSET
    last_gap: None | SocialPlatformStreamType0LastGapType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.social_platform_stream_type_0_last_gap_type_0 import (
            SocialPlatformStreamType0LastGapType0,
        )

        lag_seconds: float | None | Unset
        if isinstance(self.lag_seconds, Unset):
            lag_seconds = UNSET
        else:
            lag_seconds = self.lag_seconds

        cursor: None | str | Unset
        if isinstance(self.cursor, Unset):
            cursor = UNSET
        else:
            cursor = self.cursor

        connected_since: None | str | Unset
        if isinstance(self.connected_since, Unset):
            connected_since = UNSET
        elif isinstance(self.connected_since, datetime.datetime):
            connected_since = self.connected_since.isoformat()
        else:
            connected_since = self.connected_since

        last_gap: dict[str, Any] | None | Unset
        if isinstance(self.last_gap, Unset):
            last_gap = UNSET
        elif isinstance(self.last_gap, SocialPlatformStreamType0LastGapType0):
            last_gap = self.last_gap.to_dict()
        else:
            last_gap = self.last_gap

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if lag_seconds is not UNSET:
            field_dict["lagSeconds"] = lag_seconds
        if cursor is not UNSET:
            field_dict["cursor"] = cursor
        if connected_since is not UNSET:
            field_dict["connectedSince"] = connected_since
        if last_gap is not UNSET:
            field_dict["lastGap"] = last_gap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_platform_stream_type_0_last_gap_type_0 import (
            SocialPlatformStreamType0LastGapType0,
        )

        d = dict(src_dict)

        def _parse_lag_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lag_seconds = _parse_lag_seconds(d.pop("lagSeconds", UNSET))

        def _parse_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cursor = _parse_cursor(d.pop("cursor", UNSET))

        def _parse_connected_since(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                connected_since_type_0 = datetime.datetime.fromisoformat(data)

                return connected_since_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        connected_since = _parse_connected_since(d.pop("connectedSince", UNSET))

        def _parse_last_gap(
            data: object,
        ) -> None | SocialPlatformStreamType0LastGapType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                last_gap_type_0 = SocialPlatformStreamType0LastGapType0.from_dict(data)

                return last_gap_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialPlatformStreamType0LastGapType0 | Unset, data)

        last_gap = _parse_last_gap(d.pop("lastGap", UNSET))

        social_platform_stream_type_0 = cls(
            lag_seconds=lag_seconds,
            cursor=cursor,
            connected_since=connected_since,
            last_gap=last_gap,
        )

        social_platform_stream_type_0.additional_properties = d
        return social_platform_stream_type_0

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
