from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

T = TypeVar("T", bound="SourceRelevanceRequest")


@_attrs_define
class SourceRelevanceRequest:
    """
    Attributes:
        source_ids (list[str]): Registered source ids, the candidate dataset of a rule (as many as
            `TriggerRuleInput.sources` takes) Example: ['usgs-earthquakes', 'emsc-earthquakes', 'nws-alerts'].
    """

    source_ids: list[str]

    def to_dict(self) -> dict[str, Any]:
        source_ids = self.source_ids

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "sourceIds": source_ids,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_ids = cast(list[str], d.pop("sourceIds"))

        source_relevance_request = cls(
            source_ids=source_ids,
        )

        return source_relevance_request
