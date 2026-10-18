from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_platform_id import SocialPlatformId
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialCueQuery")


@_attrs_define
class SocialCueQuery:
    """One exact query run on one platform

    Attributes:
        platform (SocialPlatformId): Social platform adapter (a 004 source with descriptor, budget, health and
            credentials). Only `bluesky-jetstream` is on by default (135)
        query (str): Exact query string sent
        language (None | str | Unset):
        estimated_cost_usd (float | None | Unset):
    """

    platform: SocialPlatformId
    query: str
    language: None | str | Unset = UNSET
    estimated_cost_usd: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform.value

        query = self.query

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        estimated_cost_usd: float | None | Unset
        if isinstance(self.estimated_cost_usd, Unset):
            estimated_cost_usd = UNSET
        else:
            estimated_cost_usd = self.estimated_cost_usd

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "query": query,
            }
        )
        if language is not UNSET:
            field_dict["language"] = language
        if estimated_cost_usd is not UNSET:
            field_dict["estimatedCostUsd"] = estimated_cost_usd

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        platform = SocialPlatformId(d.pop("platform"))

        query = d.pop("query")

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        def _parse_estimated_cost_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        estimated_cost_usd = _parse_estimated_cost_usd(d.pop("estimatedCostUsd", UNSET))

        social_cue_query = cls(
            platform=platform,
            query=query,
            language=language,
            estimated_cost_usd=estimated_cost_usd,
        )

        social_cue_query.additional_properties = d
        return social_cue_query

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
