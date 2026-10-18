from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RateLimitSettingsStream")


@_attrs_define
class RateLimitSettingsStream:
    """SSE limits

    Attributes:
        opens_per_minute (int | Unset):  Default: 30.
        concurrent (int | Unset):  Default: 8.
    """

    opens_per_minute: int | Unset = 30
    concurrent: int | Unset = 8
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        opens_per_minute = self.opens_per_minute

        concurrent = self.concurrent

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if opens_per_minute is not UNSET:
            field_dict["opensPerMinute"] = opens_per_minute
        if concurrent is not UNSET:
            field_dict["concurrent"] = concurrent

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        opens_per_minute = d.pop("opensPerMinute", UNSET)

        concurrent = d.pop("concurrent", UNSET)

        rate_limit_settings_stream = cls(
            opens_per_minute=opens_per_minute,
            concurrent=concurrent,
        )

        rate_limit_settings_stream.additional_properties = d
        return rate_limit_settings_stream

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
