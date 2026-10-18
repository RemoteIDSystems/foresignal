from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.rate_limit_settings_stream import RateLimitSettingsStream


T = TypeVar("T", bound="RateLimitSettings")


@_attrs_define
class RateLimitSettings:
    """Per-client rate limits per class, requests per minute (035). Key = token id, else feed-key id, else client IP
    (trusted proxies only).

        Attributes:
            read (int | Unset):  Default: 600.
            tiles (int | Unset):  Default: 1200.
            stream (RateLimitSettingsStream | Unset): SSE limits
            expensive (int | Unset): search, Ask, LLM-backed MCP tools, /v1/sample Default: 20.
            write (int | Unset):  Default: 120.
            anonymous (int | Unset): Per IP Default: 120.
            metrics_per_minute (int | None | Unset): Optional limit for /actuator/prometheus by IP
    """

    read: int | Unset = 600
    tiles: int | Unset = 1200
    stream: RateLimitSettingsStream | Unset = UNSET
    expensive: int | Unset = 20
    write: int | Unset = 120
    anonymous: int | Unset = 120
    metrics_per_minute: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        read = self.read

        tiles = self.tiles

        stream: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stream, Unset):
            stream = self.stream.to_dict()

        expensive = self.expensive

        write = self.write

        anonymous = self.anonymous

        metrics_per_minute: int | None | Unset
        if isinstance(self.metrics_per_minute, Unset):
            metrics_per_minute = UNSET
        else:
            metrics_per_minute = self.metrics_per_minute

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if read is not UNSET:
            field_dict["read"] = read
        if tiles is not UNSET:
            field_dict["tiles"] = tiles
        if stream is not UNSET:
            field_dict["stream"] = stream
        if expensive is not UNSET:
            field_dict["expensive"] = expensive
        if write is not UNSET:
            field_dict["write"] = write
        if anonymous is not UNSET:
            field_dict["anonymous"] = anonymous
        if metrics_per_minute is not UNSET:
            field_dict["metricsPerMinute"] = metrics_per_minute

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.rate_limit_settings_stream import RateLimitSettingsStream

        d = dict(src_dict)
        read = d.pop("read", UNSET)

        tiles = d.pop("tiles", UNSET)

        _stream = d.pop("stream", UNSET)
        stream: RateLimitSettingsStream | Unset
        if isinstance(_stream, Unset):
            stream = UNSET
        else:
            stream = RateLimitSettingsStream.from_dict(_stream)

        expensive = d.pop("expensive", UNSET)

        write = d.pop("write", UNSET)

        anonymous = d.pop("anonymous", UNSET)

        def _parse_metrics_per_minute(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        metrics_per_minute = _parse_metrics_per_minute(d.pop("metricsPerMinute", UNSET))

        rate_limit_settings = cls(
            read=read,
            tiles=tiles,
            stream=stream,
            expensive=expensive,
            write=write,
            anonymous=anonymous,
            metrics_per_minute=metrics_per_minute,
        )

        rate_limit_settings.additional_properties = d
        return rate_limit_settings

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
