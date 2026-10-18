from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_question_origin import ForecastQuestionOrigin
from ..models.forecast_question_resolver_type import ForecastQuestionResolverType
from ..models.forecast_question_status import ForecastQuestionStatus
from ..models.forecast_question_type import ForecastQuestionType
from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast import Forecast
    from ..models.forecast_question_baseline_notes import ForecastQuestionBaselineNotes
    from ..models.forecast_resolution import ForecastResolution
    from ..models.forecast_resolver import ForecastResolver
    from ..models.forecast_score import ForecastScore
    from ..models.question_annulment import QuestionAnnulment
    from ..models.question_links import QuestionLinks
    from ..models.question_outcomes import QuestionOutcomes


T = TypeVar("T", bound="ForecastQuestion")


@_attrs_define
class ForecastQuestion:
    """A forecast question with criteria, resolver, latest forecasts per forecaster, ensemble, ICD term and scores

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        type_ (ForecastQuestionType):
        status (ForecastQuestionStatus):
        category (str): e.g. `conflict`, `maritime`, `markets`, `weather-impact`, `activity`
        resolver_type (ForecastQuestionResolverType):
        opens_at (datetime.datetime):
        closes_at (datetime.datetime):
        resolve_by (datetime.datetime):
        origin (ForecastQuestionOrigin):
        private (bool):
        resolution_criteria (str):
        resolver (ForecastResolver): Machine resolver (pure, versioned, validated) or `{type: manual}`; shown verbatim
            to every forecaster
        latest_forecasts (list[Forecast]): Latest forecast per forecaster, newest first
        tags (list[str] | Unset):
        links (QuestionLinks | Unset): Objects a question is about
        forecast_count (int | Unset):
        ensemble_probability (float | None | Unset): Latest ensemble probability (binary questions)
        estimative_term (IcdTerm | None | Unset):
        resolved_outcome (None | str | Unset):
        resolved_value (float | None | Unset):
        outcomes (QuestionOutcomes | Unset): Outcome space: labels for multiple/ordered questions, unit and bounds for
            numeric
        resolution (ForecastResolution | None | Unset):
        annulment (None | QuestionAnnulment | Unset):
        dedup_key (None | str | Unset):
        ensemble (Forecast | None | Unset):
        scores (list[ForecastScore] | Unset): Present once resolved
        created_at (datetime.datetime | Unset):
        updated_at (datetime.datetime | Unset):
        baseline_notes (ForecastQuestionBaselineNotes | Unset): Why a baseline forecaster that supports this kind of
            question forecast nothing, by forecaster id (e.g. no reference family, no covered history)
        manifest_id (None | str | Unset): 116 manifest of the question's opening
    """

    id: str
    title: str
    type_: ForecastQuestionType
    status: ForecastQuestionStatus
    category: str
    resolver_type: ForecastQuestionResolverType
    opens_at: datetime.datetime
    closes_at: datetime.datetime
    resolve_by: datetime.datetime
    origin: ForecastQuestionOrigin
    private: bool
    resolution_criteria: str
    resolver: ForecastResolver
    latest_forecasts: list[Forecast]
    tags: list[str] | Unset = UNSET
    links: QuestionLinks | Unset = UNSET
    forecast_count: int | Unset = UNSET
    ensemble_probability: float | None | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    resolved_outcome: None | str | Unset = UNSET
    resolved_value: float | None | Unset = UNSET
    outcomes: QuestionOutcomes | Unset = UNSET
    resolution: ForecastResolution | None | Unset = UNSET
    annulment: None | QuestionAnnulment | Unset = UNSET
    dedup_key: None | str | Unset = UNSET
    ensemble: Forecast | None | Unset = UNSET
    scores: list[ForecastScore] | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    baseline_notes: ForecastQuestionBaselineNotes | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast import Forecast
        from ..models.forecast_resolution import ForecastResolution
        from ..models.question_annulment import QuestionAnnulment

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

        resolution_criteria = self.resolution_criteria

        resolver = self.resolver.to_dict()

        latest_forecasts = []
        for latest_forecasts_item_data in self.latest_forecasts:
            latest_forecasts_item = latest_forecasts_item_data.to_dict()
            latest_forecasts.append(latest_forecasts_item)

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

        outcomes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.outcomes, Unset):
            outcomes = self.outcomes.to_dict()

        resolution: dict[str, Any] | None | Unset
        if isinstance(self.resolution, Unset):
            resolution = UNSET
        elif isinstance(self.resolution, ForecastResolution):
            resolution = self.resolution.to_dict()
        else:
            resolution = self.resolution

        annulment: dict[str, Any] | None | Unset
        if isinstance(self.annulment, Unset):
            annulment = UNSET
        elif isinstance(self.annulment, QuestionAnnulment):
            annulment = self.annulment.to_dict()
        else:
            annulment = self.annulment

        dedup_key: None | str | Unset
        if isinstance(self.dedup_key, Unset):
            dedup_key = UNSET
        else:
            dedup_key = self.dedup_key

        ensemble: dict[str, Any] | None | Unset
        if isinstance(self.ensemble, Unset):
            ensemble = UNSET
        elif isinstance(self.ensemble, Forecast):
            ensemble = self.ensemble.to_dict()
        else:
            ensemble = self.ensemble

        scores: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.scores, Unset):
            scores = []
            for scores_item_data in self.scores:
                scores_item = scores_item_data.to_dict()
                scores.append(scores_item)

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        baseline_notes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.baseline_notes, Unset):
            baseline_notes = self.baseline_notes.to_dict()

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

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
                "resolutionCriteria": resolution_criteria,
                "resolver": resolver,
                "latestForecasts": latest_forecasts,
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
        if outcomes is not UNSET:
            field_dict["outcomes"] = outcomes
        if resolution is not UNSET:
            field_dict["resolution"] = resolution
        if annulment is not UNSET:
            field_dict["annulment"] = annulment
        if dedup_key is not UNSET:
            field_dict["dedupKey"] = dedup_key
        if ensemble is not UNSET:
            field_dict["ensemble"] = ensemble
        if scores is not UNSET:
            field_dict["scores"] = scores
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if baseline_notes is not UNSET:
            field_dict["baselineNotes"] = baseline_notes
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast import Forecast
        from ..models.forecast_question_baseline_notes import (
            ForecastQuestionBaselineNotes,
        )
        from ..models.forecast_resolution import ForecastResolution
        from ..models.forecast_resolver import ForecastResolver
        from ..models.forecast_score import ForecastScore
        from ..models.question_annulment import QuestionAnnulment
        from ..models.question_links import QuestionLinks
        from ..models.question_outcomes import QuestionOutcomes

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        type_ = ForecastQuestionType(d.pop("type"))

        status = ForecastQuestionStatus(d.pop("status"))

        category = d.pop("category")

        resolver_type = ForecastQuestionResolverType(d.pop("resolverType"))

        opens_at = datetime.datetime.fromisoformat(d.pop("opensAt"))

        closes_at = datetime.datetime.fromisoformat(d.pop("closesAt"))

        resolve_by = datetime.datetime.fromisoformat(d.pop("resolveBy"))

        origin = ForecastQuestionOrigin(d.pop("origin"))

        private = d.pop("private")

        resolution_criteria = d.pop("resolutionCriteria")

        resolver = ForecastResolver.from_dict(d.pop("resolver"))

        latest_forecasts = []
        _latest_forecasts = d.pop("latestForecasts")
        for latest_forecasts_item_data in _latest_forecasts:
            latest_forecasts_item = Forecast.from_dict(latest_forecasts_item_data)

            latest_forecasts.append(latest_forecasts_item)

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

        _outcomes = d.pop("outcomes", UNSET)
        outcomes: QuestionOutcomes | Unset
        if isinstance(_outcomes, Unset):
            outcomes = UNSET
        else:
            outcomes = QuestionOutcomes.from_dict(_outcomes)

        def _parse_resolution(data: object) -> ForecastResolution | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                resolution_type_0 = ForecastResolution.from_dict(data)

                return resolution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastResolution | None | Unset, data)

        resolution = _parse_resolution(d.pop("resolution", UNSET))

        def _parse_annulment(data: object) -> None | QuestionAnnulment | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                annulment_type_0 = QuestionAnnulment.from_dict(data)

                return annulment_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | QuestionAnnulment | Unset, data)

        annulment = _parse_annulment(d.pop("annulment", UNSET))

        def _parse_dedup_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        dedup_key = _parse_dedup_key(d.pop("dedupKey", UNSET))

        def _parse_ensemble(data: object) -> Forecast | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                ensemble_type_0 = Forecast.from_dict(data)

                return ensemble_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Forecast | None | Unset, data)

        ensemble = _parse_ensemble(d.pop("ensemble", UNSET))

        _scores = d.pop("scores", UNSET)
        scores: list[ForecastScore] | Unset = UNSET
        if _scores is not UNSET:
            scores = []
            for scores_item_data in _scores:
                scores_item = ForecastScore.from_dict(scores_item_data)

                scores.append(scores_item)

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        _baseline_notes = d.pop("baselineNotes", UNSET)
        baseline_notes: ForecastQuestionBaselineNotes | Unset
        if isinstance(_baseline_notes, Unset):
            baseline_notes = UNSET
        else:
            baseline_notes = ForecastQuestionBaselineNotes.from_dict(_baseline_notes)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        forecast_question = cls(
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
            resolution_criteria=resolution_criteria,
            resolver=resolver,
            latest_forecasts=latest_forecasts,
            tags=tags,
            links=links,
            forecast_count=forecast_count,
            ensemble_probability=ensemble_probability,
            estimative_term=estimative_term,
            resolved_outcome=resolved_outcome,
            resolved_value=resolved_value,
            outcomes=outcomes,
            resolution=resolution,
            annulment=annulment,
            dedup_key=dedup_key,
            ensemble=ensemble,
            scores=scores,
            created_at=created_at,
            updated_at=updated_at,
            baseline_notes=baseline_notes,
            manifest_id=manifest_id,
        )

        forecast_question.additional_properties = d
        return forecast_question

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
