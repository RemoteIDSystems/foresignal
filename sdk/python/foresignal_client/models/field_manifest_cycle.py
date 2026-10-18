from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="FieldManifestCycle")


@_attrs_define
class FieldManifestCycle:
    """Model cycle

    Attributes:
        date (str):  Example: 20261010.
        hour (int):
        forecast_hour (int):
        run_iso (datetime.datetime):
        valid_iso (datetime.datetime):
    """

    date: str
    hour: int
    forecast_hour: int
    run_iso: datetime.datetime
    valid_iso: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        date = self.date

        hour = self.hour

        forecast_hour = self.forecast_hour

        run_iso = self.run_iso.isoformat()

        valid_iso = self.valid_iso.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "date": date,
                "hour": hour,
                "forecastHour": forecast_hour,
                "runIso": run_iso,
                "validIso": valid_iso,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        date = d.pop("date")

        hour = d.pop("hour")

        forecast_hour = d.pop("forecastHour")

        run_iso = datetime.datetime.fromisoformat(d.pop("runIso"))

        valid_iso = datetime.datetime.fromisoformat(d.pop("validIso"))

        field_manifest_cycle = cls(
            date=date,
            hour=hour,
            forecast_hour=forecast_hour,
            run_iso=run_iso,
            valid_iso=valid_iso,
        )

        field_manifest_cycle.additional_properties = d
        return field_manifest_cycle

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
