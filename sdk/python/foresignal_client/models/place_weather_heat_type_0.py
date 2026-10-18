from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherHeatType0")


@_attrs_define
class PlaceWeatherHeatType0:
    """106: WPC HeatRisk (experimental NWS product)

    Attributes:
        status (SampleStatus | Unset): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside
            product coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame
            older than maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or
            frame expired
        risk_max_day_1 (int | None | Unset):
        risk_max_7_d (int | None | Unset):
        peak_day (datetime.date | None | Unset):
    """

    status: SampleStatus | Unset = UNSET
    risk_max_day_1: int | None | Unset = UNSET
    risk_max_7_d: int | None | Unset = UNSET
    peak_day: datetime.date | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        risk_max_day_1: int | None | Unset
        if isinstance(self.risk_max_day_1, Unset):
            risk_max_day_1 = UNSET
        else:
            risk_max_day_1 = self.risk_max_day_1

        risk_max_7_d: int | None | Unset
        if isinstance(self.risk_max_7_d, Unset):
            risk_max_7_d = UNSET
        else:
            risk_max_7_d = self.risk_max_7_d

        peak_day: None | str | Unset
        if isinstance(self.peak_day, Unset):
            peak_day = UNSET
        elif isinstance(self.peak_day, datetime.date):
            peak_day = self.peak_day.isoformat()
        else:
            peak_day = self.peak_day

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if status is not UNSET:
            field_dict["status"] = status
        if risk_max_day_1 is not UNSET:
            field_dict["riskMaxDay1"] = risk_max_day_1
        if risk_max_7_d is not UNSET:
            field_dict["riskMax7d"] = risk_max_7_d
        if peak_day is not UNSET:
            field_dict["peakDay"] = peak_day

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _status = d.pop("status", UNSET)
        status: SampleStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = SampleStatus(_status)

        def _parse_risk_max_day_1(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        risk_max_day_1 = _parse_risk_max_day_1(d.pop("riskMaxDay1", UNSET))

        def _parse_risk_max_7_d(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        risk_max_7_d = _parse_risk_max_7_d(d.pop("riskMax7d", UNSET))

        def _parse_peak_day(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                peak_day_type_0 = datetime.date.fromisoformat(data)

                return peak_day_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        peak_day = _parse_peak_day(d.pop("peakDay", UNSET))

        place_weather_heat_type_0 = cls(
            status=status,
            risk_max_day_1=risk_max_day_1,
            risk_max_7_d=risk_max_7_d,
            peak_day=peak_day,
        )

        place_weather_heat_type_0.additional_properties = d
        return place_weather_heat_type_0

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
