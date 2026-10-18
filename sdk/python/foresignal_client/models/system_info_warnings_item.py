from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_info_warnings_item_severity import SystemInfoWarningsItemSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoWarningsItem")


@_attrs_define
class SystemInfoWarningsItem:
    """
    Attributes:
        code (str): OPEN_READS_EXPOSED, OPEN_READS_ALL_INTERFACES, HISTORY_RATE_HIGH, PARTITION_HEADROOM_LOW,
            RETENTION_CONSUMER_LAG, RASTER_DISK_QUOTA (042: the raster store is at 90 % of its quota or more) (open set)
        message (str):
        severity (SystemInfoWarningsItemSeverity | Unset):
    """

    code: str
    message: str
    severity: SystemInfoWarningsItemSeverity | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        code = self.code

        message = self.message

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "code": code,
                "message": message,
            }
        )
        if severity is not UNSET:
            field_dict["severity"] = severity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        code = d.pop("code")

        message = d.pop("message")

        _severity = d.pop("severity", UNSET)
        severity: SystemInfoWarningsItemSeverity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = SystemInfoWarningsItemSeverity(_severity)

        system_info_warnings_item = cls(
            code=code,
            message=message,
            severity=severity,
        )

        system_info_warnings_item.additional_properties = d
        return system_info_warnings_item

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
