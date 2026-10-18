from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_evaluation_status import ForecastEvaluationStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.forecast_evaluation_skipped import ForecastEvaluationSkipped
    from ..models.forecast_scorecard import ForecastScorecard


T = TypeVar("T", bound="ForecastEvaluation")


@_attrs_define
class ForecastEvaluation:
    """A retrospective evaluation (103 job `forecast-evaluation`) and its scorecard

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster_id (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        status (ForecastEvaluationStatus):
        questions_considered (int):
        questions_evaluated (int):
        skipped (ForecastEvaluationSkipped): Skipped questions by reason
        caveats (list[Caveat]):
        created_by (str):
        created_at (datetime.datetime):
        categories (list[str] | Unset):
        max_questions (int | None | Unset):
        progress (float | None | Unset):
        scorecard (ForecastScorecard | None | Unset):
        error (None | str | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        manifest_id (None | str | Unset):
    """

    id: str
    forecaster_id: str
    from_: datetime.datetime
    to: datetime.datetime
    status: ForecastEvaluationStatus
    questions_considered: int
    questions_evaluated: int
    skipped: ForecastEvaluationSkipped
    caveats: list[Caveat]
    created_by: str
    created_at: datetime.datetime
    categories: list[str] | Unset = UNSET
    max_questions: int | None | Unset = UNSET
    progress: float | None | Unset = UNSET
    scorecard: ForecastScorecard | None | Unset = UNSET
    error: None | str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast_scorecard import ForecastScorecard

        id = self.id

        forecaster_id = self.forecaster_id

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        status = self.status.value

        questions_considered = self.questions_considered

        questions_evaluated = self.questions_evaluated

        skipped = self.skipped.to_dict()

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        created_by = self.created_by

        created_at = self.created_at.isoformat()

        categories: list[str] | Unset = UNSET
        if not isinstance(self.categories, Unset):
            categories = self.categories

        max_questions: int | None | Unset
        if isinstance(self.max_questions, Unset):
            max_questions = UNSET
        else:
            max_questions = self.max_questions

        progress: float | None | Unset
        if isinstance(self.progress, Unset):
            progress = UNSET
        else:
            progress = self.progress

        scorecard: dict[str, Any] | None | Unset
        if isinstance(self.scorecard, Unset):
            scorecard = UNSET
        elif isinstance(self.scorecard, ForecastScorecard):
            scorecard = self.scorecard.to_dict()
        else:
            scorecard = self.scorecard

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

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
                "forecasterId": forecaster_id,
                "from": from_,
                "to": to,
                "status": status,
                "questionsConsidered": questions_considered,
                "questionsEvaluated": questions_evaluated,
                "skipped": skipped,
                "caveats": caveats,
                "createdBy": created_by,
                "createdAt": created_at,
            }
        )
        if categories is not UNSET:
            field_dict["categories"] = categories
        if max_questions is not UNSET:
            field_dict["maxQuestions"] = max_questions
        if progress is not UNSET:
            field_dict["progress"] = progress
        if scorecard is not UNSET:
            field_dict["scorecard"] = scorecard
        if error is not UNSET:
            field_dict["error"] = error
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.forecast_evaluation_skipped import ForecastEvaluationSkipped
        from ..models.forecast_scorecard import ForecastScorecard

        d = dict(src_dict)
        id = d.pop("id")

        forecaster_id = d.pop("forecasterId")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        status = ForecastEvaluationStatus(d.pop("status"))

        questions_considered = d.pop("questionsConsidered")

        questions_evaluated = d.pop("questionsEvaluated")

        skipped = ForecastEvaluationSkipped.from_dict(d.pop("skipped"))

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        created_by = d.pop("createdBy")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        categories = cast(list[str], d.pop("categories", UNSET))

        def _parse_max_questions(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_questions = _parse_max_questions(d.pop("maxQuestions", UNSET))

        def _parse_progress(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        progress = _parse_progress(d.pop("progress", UNSET))

        def _parse_scorecard(data: object) -> ForecastScorecard | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                scorecard_type_0 = ForecastScorecard.from_dict(data)

                return scorecard_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastScorecard | None | Unset, data)

        scorecard = _parse_scorecard(d.pop("scorecard", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

        def _parse_finished_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                finished_at_type_0 = datetime.datetime.fromisoformat(data)

                return finished_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        finished_at = _parse_finished_at(d.pop("finishedAt", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        forecast_evaluation = cls(
            id=id,
            forecaster_id=forecaster_id,
            from_=from_,
            to=to,
            status=status,
            questions_considered=questions_considered,
            questions_evaluated=questions_evaluated,
            skipped=skipped,
            caveats=caveats,
            created_by=created_by,
            created_at=created_at,
            categories=categories,
            max_questions=max_questions,
            progress=progress,
            scorecard=scorecard,
            error=error,
            started_at=started_at,
            finished_at=finished_at,
            manifest_id=manifest_id,
        )

        forecast_evaluation.additional_properties = d
        return forecast_evaluation

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
