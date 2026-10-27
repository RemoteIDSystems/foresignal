from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RetentionPreviewDbCapType0")


@_attrs_define
class RetentionPreviewDbCapType0:
    """The database-size cap as the next run would find it; null when it is off

    Attributes:
        cap_bytes (int | Unset):
        size_bytes (int | Unset):
        exceeded (bool | Unset):
        window_days (int | None | Unset): Window the cap has stepped down to, or null
        overwrite_oldest (bool | Unset):
    """

    cap_bytes: int | Unset = UNSET
    size_bytes: int | Unset = UNSET
    exceeded: bool | Unset = UNSET
    window_days: int | None | Unset = UNSET
    overwrite_oldest: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cap_bytes = self.cap_bytes

        size_bytes = self.size_bytes

        exceeded = self.exceeded

        window_days: int | None | Unset
        if isinstance(self.window_days, Unset):
            window_days = UNSET
        else:
            window_days = self.window_days

        overwrite_oldest = self.overwrite_oldest

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if cap_bytes is not UNSET:
            field_dict["capBytes"] = cap_bytes
        if size_bytes is not UNSET:
            field_dict["sizeBytes"] = size_bytes
        if exceeded is not UNSET:
            field_dict["exceeded"] = exceeded
        if window_days is not UNSET:
            field_dict["windowDays"] = window_days
        if overwrite_oldest is not UNSET:
            field_dict["overwriteOldest"] = overwrite_oldest

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        cap_bytes = d.pop("capBytes", UNSET)

        size_bytes = d.pop("sizeBytes", UNSET)

        exceeded = d.pop("exceeded", UNSET)

        def _parse_window_days(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        window_days = _parse_window_days(d.pop("windowDays", UNSET))

        overwrite_oldest = d.pop("overwriteOldest", UNSET)

        retention_preview_db_cap_type_0 = cls(
            cap_bytes=cap_bytes,
            size_bytes=size_bytes,
            exceeded=exceeded,
            window_days=window_days,
            overwrite_oldest=overwrite_oldest,
        )

        retention_preview_db_cap_type_0.additional_properties = d
        return retention_preview_db_cap_type_0

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
