from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherAuroraType0")


@_attrs_define
class PlaceWeatherAuroraType0:
    """106: only when |lat| ≥ 40°

    Attributes:
        status (SampleStatus | Unset): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside
            product coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame
            older than maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or
            frame expired
        prob_max_nearby (float | None | Unset):
    """

    status: SampleStatus | Unset = UNSET
    prob_max_nearby: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        prob_max_nearby: float | None | Unset
        if isinstance(self.prob_max_nearby, Unset):
            prob_max_nearby = UNSET
        else:
            prob_max_nearby = self.prob_max_nearby

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if status is not UNSET:
            field_dict["status"] = status
        if prob_max_nearby is not UNSET:
            field_dict["probMaxNearby"] = prob_max_nearby

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

        def _parse_prob_max_nearby(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        prob_max_nearby = _parse_prob_max_nearby(d.pop("probMaxNearby", UNSET))

        place_weather_aurora_type_0 = cls(
            status=status,
            prob_max_nearby=prob_max_nearby,
        )

        place_weather_aurora_type_0.additional_properties = d
        return place_weather_aurora_type_0

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
