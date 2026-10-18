from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_usage_series_resolution import LlmUsageSeriesResolution
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_usage_point import LlmUsagePoint


T = TypeVar("T", bound="LlmUsageSeries")


@_attrs_define
class LlmUsageSeries:
    """
    Attributes:
        resolution (LlmUsageSeriesResolution):
        points (list[LlmUsagePoint]):
        provider (None | str | Unset):
        totals (LlmUsagePoint | Unset):
    """

    resolution: LlmUsageSeriesResolution
    points: list[LlmUsagePoint]
    provider: None | str | Unset = UNSET
    totals: LlmUsagePoint | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        resolution = self.resolution.value

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        provider: None | str | Unset
        if isinstance(self.provider, Unset):
            provider = UNSET
        else:
            provider = self.provider

        totals: dict[str, Any] | Unset = UNSET
        if not isinstance(self.totals, Unset):
            totals = self.totals.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "resolution": resolution,
                "points": points,
            }
        )
        if provider is not UNSET:
            field_dict["provider"] = provider
        if totals is not UNSET:
            field_dict["totals"] = totals

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_usage_point import LlmUsagePoint

        d = dict(src_dict)
        resolution = LlmUsageSeriesResolution(d.pop("resolution"))

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = LlmUsagePoint.from_dict(points_item_data)

            points.append(points_item)

        def _parse_provider(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        provider = _parse_provider(d.pop("provider", UNSET))

        _totals = d.pop("totals", UNSET)
        totals: LlmUsagePoint | Unset
        if isinstance(_totals, Unset):
            totals = UNSET
        else:
            totals = LlmUsagePoint.from_dict(_totals)

        llm_usage_series = cls(
            resolution=resolution,
            points=points,
            provider=provider,
            totals=totals,
        )

        llm_usage_series.additional_properties = d
        return llm_usage_series

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
