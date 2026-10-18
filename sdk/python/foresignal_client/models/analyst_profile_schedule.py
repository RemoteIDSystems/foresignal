from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalystProfileSchedule")


@_attrs_define
class AnalystProfileSchedule:
    """5-field cron with an IANA timezone, or `every` (≥ 10m); default every 30m

    Attributes:
        cron (None | str | Unset):  Example: */30 * * * *.
        timezone (None | str | Unset):  Example: America/Chicago.
        every (None | str | Unset): Duration ≥ 10m Example: 30m.
    """

    cron: None | str | Unset = UNSET
    timezone: None | str | Unset = UNSET
    every: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cron: None | str | Unset
        if isinstance(self.cron, Unset):
            cron = UNSET
        else:
            cron = self.cron

        timezone: None | str | Unset
        if isinstance(self.timezone, Unset):
            timezone = UNSET
        else:
            timezone = self.timezone

        every: None | str | Unset
        if isinstance(self.every, Unset):
            every = UNSET
        else:
            every = self.every

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if cron is not UNSET:
            field_dict["cron"] = cron
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if every is not UNSET:
            field_dict["every"] = every

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_cron(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cron = _parse_cron(d.pop("cron", UNSET))

        def _parse_timezone(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        timezone = _parse_timezone(d.pop("timezone", UNSET))

        def _parse_every(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        every = _parse_every(d.pop("every", UNSET))

        analyst_profile_schedule = cls(
            cron=cron,
            timezone=timezone,
            every=every,
        )

        analyst_profile_schedule.additional_properties = d
        return analyst_profile_schedule

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
