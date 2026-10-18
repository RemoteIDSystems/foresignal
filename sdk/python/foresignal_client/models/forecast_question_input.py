from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_question_input_type import ForecastQuestionInputType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_resolver import ForecastResolver
    from ..models.question_links import QuestionLinks
    from ..models.question_outcomes import QuestionOutcomes


T = TypeVar("T", bound="ForecastQuestionInput")


@_attrs_define
class ForecastQuestionInput:
    """Open a resolvable forecast question

    Attributes:
        title (str):
        type_ (ForecastQuestionInputType):
        resolution_criteria (str): Shown verbatim to every forecaster
        resolver (ForecastResolver): Machine resolver (pure, versioned, validated) or `{type: manual}`; shown verbatim
            to every forecaster
        closes_at (datetime.datetime):
        resolve_by (datetime.datetime):
        category (str):
        outcomes (QuestionOutcomes | Unset): Outcome space: labels for multiple/ordered questions, unit and bounds for
            numeric
        opens_at (datetime.datetime | Unset): Default: now
        tags (list[str] | Unset):
        links (QuestionLinks | Unset): Objects a question is about
        dedup_key (None | str | Unset):
        private (bool | Unset):  Default: False.
    """

    title: str
    type_: ForecastQuestionInputType
    resolution_criteria: str
    resolver: ForecastResolver
    closes_at: datetime.datetime
    resolve_by: datetime.datetime
    category: str
    outcomes: QuestionOutcomes | Unset = UNSET
    opens_at: datetime.datetime | Unset = UNSET
    tags: list[str] | Unset = UNSET
    links: QuestionLinks | Unset = UNSET
    dedup_key: None | str | Unset = UNSET
    private: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        type_ = self.type_.value

        resolution_criteria = self.resolution_criteria

        resolver = self.resolver.to_dict()

        closes_at = self.closes_at.isoformat()

        resolve_by = self.resolve_by.isoformat()

        category = self.category

        outcomes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.outcomes, Unset):
            outcomes = self.outcomes.to_dict()

        opens_at: str | Unset = UNSET
        if not isinstance(self.opens_at, Unset):
            opens_at = self.opens_at.isoformat()

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        dedup_key: None | str | Unset
        if isinstance(self.dedup_key, Unset):
            dedup_key = UNSET
        else:
            dedup_key = self.dedup_key

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "title": title,
                "type": type_,
                "resolutionCriteria": resolution_criteria,
                "resolver": resolver,
                "closesAt": closes_at,
                "resolveBy": resolve_by,
                "category": category,
            }
        )
        if outcomes is not UNSET:
            field_dict["outcomes"] = outcomes
        if opens_at is not UNSET:
            field_dict["opensAt"] = opens_at
        if tags is not UNSET:
            field_dict["tags"] = tags
        if links is not UNSET:
            field_dict["links"] = links
        if dedup_key is not UNSET:
            field_dict["dedupKey"] = dedup_key
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_resolver import ForecastResolver
        from ..models.question_links import QuestionLinks
        from ..models.question_outcomes import QuestionOutcomes

        d = dict(src_dict)
        title = d.pop("title")

        type_ = ForecastQuestionInputType(d.pop("type"))

        resolution_criteria = d.pop("resolutionCriteria")

        resolver = ForecastResolver.from_dict(d.pop("resolver"))

        closes_at = datetime.datetime.fromisoformat(d.pop("closesAt"))

        resolve_by = datetime.datetime.fromisoformat(d.pop("resolveBy"))

        category = d.pop("category")

        _outcomes = d.pop("outcomes", UNSET)
        outcomes: QuestionOutcomes | Unset
        if isinstance(_outcomes, Unset):
            outcomes = UNSET
        else:
            outcomes = QuestionOutcomes.from_dict(_outcomes)

        _opens_at = d.pop("opensAt", UNSET)
        opens_at: datetime.datetime | Unset
        if isinstance(_opens_at, Unset):
            opens_at = UNSET
        else:
            opens_at = datetime.datetime.fromisoformat(_opens_at)

        tags = cast(list[str], d.pop("tags", UNSET))

        _links = d.pop("links", UNSET)
        links: QuestionLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = QuestionLinks.from_dict(_links)

        def _parse_dedup_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        dedup_key = _parse_dedup_key(d.pop("dedupKey", UNSET))

        private = d.pop("private", UNSET)

        forecast_question_input = cls(
            title=title,
            type_=type_,
            resolution_criteria=resolution_criteria,
            resolver=resolver,
            closes_at=closes_at,
            resolve_by=resolve_by,
            category=category,
            outcomes=outcomes,
            opens_at=opens_at,
            tags=tags,
            links=links,
            dedup_key=dedup_key,
            private=private,
        )

        forecast_question_input.additional_properties = d
        return forecast_question_input

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
