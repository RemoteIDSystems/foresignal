from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_score_interval_hits_type_0 import (
        ForecastScoreIntervalHitsType0,
    )
    from ..models.forecast_score_pinball_type_0 import ForecastScorePinballType0


T = TypeVar("T", bound="ForecastScore")


@_attrs_define
class ForecastScore:
    """Scores of one forecaster on one resolved question

    Attributes:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster_id (str):
        n_days (int):
        scorer_version (str):
        computed_at (datetime.datetime):
        brier (float | None | Unset):
        log_score (float | None | Unset):
        rps (float | None | Unset):
        crps (float | None | Unset):
        pinball (ForecastScorePinballType0 | None | Unset): Pinball loss per quantile level
        interval_hits (ForecastScoreIntervalHitsType0 | None | Unset): Whether the outcome fell inside the central
            intervals (numeric)
        time_averaged_brier (float | None | Unset):
        final_probability (float | None | Unset):
    """

    question_id: str
    forecaster_id: str
    n_days: int
    scorer_version: str
    computed_at: datetime.datetime
    brier: float | None | Unset = UNSET
    log_score: float | None | Unset = UNSET
    rps: float | None | Unset = UNSET
    crps: float | None | Unset = UNSET
    pinball: ForecastScorePinballType0 | None | Unset = UNSET
    interval_hits: ForecastScoreIntervalHitsType0 | None | Unset = UNSET
    time_averaged_brier: float | None | Unset = UNSET
    final_probability: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast_score_interval_hits_type_0 import (
            ForecastScoreIntervalHitsType0,
        )
        from ..models.forecast_score_pinball_type_0 import ForecastScorePinballType0

        question_id = self.question_id

        forecaster_id = self.forecaster_id

        n_days = self.n_days

        scorer_version = self.scorer_version

        computed_at = self.computed_at.isoformat()

        brier: float | None | Unset
        if isinstance(self.brier, Unset):
            brier = UNSET
        else:
            brier = self.brier

        log_score: float | None | Unset
        if isinstance(self.log_score, Unset):
            log_score = UNSET
        else:
            log_score = self.log_score

        rps: float | None | Unset
        if isinstance(self.rps, Unset):
            rps = UNSET
        else:
            rps = self.rps

        crps: float | None | Unset
        if isinstance(self.crps, Unset):
            crps = UNSET
        else:
            crps = self.crps

        pinball: dict[str, Any] | None | Unset
        if isinstance(self.pinball, Unset):
            pinball = UNSET
        elif isinstance(self.pinball, ForecastScorePinballType0):
            pinball = self.pinball.to_dict()
        else:
            pinball = self.pinball

        interval_hits: dict[str, Any] | None | Unset
        if isinstance(self.interval_hits, Unset):
            interval_hits = UNSET
        elif isinstance(self.interval_hits, ForecastScoreIntervalHitsType0):
            interval_hits = self.interval_hits.to_dict()
        else:
            interval_hits = self.interval_hits

        time_averaged_brier: float | None | Unset
        if isinstance(self.time_averaged_brier, Unset):
            time_averaged_brier = UNSET
        else:
            time_averaged_brier = self.time_averaged_brier

        final_probability: float | None | Unset
        if isinstance(self.final_probability, Unset):
            final_probability = UNSET
        else:
            final_probability = self.final_probability

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "questionId": question_id,
                "forecasterId": forecaster_id,
                "nDays": n_days,
                "scorerVersion": scorer_version,
                "computedAt": computed_at,
            }
        )
        if brier is not UNSET:
            field_dict["brier"] = brier
        if log_score is not UNSET:
            field_dict["logScore"] = log_score
        if rps is not UNSET:
            field_dict["rps"] = rps
        if crps is not UNSET:
            field_dict["crps"] = crps
        if pinball is not UNSET:
            field_dict["pinball"] = pinball
        if interval_hits is not UNSET:
            field_dict["intervalHits"] = interval_hits
        if time_averaged_brier is not UNSET:
            field_dict["timeAveragedBrier"] = time_averaged_brier
        if final_probability is not UNSET:
            field_dict["finalProbability"] = final_probability

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_score_interval_hits_type_0 import (
            ForecastScoreIntervalHitsType0,
        )
        from ..models.forecast_score_pinball_type_0 import ForecastScorePinballType0

        d = dict(src_dict)
        question_id = d.pop("questionId")

        forecaster_id = d.pop("forecasterId")

        n_days = d.pop("nDays")

        scorer_version = d.pop("scorerVersion")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_brier(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        brier = _parse_brier(d.pop("brier", UNSET))

        def _parse_log_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        log_score = _parse_log_score(d.pop("logScore", UNSET))

        def _parse_rps(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        rps = _parse_rps(d.pop("rps", UNSET))

        def _parse_crps(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        crps = _parse_crps(d.pop("crps", UNSET))

        def _parse_pinball(data: object) -> ForecastScorePinballType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                pinball_type_0 = ForecastScorePinballType0.from_dict(data)

                return pinball_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastScorePinballType0 | None | Unset, data)

        pinball = _parse_pinball(d.pop("pinball", UNSET))

        def _parse_interval_hits(
            data: object,
        ) -> ForecastScoreIntervalHitsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                interval_hits_type_0 = ForecastScoreIntervalHitsType0.from_dict(data)

                return interval_hits_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastScoreIntervalHitsType0 | None | Unset, data)

        interval_hits = _parse_interval_hits(d.pop("intervalHits", UNSET))

        def _parse_time_averaged_brier(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        time_averaged_brier = _parse_time_averaged_brier(
            d.pop("timeAveragedBrier", UNSET)
        )

        def _parse_final_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        final_probability = _parse_final_probability(d.pop("finalProbability", UNSET))

        forecast_score = cls(
            question_id=question_id,
            forecaster_id=forecaster_id,
            n_days=n_days,
            scorer_version=scorer_version,
            computed_at=computed_at,
            brier=brier,
            log_score=log_score,
            rps=rps,
            crps=crps,
            pinball=pinball,
            interval_hits=interval_hits,
            time_averaged_brier=time_averaged_brier,
            final_probability=final_probability,
        )

        forecast_score.additional_properties = d
        return forecast_score

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
