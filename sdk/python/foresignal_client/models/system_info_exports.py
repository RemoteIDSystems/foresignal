from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoExports")


@_attrs_define
class SystemInfoExports:
    """Export jobs

    Attributes:
        queued (int | Unset):
        running (int | Unset):
        bytes_ (int | Unset):
        quota_bytes (int | Unset): exports.diskQuotaGb in bytes
        max_concurrent (int | Unset): exports.maxConcurrent
    """

    queued: int | Unset = UNSET
    running: int | Unset = UNSET
    bytes_: int | Unset = UNSET
    quota_bytes: int | Unset = UNSET
    max_concurrent: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        queued = self.queued

        running = self.running

        bytes_ = self.bytes_

        quota_bytes = self.quota_bytes

        max_concurrent = self.max_concurrent

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if queued is not UNSET:
            field_dict["queued"] = queued
        if running is not UNSET:
            field_dict["running"] = running
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if quota_bytes is not UNSET:
            field_dict["quotaBytes"] = quota_bytes
        if max_concurrent is not UNSET:
            field_dict["maxConcurrent"] = max_concurrent

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        queued = d.pop("queued", UNSET)

        running = d.pop("running", UNSET)

        bytes_ = d.pop("bytes", UNSET)

        quota_bytes = d.pop("quotaBytes", UNSET)

        max_concurrent = d.pop("maxConcurrent", UNSET)

        system_info_exports = cls(
            queued=queued,
            running=running,
            bytes_=bytes_,
            quota_bytes=quota_bytes,
            max_concurrent=max_concurrent,
        )

        system_info_exports.additional_properties = d
        return system_info_exports

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
