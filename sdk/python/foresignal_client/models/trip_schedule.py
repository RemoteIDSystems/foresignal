from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.trip_schedule_days_item import TripScheduleDaysItem
from ..models.trip_schedule_type import TripScheduleType
from ..types import UNSET, Unset

T = TypeVar("T", bound="TripSchedule")


@_attrs_define
class TripSchedule:
    """One-off departure or recurring commute schedule (114)

    Attributes:
        type_ (TripScheduleType):
        depart_at (datetime.datetime | None | Unset): once
        days (list[TripScheduleDaysItem] | Unset): recurring
        depart_time (None | str | Unset): recurring, local HH:mm
        timezone (None | str | Unset): IANA zone for recurring schedules
    """

    type_: TripScheduleType
    depart_at: datetime.datetime | None | Unset = UNSET
    days: list[TripScheduleDaysItem] | Unset = UNSET
    depart_time: None | str | Unset = UNSET
    timezone: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        depart_at: None | str | Unset
        if isinstance(self.depart_at, Unset):
            depart_at = UNSET
        elif isinstance(self.depart_at, datetime.datetime):
            depart_at = self.depart_at.isoformat()
        else:
            depart_at = self.depart_at

        days: list[str] | Unset = UNSET
        if not isinstance(self.days, Unset):
            days = []
            for days_item_data in self.days:
                days_item = days_item_data.value
                days.append(days_item)

        depart_time: None | str | Unset
        if isinstance(self.depart_time, Unset):
            depart_time = UNSET
        else:
            depart_time = self.depart_time

        timezone: None | str | Unset
        if isinstance(self.timezone, Unset):
            timezone = UNSET
        else:
            timezone = self.timezone

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if depart_at is not UNSET:
            field_dict["departAt"] = depart_at
        if days is not UNSET:
            field_dict["days"] = days
        if depart_time is not UNSET:
            field_dict["departTime"] = depart_time
        if timezone is not UNSET:
            field_dict["timezone"] = timezone

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = TripScheduleType(d.pop("type"))

        def _parse_depart_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                depart_at_type_0 = datetime.datetime.fromisoformat(data)

                return depart_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        depart_at = _parse_depart_at(d.pop("departAt", UNSET))

        _days = d.pop("days", UNSET)
        days: list[TripScheduleDaysItem] | Unset = UNSET
        if _days is not UNSET:
            days = []
            for days_item_data in _days:
                days_item = TripScheduleDaysItem(days_item_data)

                days.append(days_item)

        def _parse_depart_time(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        depart_time = _parse_depart_time(d.pop("departTime", UNSET))

        def _parse_timezone(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        timezone = _parse_timezone(d.pop("timezone", UNSET))

        trip_schedule = cls(
            type_=type_,
            depart_at=depart_at,
            days=days,
            depart_time=depart_time,
            timezone=timezone,
        )

        trip_schedule.additional_properties = d
        return trip_schedule

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
