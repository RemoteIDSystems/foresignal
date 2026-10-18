from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.quiet_hours_bypass_severity_type_1 import QuietHoursBypassSeverityType1
from ..models.quiet_hours_bypass_severity_type_2_type_1 import (
    QuietHoursBypassSeverityType2Type1,
)
from ..models.quiet_hours_bypass_severity_type_3_type_1 import (
    QuietHoursBypassSeverityType3Type1,
)
from ..models.quiet_hours_days_item import QuietHoursDaysItem
from ..types import UNSET, Unset

T = TypeVar("T", bound="QuietHours")


@_attrs_define
class QuietHours:
    """Per-channel quiet hours in the channel timezone; deferred deliveries are released as one digest at window end

    Attributes:
        start (str):  Example: 22:00.
        end (str):  Example: 07:00.
        days (list[QuietHoursDaysItem] | Unset): Days the window starts on; empty or omitted = every day
        bypass_severity (None | QuietHoursBypassSeverityType1 | QuietHoursBypassSeverityType2Type1 |
            QuietHoursBypassSeverityType3Type1 | Unset): Severity at or above which messages are sent anyway Default:
            QuietHoursBypassSeverityType1.CRITICAL.
    """

    start: str
    end: str
    days: list[QuietHoursDaysItem] | Unset = UNSET
    bypass_severity: (
        None
        | QuietHoursBypassSeverityType1
        | QuietHoursBypassSeverityType2Type1
        | QuietHoursBypassSeverityType3Type1
        | Unset
    ) = QuietHoursBypassSeverityType1.CRITICAL
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        start = self.start

        end = self.end

        days: list[str] | Unset = UNSET
        if not isinstance(self.days, Unset):
            days = []
            for days_item_data in self.days:
                days_item = days_item_data.value
                days.append(days_item)

        bypass_severity: None | str | Unset
        if isinstance(self.bypass_severity, Unset):
            bypass_severity = UNSET
        elif (
            isinstance(self.bypass_severity, QuietHoursBypassSeverityType1)
            or isinstance(self.bypass_severity, QuietHoursBypassSeverityType2Type1)
            or isinstance(self.bypass_severity, QuietHoursBypassSeverityType3Type1)
        ):
            bypass_severity = self.bypass_severity.value
        else:
            bypass_severity = self.bypass_severity

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "start": start,
                "end": end,
            }
        )
        if days is not UNSET:
            field_dict["days"] = days
        if bypass_severity is not UNSET:
            field_dict["bypassSeverity"] = bypass_severity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        start = d.pop("start")

        end = d.pop("end")

        _days = d.pop("days", UNSET)
        days: list[QuietHoursDaysItem] | Unset = UNSET
        if _days is not UNSET:
            days = []
            for days_item_data in _days:
                days_item = QuietHoursDaysItem(days_item_data)

                days.append(days_item)

        def _parse_bypass_severity(
            data: object,
        ) -> (
            None
            | QuietHoursBypassSeverityType1
            | QuietHoursBypassSeverityType2Type1
            | QuietHoursBypassSeverityType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                bypass_severity_type_1 = QuietHoursBypassSeverityType1(data)

                return bypass_severity_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                bypass_severity_type_2_type_1 = QuietHoursBypassSeverityType2Type1(data)

                return bypass_severity_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                bypass_severity_type_3_type_1 = QuietHoursBypassSeverityType3Type1(data)

                return bypass_severity_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | QuietHoursBypassSeverityType1
                | QuietHoursBypassSeverityType2Type1
                | QuietHoursBypassSeverityType3Type1
                | Unset,
                data,
            )

        bypass_severity = _parse_bypass_severity(d.pop("bypassSeverity", UNSET))

        quiet_hours = cls(
            start=start,
            end=end,
            days=days,
            bypass_severity=bypass_severity,
        )

        quiet_hours.additional_properties = d
        return quiet_hours

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
