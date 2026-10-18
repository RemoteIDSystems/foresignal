from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_coverage_bucket import SocialCoverageBucket


T = TypeVar("T", bound="SocialCoverage")


@_attrs_define
class SocialCoverage:
    """Hourly collection coverage of one platform in one region (`social_coverage`, 135)

    Attributes:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        since (datetime.datetime):
        until (datetime.datetime):
        buckets (list[SocialCoverageBucket]):
        complete_share (float):
        absence_ok (bool): `canSeeAbsence(social-attention, region, window)`: a healthy complete-stream platform and
            baseline ≥ `social.minRegionBaseline`
        region_code (None | str | Unset): 040 region code; null = all regions
        baseline_posts_per_hour (float | None | Unset): Trailing 8-week baseline used by social observability
        reason (None | str | Unset): LOW_SOCIAL_COVERAGE or a gap reason when absenceOk is false
    """

    platform: str
    since: datetime.datetime
    until: datetime.datetime
    buckets: list[SocialCoverageBucket]
    complete_share: float
    absence_ok: bool
    region_code: None | str | Unset = UNSET
    baseline_posts_per_hour: float | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform

        since = self.since.isoformat()

        until = self.until.isoformat()

        buckets = []
        for buckets_item_data in self.buckets:
            buckets_item = buckets_item_data.to_dict()
            buckets.append(buckets_item)

        complete_share = self.complete_share

        absence_ok = self.absence_ok

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        baseline_posts_per_hour: float | None | Unset
        if isinstance(self.baseline_posts_per_hour, Unset):
            baseline_posts_per_hour = UNSET
        else:
            baseline_posts_per_hour = self.baseline_posts_per_hour

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "since": since,
                "until": until,
                "buckets": buckets,
                "completeShare": complete_share,
                "absenceOk": absence_ok,
            }
        )
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code
        if baseline_posts_per_hour is not UNSET:
            field_dict["baselinePostsPerHour"] = baseline_posts_per_hour
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_coverage_bucket import SocialCoverageBucket

        d = dict(src_dict)
        platform = d.pop("platform")

        since = datetime.datetime.fromisoformat(d.pop("since"))

        until = datetime.datetime.fromisoformat(d.pop("until"))

        buckets = []
        _buckets = d.pop("buckets")
        for buckets_item_data in _buckets:
            buckets_item = SocialCoverageBucket.from_dict(buckets_item_data)

            buckets.append(buckets_item)

        complete_share = d.pop("completeShare")

        absence_ok = d.pop("absenceOk")

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        def _parse_baseline_posts_per_hour(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        baseline_posts_per_hour = _parse_baseline_posts_per_hour(
            d.pop("baselinePostsPerHour", UNSET)
        )

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        social_coverage = cls(
            platform=platform,
            since=since,
            until=until,
            buckets=buckets,
            complete_share=complete_share,
            absence_ok=absence_ok,
            region_code=region_code,
            baseline_posts_per_hour=baseline_posts_per_hour,
            reason=reason,
        )

        social_coverage.additional_properties = d
        return social_coverage

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
