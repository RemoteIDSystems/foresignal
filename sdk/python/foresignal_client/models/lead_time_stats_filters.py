from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.lead_time_stats_filters_found_by_type_1 import (
    LeadTimeStatsFiltersFoundByType1,
)
from ..models.lead_time_stats_filters_found_by_type_2_type_1 import (
    LeadTimeStatsFiltersFoundByType2Type1,
)
from ..models.lead_time_stats_filters_found_by_type_3_type_1 import (
    LeadTimeStatsFiltersFoundByType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="LeadTimeStatsFilters")


@_attrs_define
class LeadTimeStatsFilters:
    """Applied filters

    Attributes:
        claim_type (None | str | Unset):
        platform (None | str | Unset):
        region (None | str | Unset):
        found_by (LeadTimeStatsFiltersFoundByType1 | LeadTimeStatsFiltersFoundByType2Type1 |
            LeadTimeStatsFiltersFoundByType3Type1 | None | Unset):
        since (datetime.datetime | None | Unset):
    """

    claim_type: None | str | Unset = UNSET
    platform: None | str | Unset = UNSET
    region: None | str | Unset = UNSET
    found_by: (
        LeadTimeStatsFiltersFoundByType1
        | LeadTimeStatsFiltersFoundByType2Type1
        | LeadTimeStatsFiltersFoundByType3Type1
        | None
        | Unset
    ) = UNSET
    since: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        claim_type: None | str | Unset
        if isinstance(self.claim_type, Unset):
            claim_type = UNSET
        else:
            claim_type = self.claim_type

        platform: None | str | Unset
        if isinstance(self.platform, Unset):
            platform = UNSET
        else:
            platform = self.platform

        region: None | str | Unset
        if isinstance(self.region, Unset):
            region = UNSET
        else:
            region = self.region

        found_by: None | str | Unset
        if isinstance(self.found_by, Unset):
            found_by = UNSET
        elif (
            isinstance(self.found_by, LeadTimeStatsFiltersFoundByType1)
            or isinstance(self.found_by, LeadTimeStatsFiltersFoundByType2Type1)
            or isinstance(self.found_by, LeadTimeStatsFiltersFoundByType3Type1)
        ):
            found_by = self.found_by.value
        else:
            found_by = self.found_by

        since: None | str | Unset
        if isinstance(self.since, Unset):
            since = UNSET
        elif isinstance(self.since, datetime.datetime):
            since = self.since.isoformat()
        else:
            since = self.since

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if claim_type is not UNSET:
            field_dict["claimType"] = claim_type
        if platform is not UNSET:
            field_dict["platform"] = platform
        if region is not UNSET:
            field_dict["region"] = region
        if found_by is not UNSET:
            field_dict["foundBy"] = found_by
        if since is not UNSET:
            field_dict["since"] = since

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_claim_type(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        claim_type = _parse_claim_type(d.pop("claimType", UNSET))

        def _parse_platform(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        platform = _parse_platform(d.pop("platform", UNSET))

        def _parse_region(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region = _parse_region(d.pop("region", UNSET))

        def _parse_found_by(
            data: object,
        ) -> (
            LeadTimeStatsFiltersFoundByType1
            | LeadTimeStatsFiltersFoundByType2Type1
            | LeadTimeStatsFiltersFoundByType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                found_by_type_1 = LeadTimeStatsFiltersFoundByType1(data)

                return found_by_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                found_by_type_2_type_1 = LeadTimeStatsFiltersFoundByType2Type1(data)

                return found_by_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                found_by_type_3_type_1 = LeadTimeStatsFiltersFoundByType3Type1(data)

                return found_by_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                LeadTimeStatsFiltersFoundByType1
                | LeadTimeStatsFiltersFoundByType2Type1
                | LeadTimeStatsFiltersFoundByType3Type1
                | None
                | Unset,
                data,
            )

        found_by = _parse_found_by(d.pop("foundBy", UNSET))

        def _parse_since(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                since_type_0 = datetime.datetime.fromisoformat(data)

                return since_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        since = _parse_since(d.pop("since", UNSET))

        lead_time_stats_filters = cls(
            claim_type=claim_type,
            platform=platform,
            region=region,
            found_by=found_by,
            since=since,
        )

        lead_time_stats_filters.additional_properties = d
        return lead_time_stats_filters

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
