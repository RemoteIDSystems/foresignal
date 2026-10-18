from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.usage_series_resolution import UsageSeriesResolution
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.usage_point import UsagePoint


T = TypeVar("T", bound="UsageSeries")


@_attrs_define
class UsageSeries:
    """
    Attributes:
        source_id (str):
        resolution (UsageSeriesResolution):
        points (list[UsagePoint]):
        totals (UsagePoint | Unset):
    """

    source_id: str
    resolution: UsageSeriesResolution
    points: list[UsagePoint]
    totals: UsagePoint | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        resolution = self.resolution.value

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        totals: dict[str, Any] | Unset = UNSET
        if not isinstance(self.totals, Unset):
            totals = self.totals.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "resolution": resolution,
                "points": points,
            }
        )
        if totals is not UNSET:
            field_dict["totals"] = totals

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.usage_point import UsagePoint

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        resolution = UsageSeriesResolution(d.pop("resolution"))

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = UsagePoint.from_dict(points_item_data)

            points.append(points_item)

        _totals = d.pop("totals", UNSET)
        totals: UsagePoint | Unset
        if isinstance(_totals, Unset):
            totals = UNSET
        else:
            totals = UsagePoint.from_dict(_totals)

        usage_series = cls(
            source_id=source_id,
            resolution=resolution,
            points=points,
            totals=totals,
        )

        usage_series.additional_properties = d
        return usage_series

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
