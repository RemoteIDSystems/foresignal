from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_resolver import ForecastResolver


T = TypeVar("T", bound="ForecastQuestionPatch")


@_attrs_define
class ForecastQuestionPatch:
    """JSON merge patch for a question; criteria and resolver are immutable once a forecast exists

    Attributes:
        title (str | Unset):
        tags (list[str] | Unset):
        closes_at (datetime.datetime | Unset):
        resolution_criteria (str | Unset):
        resolver (ForecastResolver | Unset): Machine resolver (pure, versioned, validated) or `{type: manual}`; shown
            verbatim to every forecaster
    """

    title: str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    closes_at: datetime.datetime | Unset = UNSET
    resolution_criteria: str | Unset = UNSET
    resolver: ForecastResolver | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        closes_at: str | Unset = UNSET
        if not isinstance(self.closes_at, Unset):
            closes_at = self.closes_at.isoformat()

        resolution_criteria = self.resolution_criteria

        resolver: dict[str, Any] | Unset = UNSET
        if not isinstance(self.resolver, Unset):
            resolver = self.resolver.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if tags is not UNSET:
            field_dict["tags"] = tags
        if closes_at is not UNSET:
            field_dict["closesAt"] = closes_at
        if resolution_criteria is not UNSET:
            field_dict["resolutionCriteria"] = resolution_criteria
        if resolver is not UNSET:
            field_dict["resolver"] = resolver

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_resolver import ForecastResolver

        d = dict(src_dict)
        title = d.pop("title", UNSET)

        tags = cast(list[str], d.pop("tags", UNSET))

        _closes_at = d.pop("closesAt", UNSET)
        closes_at: datetime.datetime | Unset
        if isinstance(_closes_at, Unset):
            closes_at = UNSET
        else:
            closes_at = datetime.datetime.fromisoformat(_closes_at)

        resolution_criteria = d.pop("resolutionCriteria", UNSET)

        _resolver = d.pop("resolver", UNSET)
        resolver: ForecastResolver | Unset
        if isinstance(_resolver, Unset):
            resolver = UNSET
        else:
            resolver = ForecastResolver.from_dict(_resolver)

        forecast_question_patch = cls(
            title=title,
            tags=tags,
            closes_at=closes_at,
            resolution_criteria=resolution_criteria,
            resolver=resolver,
        )

        forecast_question_patch.additional_properties = d
        return forecast_question_patch

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
