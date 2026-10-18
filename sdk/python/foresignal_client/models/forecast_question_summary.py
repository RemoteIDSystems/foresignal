from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_question_summary_origin import ForecastQuestionSummaryOrigin
from ..models.forecast_question_summary_resolver_type import (
    ForecastQuestionSummaryResolverType,
)
from ..models.forecast_question_summary_status import ForecastQuestionSummaryStatus
from ..models.forecast_question_summary_type import ForecastQuestionSummaryType
from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.question_links import QuestionLinks


T = TypeVar("T", bound="ForecastQuestionSummary")


@_attrs_define
class ForecastQuestionSummary:
    """Compact forecast question for lists and SSE events

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        type_ (ForecastQuestionSummaryType):
        status (ForecastQuestionSummaryStatus):
        category (str): e.g. `conflict`, `maritime`, `markets`, `weather-impact`, `activity`
        resolver_type (ForecastQuestionSummaryResolverType):
        opens_at (datetime.datetime):
        closes_at (datetime.datetime):
        resolve_by (datetime.datetime):
        origin (ForecastQuestionSummaryOrigin):
        private (bool):
        tags (list[str] | Unset):
        links (QuestionLinks | Unset): Objects a question is about
        forecast_count (int | Unset):
        ensemble_probability (float | None | Unset): Latest ensemble probability (binary questions)
        estimative_term (IcdTerm | None | Unset):
        resolved_outcome (None | str | Unset):
        resolved_value (float | None | Unset):
    """

    id: str
    title: str
    type_: ForecastQuestionSummaryType
    status: ForecastQuestionSummaryStatus
    category: str
    resolver_type: ForecastQuestionSummaryResolverType
    opens_at: datetime.datetime
    closes_at: datetime.datetime
    resolve_by: datetime.datetime
    origin: ForecastQuestionSummaryOrigin
    private: bool
    tags: list[str] | Unset = UNSET
    links: QuestionLinks | Unset = UNSET
    forecast_count: int | Unset = UNSET
    ensemble_probability: float | None | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    resolved_outcome: None | str | Unset = UNSET
    resolved_value: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        title = self.title

        type_ = self.type_.value

        status = self.status.value

        category = self.category

        resolver_type = self.resolver_type.value

        opens_at = self.opens_at.isoformat()

        closes_at = self.closes_at.isoformat()

        resolve_by = self.resolve_by.isoformat()

        origin = self.origin.value

        private = self.private

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        forecast_count = self.forecast_count

        ensemble_probability: float | None | Unset
        if isinstance(self.ensemble_probability, Unset):
            ensemble_probability = UNSET
        else:
            ensemble_probability = self.ensemble_probability

        estimative_term: None | str | Unset
        if isinstance(self.estimative_term, Unset):
            estimative_term = UNSET
        elif isinstance(self.estimative_term, IcdTerm):
            estimative_term = self.estimative_term.value
        else:
            estimative_term = self.estimative_term

        resolved_outcome: None | str | Unset
        if isinstance(self.resolved_outcome, Unset):
            resolved_outcome = UNSET
        else:
            resolved_outcome = self.resolved_outcome

        resolved_value: float | None | Unset
        if isinstance(self.resolved_value, Unset):
            resolved_value = UNSET
        else:
            resolved_value = self.resolved_value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "type": type_,
                "status": status,
                "category": category,
                "resolverType": resolver_type,
                "opensAt": opens_at,
                "closesAt": closes_at,
                "resolveBy": resolve_by,
                "origin": origin,
                "private": private,
            }
        )
        if tags is not UNSET:
            field_dict["tags"] = tags
        if links is not UNSET:
            field_dict["links"] = links
        if forecast_count is not UNSET:
            field_dict["forecastCount"] = forecast_count
        if ensemble_probability is not UNSET:
            field_dict["ensembleProbability"] = ensemble_probability
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term
        if resolved_outcome is not UNSET:
            field_dict["resolvedOutcome"] = resolved_outcome
        if resolved_value is not UNSET:
            field_dict["resolvedValue"] = resolved_value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.question_links import QuestionLinks

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        type_ = ForecastQuestionSummaryType(d.pop("type"))

        status = ForecastQuestionSummaryStatus(d.pop("status"))

        category = d.pop("category")

        resolver_type = ForecastQuestionSummaryResolverType(d.pop("resolverType"))

        opens_at = datetime.datetime.fromisoformat(d.pop("opensAt"))

        closes_at = datetime.datetime.fromisoformat(d.pop("closesAt"))

        resolve_by = datetime.datetime.fromisoformat(d.pop("resolveBy"))

        origin = ForecastQuestionSummaryOrigin(d.pop("origin"))

        private = d.pop("private")

        tags = cast(list[str], d.pop("tags", UNSET))

        _links = d.pop("links", UNSET)
        links: QuestionLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = QuestionLinks.from_dict(_links)

        forecast_count = d.pop("forecastCount", UNSET)

        def _parse_ensemble_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        ensemble_probability = _parse_ensemble_probability(
            d.pop("ensembleProbability", UNSET)
        )

        def _parse_estimative_term(data: object) -> IcdTerm | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                estimative_term_type_0 = IcdTerm(data)

                return estimative_term_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IcdTerm | None | Unset, data)

        estimative_term = _parse_estimative_term(d.pop("estimativeTerm", UNSET))

        def _parse_resolved_outcome(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resolved_outcome = _parse_resolved_outcome(d.pop("resolvedOutcome", UNSET))

        def _parse_resolved_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        resolved_value = _parse_resolved_value(d.pop("resolvedValue", UNSET))

        forecast_question_summary = cls(
            id=id,
            title=title,
            type_=type_,
            status=status,
            category=category,
            resolver_type=resolver_type,
            opens_at=opens_at,
            closes_at=closes_at,
            resolve_by=resolve_by,
            origin=origin,
            private=private,
            tags=tags,
            links=links,
            forecast_count=forecast_count,
            ensemble_probability=ensemble_probability,
            estimative_term=estimative_term,
            resolved_outcome=resolved_outcome,
            resolved_value=resolved_value,
        )

        forecast_question_summary.additional_properties = d
        return forecast_question_summary

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
