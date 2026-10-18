from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.lead_time_stats_filters import LeadTimeStatsFilters
    from ..models.lead_time_stats_groups_item import LeadTimeStatsGroupsItem


T = TypeVar("T", bound="LeadTimeStats")


@_attrs_define
class LeadTimeStats:
    """Lead of social first observation over physical, news and official first observation, aggregated over corroborated
    comparisons; only out-of-sample comparisons support 'leads by' statements (137)

        Attributes:
            generated_at (datetime.datetime):
            training_cutoff (datetime.datetime): Comparisons evaluated after this are out of sample
            groups (list[LeadTimeStatsGroupsItem]):
            filters (LeadTimeStatsFilters | Unset): Applied filters
            caveats (list[Caveat] | Unset):
    """

    generated_at: datetime.datetime
    training_cutoff: datetime.datetime
    groups: list[LeadTimeStatsGroupsItem]
    filters: LeadTimeStatsFilters | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        generated_at = self.generated_at.isoformat()

        training_cutoff = self.training_cutoff.isoformat()

        groups = []
        for groups_item_data in self.groups:
            groups_item = groups_item_data.to_dict()
            groups.append(groups_item)

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

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
                "generatedAt": generated_at,
                "trainingCutoff": training_cutoff,
                "groups": groups,
            }
        )
        if filters is not UNSET:
            field_dict["filters"] = filters
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.lead_time_stats_filters import LeadTimeStatsFilters
        from ..models.lead_time_stats_groups_item import LeadTimeStatsGroupsItem

        d = dict(src_dict)
        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        training_cutoff = datetime.datetime.fromisoformat(d.pop("trainingCutoff"))

        groups = []
        _groups = d.pop("groups")
        for groups_item_data in _groups:
            groups_item = LeadTimeStatsGroupsItem.from_dict(groups_item_data)

            groups.append(groups_item)

        _filters = d.pop("filters", UNSET)
        filters: LeadTimeStatsFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = LeadTimeStatsFilters.from_dict(_filters)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        lead_time_stats = cls(
            generated_at=generated_at,
            training_cutoff=training_cutoff,
            groups=groups,
            filters=filters,
            caveats=caveats,
        )

        lead_time_stats.additional_properties = d
        return lead_time_stats

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
