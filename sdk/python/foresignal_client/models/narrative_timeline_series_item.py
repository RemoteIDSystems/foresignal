from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.narrative_timeline_series_item_points_item import (
        NarrativeTimelineSeriesItemPointsItem,
    )


T = TypeVar("T", bound="NarrativeTimelineSeriesItem")


@_attrs_define
class NarrativeTimelineSeriesItem:
    """One platform

    Attributes:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        points (list[NarrativeTimelineSeriesItemPointsItem]):
    """

    platform: str
    points: list[NarrativeTimelineSeriesItemPointsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "points": points,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.narrative_timeline_series_item_points_item import (
            NarrativeTimelineSeriesItemPointsItem,
        )

        d = dict(src_dict)
        platform = d.pop("platform")

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = NarrativeTimelineSeriesItemPointsItem.from_dict(
                points_item_data
            )

            points.append(points_item)

        narrative_timeline_series_item = cls(
            platform=platform,
            points=points,
        )

        narrative_timeline_series_item.additional_properties = d
        return narrative_timeline_series_item

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
