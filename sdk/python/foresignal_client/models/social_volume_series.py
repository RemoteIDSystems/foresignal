from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_collection_type import SocialCollectionType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_volume_point import SocialVolumePoint


T = TypeVar("T", bound="SocialVolumeSeries")


@_attrs_define
class SocialVolumeSeries:
    """One platform (and language) series of a scope

    Attributes:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        series_ref (str): 119 analysis-series registry id `social:{scope}/{platform}/{metric}` Example:
            social:topic:red-sea/bluesky/posts.
        collection_type (SocialCollectionType): Measured versus sampled (135): `complete` stream for its filter
            (Jetstream, Mastodon hashtag stream, X counts), `paged` search (complete within the API's window and limits),
            `sampled` ranked or targeted sample (top-post search, model-mediated search, cues). Only complete and paged
            paths feed baselines and velocity
        baseline_eligible (bool): False for sampled paths (no baseline, no velocity)
        points (list[SocialVolumePoint]):
        language (None | str | Unset):
    """

    platform: str
    series_ref: str
    collection_type: SocialCollectionType
    baseline_eligible: bool
    points: list[SocialVolumePoint]
    language: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform

        series_ref = self.series_ref

        collection_type = self.collection_type.value

        baseline_eligible = self.baseline_eligible

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "seriesRef": series_ref,
                "collectionType": collection_type,
                "baselineEligible": baseline_eligible,
                "points": points,
            }
        )
        if language is not UNSET:
            field_dict["language"] = language

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_volume_point import SocialVolumePoint

        d = dict(src_dict)
        platform = d.pop("platform")

        series_ref = d.pop("seriesRef")

        collection_type = SocialCollectionType(d.pop("collectionType"))

        baseline_eligible = d.pop("baselineEligible")

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = SocialVolumePoint.from_dict(points_item_data)

            points.append(points_item)

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        social_volume_series = cls(
            platform=platform,
            series_ref=series_ref,
            collection_type=collection_type,
            baseline_eligible=baseline_eligible,
            points=points,
            language=language,
        )

        social_volume_series.additional_properties = d
        return social_volume_series

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
