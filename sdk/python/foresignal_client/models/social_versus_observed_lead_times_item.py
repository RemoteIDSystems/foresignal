from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.lead_time_summary import LeadTimeSummary


T = TypeVar("T", bound="SocialVersusObservedLeadTimesItem")


@_attrs_define
class SocialVersusObservedLeadTimesItem:
    """Notable out-of-sample lead time

    Attributes:
        claim_type (str):
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        over_physical (LeadTimeSummary): Median and interquartile lead in seconds (positive = social first)
        region_code (None | str | Unset):
    """

    claim_type: str
    platform: str
    over_physical: LeadTimeSummary
    region_code: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        claim_type = self.claim_type

        platform = self.platform

        over_physical = self.over_physical.to_dict()

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "claimType": claim_type,
                "platform": platform,
                "overPhysical": over_physical,
            }
        )
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.lead_time_summary import LeadTimeSummary

        d = dict(src_dict)
        claim_type = d.pop("claimType")

        platform = d.pop("platform")

        over_physical = LeadTimeSummary.from_dict(d.pop("overPhysical"))

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        social_versus_observed_lead_times_item = cls(
            claim_type=claim_type,
            platform=platform,
            over_physical=over_physical,
            region_code=region_code,
        )

        social_versus_observed_lead_times_item.additional_properties = d
        return social_versus_observed_lead_times_item

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
