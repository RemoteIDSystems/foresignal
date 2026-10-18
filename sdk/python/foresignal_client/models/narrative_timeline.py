from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.narrative_timeline_interval import NarrativeTimelineInterval

if TYPE_CHECKING:
    from ..models.narrative_timeline_series_item import NarrativeTimelineSeriesItem


T = TypeVar("T", bound="NarrativeTimeline")


@_attrs_define
class NarrativeTimeline:
    """Posts and unique authors per platform per interval for one narrative

    Attributes:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        interval (NarrativeTimelineInterval):
        series (list[NarrativeTimelineSeriesItem]):
    """

    narrative_id: str
    interval: NarrativeTimelineInterval
    series: list[NarrativeTimelineSeriesItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        narrative_id = self.narrative_id

        interval = self.interval.value

        series = []
        for series_item_data in self.series:
            series_item = series_item_data.to_dict()
            series.append(series_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narrativeId": narrative_id,
                "interval": interval,
                "series": series,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.narrative_timeline_series_item import NarrativeTimelineSeriesItem

        d = dict(src_dict)
        narrative_id = d.pop("narrativeId")

        interval = NarrativeTimelineInterval(d.pop("interval"))

        series = []
        _series = d.pop("series")
        for series_item_data in _series:
            series_item = NarrativeTimelineSeriesItem.from_dict(series_item_data)

            series.append(series_item)

        narrative_timeline = cls(
            narrative_id=narrative_id,
            interval=interval,
            series=series,
        )

        narrative_timeline.additional_properties = d
        return narrative_timeline

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
