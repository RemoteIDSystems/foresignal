from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.lead_time_stats_groups_item_found_by import LeadTimeStatsGroupsItemFoundBy
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.lead_time_summary import LeadTimeSummary


T = TypeVar("T", bound="LeadTimeStatsGroupsItem")


@_attrs_define
class LeadTimeStatsGroupsItem:
    """One claim type × platform × region × foundBy group

    Attributes:
        claim_type (str):
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        found_by (LeadTimeStatsGroupsItemFoundBy):
        comparisons (int):
        out_of_sample (int):
        over_physical (LeadTimeSummary): Median and interquartile lead in seconds (positive = social first)
        over_news (LeadTimeSummary): Median and interquartile lead in seconds (positive = social first)
        over_official (LeadTimeSummary): Median and interquartile lead in seconds (positive = social first)
        region_code (None | str | Unset):
        out_of_sample_over_physical (LeadTimeSummary | Unset): Median and interquartile lead in seconds (positive =
            social first)
    """

    claim_type: str
    platform: str
    found_by: LeadTimeStatsGroupsItemFoundBy
    comparisons: int
    out_of_sample: int
    over_physical: LeadTimeSummary
    over_news: LeadTimeSummary
    over_official: LeadTimeSummary
    region_code: None | str | Unset = UNSET
    out_of_sample_over_physical: LeadTimeSummary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        claim_type = self.claim_type

        platform = self.platform

        found_by = self.found_by.value

        comparisons = self.comparisons

        out_of_sample = self.out_of_sample

        over_physical = self.over_physical.to_dict()

        over_news = self.over_news.to_dict()

        over_official = self.over_official.to_dict()

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        out_of_sample_over_physical: dict[str, Any] | Unset = UNSET
        if not isinstance(self.out_of_sample_over_physical, Unset):
            out_of_sample_over_physical = self.out_of_sample_over_physical.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "claimType": claim_type,
                "platform": platform,
                "foundBy": found_by,
                "comparisons": comparisons,
                "outOfSample": out_of_sample,
                "overPhysical": over_physical,
                "overNews": over_news,
                "overOfficial": over_official,
            }
        )
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code
        if out_of_sample_over_physical is not UNSET:
            field_dict["outOfSampleOverPhysical"] = out_of_sample_over_physical

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.lead_time_summary import LeadTimeSummary

        d = dict(src_dict)
        claim_type = d.pop("claimType")

        platform = d.pop("platform")

        found_by = LeadTimeStatsGroupsItemFoundBy(d.pop("foundBy"))

        comparisons = d.pop("comparisons")

        out_of_sample = d.pop("outOfSample")

        over_physical = LeadTimeSummary.from_dict(d.pop("overPhysical"))

        over_news = LeadTimeSummary.from_dict(d.pop("overNews"))

        over_official = LeadTimeSummary.from_dict(d.pop("overOfficial"))

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        _out_of_sample_over_physical = d.pop("outOfSampleOverPhysical", UNSET)
        out_of_sample_over_physical: LeadTimeSummary | Unset
        if isinstance(_out_of_sample_over_physical, Unset):
            out_of_sample_over_physical = UNSET
        else:
            out_of_sample_over_physical = LeadTimeSummary.from_dict(
                _out_of_sample_over_physical
            )

        lead_time_stats_groups_item = cls(
            claim_type=claim_type,
            platform=platform,
            found_by=found_by,
            comparisons=comparisons,
            out_of_sample=out_of_sample,
            over_physical=over_physical,
            over_news=over_news,
            over_official=over_official,
            region_code=region_code,
            out_of_sample_over_physical=out_of_sample_over_physical,
        )

        lead_time_stats_groups_item.additional_properties = d
        return lead_time_stats_groups_item

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
