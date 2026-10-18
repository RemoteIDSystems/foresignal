from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialLeadTimes")


@_attrs_define
class SocialLeadTimes:
    """First observations and signed lead of social first observation (positive = social first)

    Attributes:
        social_first (datetime.datetime):
        physical_first (datetime.datetime | None | Unset):
        news_first (datetime.datetime | None | Unset):
        official_first (datetime.datetime | None | Unset):
        lead_over_physical_seconds (float | None | Unset):
        lead_over_news_seconds (float | None | Unset):
        lead_over_official_seconds (float | None | Unset):
    """

    social_first: datetime.datetime
    physical_first: datetime.datetime | None | Unset = UNSET
    news_first: datetime.datetime | None | Unset = UNSET
    official_first: datetime.datetime | None | Unset = UNSET
    lead_over_physical_seconds: float | None | Unset = UNSET
    lead_over_news_seconds: float | None | Unset = UNSET
    lead_over_official_seconds: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        social_first = self.social_first.isoformat()

        physical_first: None | str | Unset
        if isinstance(self.physical_first, Unset):
            physical_first = UNSET
        elif isinstance(self.physical_first, datetime.datetime):
            physical_first = self.physical_first.isoformat()
        else:
            physical_first = self.physical_first

        news_first: None | str | Unset
        if isinstance(self.news_first, Unset):
            news_first = UNSET
        elif isinstance(self.news_first, datetime.datetime):
            news_first = self.news_first.isoformat()
        else:
            news_first = self.news_first

        official_first: None | str | Unset
        if isinstance(self.official_first, Unset):
            official_first = UNSET
        elif isinstance(self.official_first, datetime.datetime):
            official_first = self.official_first.isoformat()
        else:
            official_first = self.official_first

        lead_over_physical_seconds: float | None | Unset
        if isinstance(self.lead_over_physical_seconds, Unset):
            lead_over_physical_seconds = UNSET
        else:
            lead_over_physical_seconds = self.lead_over_physical_seconds

        lead_over_news_seconds: float | None | Unset
        if isinstance(self.lead_over_news_seconds, Unset):
            lead_over_news_seconds = UNSET
        else:
            lead_over_news_seconds = self.lead_over_news_seconds

        lead_over_official_seconds: float | None | Unset
        if isinstance(self.lead_over_official_seconds, Unset):
            lead_over_official_seconds = UNSET
        else:
            lead_over_official_seconds = self.lead_over_official_seconds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "socialFirst": social_first,
            }
        )
        if physical_first is not UNSET:
            field_dict["physicalFirst"] = physical_first
        if news_first is not UNSET:
            field_dict["newsFirst"] = news_first
        if official_first is not UNSET:
            field_dict["officialFirst"] = official_first
        if lead_over_physical_seconds is not UNSET:
            field_dict["leadOverPhysicalSeconds"] = lead_over_physical_seconds
        if lead_over_news_seconds is not UNSET:
            field_dict["leadOverNewsSeconds"] = lead_over_news_seconds
        if lead_over_official_seconds is not UNSET:
            field_dict["leadOverOfficialSeconds"] = lead_over_official_seconds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        social_first = datetime.datetime.fromisoformat(d.pop("socialFirst"))

        def _parse_physical_first(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                physical_first_type_0 = datetime.datetime.fromisoformat(data)

                return physical_first_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        physical_first = _parse_physical_first(d.pop("physicalFirst", UNSET))

        def _parse_news_first(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                news_first_type_0 = datetime.datetime.fromisoformat(data)

                return news_first_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        news_first = _parse_news_first(d.pop("newsFirst", UNSET))

        def _parse_official_first(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                official_first_type_0 = datetime.datetime.fromisoformat(data)

                return official_first_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        official_first = _parse_official_first(d.pop("officialFirst", UNSET))

        def _parse_lead_over_physical_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lead_over_physical_seconds = _parse_lead_over_physical_seconds(
            d.pop("leadOverPhysicalSeconds", UNSET)
        )

        def _parse_lead_over_news_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lead_over_news_seconds = _parse_lead_over_news_seconds(
            d.pop("leadOverNewsSeconds", UNSET)
        )

        def _parse_lead_over_official_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lead_over_official_seconds = _parse_lead_over_official_seconds(
            d.pop("leadOverOfficialSeconds", UNSET)
        )

        social_lead_times = cls(
            social_first=social_first,
            physical_first=physical_first,
            news_first=news_first,
            official_first=official_first,
            lead_over_physical_seconds=lead_over_physical_seconds,
            lead_over_news_seconds=lead_over_news_seconds,
            lead_over_official_seconds=lead_over_official_seconds,
        )

        social_lead_times.additional_properties = d
        return social_lead_times

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
