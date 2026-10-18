from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_info_archive_target import SystemInfoArchiveTarget
from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoArchive")


@_attrs_define
class SystemInfoArchive:
    """Cold archive

    Attributes:
        enabled (bool | Unset):
        last_run_at (datetime.datetime | None | Unset):
        bytes_ (int | Unset):
        failures (int | Unset):
        target (SystemInfoArchiveTarget | Unset):
        last_run_bytes (int | Unset):
        last_run_failures (int | Unset):
        last_failure_at (datetime.datetime | None | Unset):
    """

    enabled: bool | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
    bytes_: int | Unset = UNSET
    failures: int | Unset = UNSET
    target: SystemInfoArchiveTarget | Unset = UNSET
    last_run_bytes: int | Unset = UNSET
    last_run_failures: int | Unset = UNSET
    last_failure_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

        bytes_ = self.bytes_

        failures = self.failures

        target: str | Unset = UNSET
        if not isinstance(self.target, Unset):
            target = self.target.value

        last_run_bytes = self.last_run_bytes

        last_run_failures = self.last_run_failures

        last_failure_at: None | str | Unset
        if isinstance(self.last_failure_at, Unset):
            last_failure_at = UNSET
        elif isinstance(self.last_failure_at, datetime.datetime):
            last_failure_at = self.last_failure_at.isoformat()
        else:
            last_failure_at = self.last_failure_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if failures is not UNSET:
            field_dict["failures"] = failures
        if target is not UNSET:
            field_dict["target"] = target
        if last_run_bytes is not UNSET:
            field_dict["lastRunBytes"] = last_run_bytes
        if last_run_failures is not UNSET:
            field_dict["lastRunFailures"] = last_run_failures
        if last_failure_at is not UNSET:
            field_dict["lastFailureAt"] = last_failure_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        def _parse_last_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_run_at = _parse_last_run_at(d.pop("lastRunAt", UNSET))

        bytes_ = d.pop("bytes", UNSET)

        failures = d.pop("failures", UNSET)

        _target = d.pop("target", UNSET)
        target: SystemInfoArchiveTarget | Unset
        if isinstance(_target, Unset):
            target = UNSET
        else:
            target = SystemInfoArchiveTarget(_target)

        last_run_bytes = d.pop("lastRunBytes", UNSET)

        last_run_failures = d.pop("lastRunFailures", UNSET)

        def _parse_last_failure_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_failure_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_failure_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_failure_at = _parse_last_failure_at(d.pop("lastFailureAt", UNSET))

        system_info_archive = cls(
            enabled=enabled,
            last_run_at=last_run_at,
            bytes_=bytes_,
            failures=failures,
            target=target,
            last_run_bytes=last_run_bytes,
            last_run_failures=last_run_failures,
            last_failure_at=last_failure_at,
        )

        system_info_archive.additional_properties = d
        return system_info_archive

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
