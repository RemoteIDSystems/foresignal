from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherMarineType0")


@_attrs_define
class PlaceWeatherMarineType0:
    """106: places tagged `coastal` only

    Attributes:
        status (SampleStatus | Unset): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside
            product coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame
            older than maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or
            frame expired
        wave_height_max_m24_h (float | None | Unset):
    """

    status: SampleStatus | Unset = UNSET
    wave_height_max_m24_h: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        wave_height_max_m24_h: float | None | Unset
        if isinstance(self.wave_height_max_m24_h, Unset):
            wave_height_max_m24_h = UNSET
        else:
            wave_height_max_m24_h = self.wave_height_max_m24_h

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if status is not UNSET:
            field_dict["status"] = status
        if wave_height_max_m24_h is not UNSET:
            field_dict["waveHeightMaxM24h"] = wave_height_max_m24_h

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

        def _parse_wave_height_max_m24_h(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        wave_height_max_m24_h = _parse_wave_height_max_m24_h(
            d.pop("waveHeightMaxM24h", UNSET)
        )

        place_weather_marine_type_0 = cls(
            status=status,
            wave_height_max_m24_h=wave_height_max_m24_h,
        )

        place_weather_marine_type_0.additional_properties = d
        return place_weather_marine_type_0

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
