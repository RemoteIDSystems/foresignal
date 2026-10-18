from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_volume_interval import SocialVolumeInterval
from ..models.social_volume_metric import SocialVolumeMetric
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.detector_ref import DetectorRef
    from ..models.social_volume_scope import SocialVolumeScope
    from ..models.social_volume_series import SocialVolumeSeries


T = TypeVar("T", bound="SocialVolume")


@_attrs_define
class SocialVolume:
    """Hourly mention volume of a topic, place or entity per platform with seasonal, denominator-scaled baselines
    (`SocialBaseline` 1.0.0)

        Attributes:
            scope (SocialVolumeScope): What is counted
            metric (SocialVolumeMetric):
            interval (SocialVolumeInterval):
            since (datetime.datetime):
            until (datetime.datetime):
            series (list[SocialVolumeSeries]):
            baseline (DetectorRef):
            caveats (list[Caveat] | Unset):
    """

    scope: SocialVolumeScope
    metric: SocialVolumeMetric
    interval: SocialVolumeInterval
    since: datetime.datetime
    until: datetime.datetime
    series: list[SocialVolumeSeries]
    baseline: DetectorRef
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        scope = self.scope.to_dict()

        metric = self.metric.value

        interval = self.interval.value

        since = self.since.isoformat()

        until = self.until.isoformat()

        series = []
        for series_item_data in self.series:
            series_item = series_item_data.to_dict()
            series.append(series_item)

        baseline = self.baseline.to_dict()

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "scope": scope,
                "metric": metric,
                "interval": interval,
                "since": since,
                "until": until,
                "series": series,
                "baseline": baseline,
            }
        )
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.detector_ref import DetectorRef
        from ..models.social_volume_scope import SocialVolumeScope
        from ..models.social_volume_series import SocialVolumeSeries

        d = dict(src_dict)
        scope = SocialVolumeScope.from_dict(d.pop("scope"))

        metric = SocialVolumeMetric(d.pop("metric"))

        interval = SocialVolumeInterval(d.pop("interval"))

        since = datetime.datetime.fromisoformat(d.pop("since"))

        until = datetime.datetime.fromisoformat(d.pop("until"))

        series = []
        _series = d.pop("series")
        for series_item_data in _series:
            series_item = SocialVolumeSeries.from_dict(series_item_data)

            series.append(series_item)

        baseline = DetectorRef.from_dict(d.pop("baseline"))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        social_volume = cls(
            scope=scope,
            metric=metric,
            interval=interval,
            since=since,
            until=until,
            series=series,
            baseline=baseline,
            caveats=caveats,
        )

        social_volume.additional_properties = d
        return social_volume

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
