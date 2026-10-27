from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ResourceGauge")


@_attrs_define
class ResourceGauge:
    """A single resource reading

    Attributes:
        status (str):
        total_bytes (int | None | Unset):
        used_bytes (int | None | Unset):
        free_bytes (int | None | Unset):
        used_percent (float | None | Unset):
    """

    status: str
    total_bytes: int | None | Unset = UNSET
    used_bytes: int | None | Unset = UNSET
    free_bytes: int | None | Unset = UNSET
    used_percent: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status

        total_bytes: int | None | Unset
        if isinstance(self.total_bytes, Unset):
            total_bytes = UNSET
        else:
            total_bytes = self.total_bytes

        used_bytes: int | None | Unset
        if isinstance(self.used_bytes, Unset):
            used_bytes = UNSET
        else:
            used_bytes = self.used_bytes

        free_bytes: int | None | Unset
        if isinstance(self.free_bytes, Unset):
            free_bytes = UNSET
        else:
            free_bytes = self.free_bytes

        used_percent: float | None | Unset
        if isinstance(self.used_percent, Unset):
            used_percent = UNSET
        else:
            used_percent = self.used_percent

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if total_bytes is not UNSET:
            field_dict["totalBytes"] = total_bytes
        if used_bytes is not UNSET:
            field_dict["usedBytes"] = used_bytes
        if free_bytes is not UNSET:
            field_dict["freeBytes"] = free_bytes
        if used_percent is not UNSET:
            field_dict["usedPercent"] = used_percent

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        status = d.pop("status")

        def _parse_total_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        total_bytes = _parse_total_bytes(d.pop("totalBytes", UNSET))

        def _parse_used_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        used_bytes = _parse_used_bytes(d.pop("usedBytes", UNSET))

        def _parse_free_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        free_bytes = _parse_free_bytes(d.pop("freeBytes", UNSET))

        def _parse_used_percent(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        used_percent = _parse_used_percent(d.pop("usedPercent", UNSET))

        resource_gauge = cls(
            status=status,
            total_bytes=total_bytes,
            used_bytes=used_bytes,
            free_bytes=free_bytes,
            used_percent=used_percent,
        )

        resource_gauge.additional_properties = d
        return resource_gauge

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
