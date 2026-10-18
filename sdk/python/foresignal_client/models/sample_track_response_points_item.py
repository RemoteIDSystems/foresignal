from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus

if TYPE_CHECKING:
    from ..models.sample_value import SampleValue


T = TypeVar("T", bound="SampleTrackResponsePointsItem")


@_attrs_define
class SampleTrackResponsePointsItem:
    """Samples at one point

    Attributes:
        lon (float):
        lat (float):
        time (datetime.datetime):
        status (SampleStatus): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside product
            coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame older than
            maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or frame expired
        values (list[SampleValue]):
    """

    lon: float
    lat: float
    time: datetime.datetime
    status: SampleStatus
    values: list[SampleValue]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lon = self.lon

        lat = self.lat

        time = self.time.isoformat()

        status = self.status.value

        values = []
        for values_item_data in self.values:
            values_item = values_item_data.to_dict()
            values.append(values_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "lon": lon,
                "lat": lat,
                "time": time,
                "status": status,
                "values": values,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sample_value import SampleValue

        d = dict(src_dict)
        lon = d.pop("lon")

        lat = d.pop("lat")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        status = SampleStatus(d.pop("status"))

        values = []
        _values = d.pop("values")
        for values_item_data in _values:
            values_item = SampleValue.from_dict(values_item_data)

            values.append(values_item)

        sample_track_response_points_item = cls(
            lon=lon,
            lat=lat,
            time=time,
            status=status,
            values=values,
        )

        sample_track_response_points_item.additional_properties = d
        return sample_track_response_points_item

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
