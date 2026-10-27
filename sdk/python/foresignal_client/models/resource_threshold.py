from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ResourceThreshold")


@_attrs_define
class ResourceThreshold:
    """Warn/critical thresholds for one resource (free space or memory). A level is reached when the free share drops below
    its percent or the free amount below its bytes; a null level is off. Critical must be below warn.

        Attributes:
            warn_free_percent (float | None | Unset): Warn when free drops below this percent
            warn_free_bytes (int | None | Unset): Warn when free drops below this many bytes
            critical_free_percent (float | None | Unset): Critical when free drops below this percent
            critical_free_bytes (int | None | Unset): Critical when free drops below this many bytes
    """

    warn_free_percent: float | None | Unset = UNSET
    warn_free_bytes: int | None | Unset = UNSET
    critical_free_percent: float | None | Unset = UNSET
    critical_free_bytes: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        warn_free_percent: float | None | Unset
        if isinstance(self.warn_free_percent, Unset):
            warn_free_percent = UNSET
        else:
            warn_free_percent = self.warn_free_percent

        warn_free_bytes: int | None | Unset
        if isinstance(self.warn_free_bytes, Unset):
            warn_free_bytes = UNSET
        else:
            warn_free_bytes = self.warn_free_bytes

        critical_free_percent: float | None | Unset
        if isinstance(self.critical_free_percent, Unset):
            critical_free_percent = UNSET
        else:
            critical_free_percent = self.critical_free_percent

        critical_free_bytes: int | None | Unset
        if isinstance(self.critical_free_bytes, Unset):
            critical_free_bytes = UNSET
        else:
            critical_free_bytes = self.critical_free_bytes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if warn_free_percent is not UNSET:
            field_dict["warnFreePercent"] = warn_free_percent
        if warn_free_bytes is not UNSET:
            field_dict["warnFreeBytes"] = warn_free_bytes
        if critical_free_percent is not UNSET:
            field_dict["criticalFreePercent"] = critical_free_percent
        if critical_free_bytes is not UNSET:
            field_dict["criticalFreeBytes"] = critical_free_bytes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_warn_free_percent(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        warn_free_percent = _parse_warn_free_percent(d.pop("warnFreePercent", UNSET))

        def _parse_warn_free_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        warn_free_bytes = _parse_warn_free_bytes(d.pop("warnFreeBytes", UNSET))

        def _parse_critical_free_percent(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        critical_free_percent = _parse_critical_free_percent(
            d.pop("criticalFreePercent", UNSET)
        )

        def _parse_critical_free_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        critical_free_bytes = _parse_critical_free_bytes(
            d.pop("criticalFreeBytes", UNSET)
        )

        resource_threshold = cls(
            warn_free_percent=warn_free_percent,
            warn_free_bytes=warn_free_bytes,
            critical_free_percent=critical_free_percent,
            critical_free_bytes=critical_free_bytes,
        )

        resource_threshold.additional_properties = d
        return resource_threshold

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
