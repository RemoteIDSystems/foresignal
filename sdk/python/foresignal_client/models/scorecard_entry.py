from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.scorecard_entry_label import ScorecardEntryLabel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.scorecard_entry_bias_type_0 import ScorecardEntryBiasType0
    from ..models.scorecard_entry_interval_coverage import (
        ScorecardEntryIntervalCoverage,
    )
    from ..models.scorecard_entry_murphy_type_0 import ScorecardEntryMurphyType0
    from ..models.scorecard_entry_recalibration_type_0 import (
        ScorecardEntryRecalibrationType0,
    )
    from ..models.scorecard_entry_skill_type_0 import ScorecardEntrySkillType0


T = TypeVar("T", bound="ScorecardEntry")


@_attrs_define
class ScorecardEntry:
    """Scores, calibration, skill and bias of one forecaster in one category

    Attributes:
        forecaster_id (str):
        n_resolved (int):
        label (ScorecardEntryLabel): `insufficient` under 20 resolved questions
        forecaster_type (str | Unset): Expected to grow (`x-extensible-enum`): a client must accept a value not listed
            here.
        category (None | str | Unset): Null = all categories
        n_forecasts (int | Unset):
        brier (float | None | Unset):
        log_score (float | None | Unset):
        rps (float | None | Unset):
        crps (float | None | Unset):
        time_averaged_brier (float | None | Unset):
        interval_coverage (ScorecardEntryIntervalCoverage | Unset): Share of numeric outcomes inside central intervals
        murphy (None | ScorecardEntryMurphyType0 | Unset):
        skill (None | ScorecardEntrySkillType0 | Unset):
        bias (None | ScorecardEntryBiasType0 | Unset):
        recalibration (None | ScorecardEntryRecalibrationType0 | Unset):
        ensemble_weight (float | None | Unset):
    """

    forecaster_id: str
    n_resolved: int
    label: ScorecardEntryLabel
    forecaster_type: str | Unset = UNSET
    category: None | str | Unset = UNSET
    n_forecasts: int | Unset = UNSET
    brier: float | None | Unset = UNSET
    log_score: float | None | Unset = UNSET
    rps: float | None | Unset = UNSET
    crps: float | None | Unset = UNSET
    time_averaged_brier: float | None | Unset = UNSET
    interval_coverage: ScorecardEntryIntervalCoverage | Unset = UNSET
    murphy: None | ScorecardEntryMurphyType0 | Unset = UNSET
    skill: None | ScorecardEntrySkillType0 | Unset = UNSET
    bias: None | ScorecardEntryBiasType0 | Unset = UNSET
    recalibration: None | ScorecardEntryRecalibrationType0 | Unset = UNSET
    ensemble_weight: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.scorecard_entry_bias_type_0 import ScorecardEntryBiasType0
        from ..models.scorecard_entry_murphy_type_0 import ScorecardEntryMurphyType0
        from ..models.scorecard_entry_recalibration_type_0 import (
            ScorecardEntryRecalibrationType0,
        )
        from ..models.scorecard_entry_skill_type_0 import ScorecardEntrySkillType0

        forecaster_id = self.forecaster_id

        n_resolved = self.n_resolved

        label = self.label.value

        forecaster_type = self.forecaster_type

        category: None | str | Unset
        if isinstance(self.category, Unset):
            category = UNSET
        else:
            category = self.category

        n_forecasts = self.n_forecasts

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

        time_averaged_brier: float | None | Unset
        if isinstance(self.time_averaged_brier, Unset):
            time_averaged_brier = UNSET
        else:
            time_averaged_brier = self.time_averaged_brier

        interval_coverage: dict[str, Any] | Unset = UNSET
        if not isinstance(self.interval_coverage, Unset):
            interval_coverage = self.interval_coverage.to_dict()

        murphy: dict[str, Any] | None | Unset
        if isinstance(self.murphy, Unset):
            murphy = UNSET
        elif isinstance(self.murphy, ScorecardEntryMurphyType0):
            murphy = self.murphy.to_dict()
        else:
            murphy = self.murphy

        skill: dict[str, Any] | None | Unset
        if isinstance(self.skill, Unset):
            skill = UNSET
        elif isinstance(self.skill, ScorecardEntrySkillType0):
            skill = self.skill.to_dict()
        else:
            skill = self.skill

        bias: dict[str, Any] | None | Unset
        if isinstance(self.bias, Unset):
            bias = UNSET
        elif isinstance(self.bias, ScorecardEntryBiasType0):
            bias = self.bias.to_dict()
        else:
            bias = self.bias

        recalibration: dict[str, Any] | None | Unset
        if isinstance(self.recalibration, Unset):
            recalibration = UNSET
        elif isinstance(self.recalibration, ScorecardEntryRecalibrationType0):
            recalibration = self.recalibration.to_dict()
        else:
            recalibration = self.recalibration

        ensemble_weight: float | None | Unset
        if isinstance(self.ensemble_weight, Unset):
            ensemble_weight = UNSET
        else:
            ensemble_weight = self.ensemble_weight

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecasterId": forecaster_id,
                "nResolved": n_resolved,
                "label": label,
            }
        )
        if forecaster_type is not UNSET:
            field_dict["forecasterType"] = forecaster_type
        if category is not UNSET:
            field_dict["category"] = category
        if n_forecasts is not UNSET:
            field_dict["nForecasts"] = n_forecasts
        if brier is not UNSET:
            field_dict["brier"] = brier
        if log_score is not UNSET:
            field_dict["logScore"] = log_score
        if rps is not UNSET:
            field_dict["rps"] = rps
        if crps is not UNSET:
            field_dict["crps"] = crps
        if time_averaged_brier is not UNSET:
            field_dict["timeAveragedBrier"] = time_averaged_brier
        if interval_coverage is not UNSET:
            field_dict["intervalCoverage"] = interval_coverage
        if murphy is not UNSET:
            field_dict["murphy"] = murphy
        if skill is not UNSET:
            field_dict["skill"] = skill
        if bias is not UNSET:
            field_dict["bias"] = bias
        if recalibration is not UNSET:
            field_dict["recalibration"] = recalibration
        if ensemble_weight is not UNSET:
            field_dict["ensembleWeight"] = ensemble_weight

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.scorecard_entry_bias_type_0 import ScorecardEntryBiasType0
        from ..models.scorecard_entry_interval_coverage import (
            ScorecardEntryIntervalCoverage,
        )
        from ..models.scorecard_entry_murphy_type_0 import ScorecardEntryMurphyType0
        from ..models.scorecard_entry_recalibration_type_0 import (
            ScorecardEntryRecalibrationType0,
        )
        from ..models.scorecard_entry_skill_type_0 import ScorecardEntrySkillType0

        d = dict(src_dict)
        forecaster_id = d.pop("forecasterId")

        n_resolved = d.pop("nResolved")

        label = ScorecardEntryLabel(d.pop("label"))

        forecaster_type = d.pop("forecasterType", UNSET)

        def _parse_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        category = _parse_category(d.pop("category", UNSET))

        n_forecasts = d.pop("nForecasts", UNSET)

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

        def _parse_time_averaged_brier(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        time_averaged_brier = _parse_time_averaged_brier(
            d.pop("timeAveragedBrier", UNSET)
        )

        _interval_coverage = d.pop("intervalCoverage", UNSET)
        interval_coverage: ScorecardEntryIntervalCoverage | Unset
        if isinstance(_interval_coverage, Unset):
            interval_coverage = UNSET
        else:
            interval_coverage = ScorecardEntryIntervalCoverage.from_dict(
                _interval_coverage
            )

        def _parse_murphy(data: object) -> None | ScorecardEntryMurphyType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                murphy_type_0 = ScorecardEntryMurphyType0.from_dict(data)

                return murphy_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ScorecardEntryMurphyType0 | Unset, data)

        murphy = _parse_murphy(d.pop("murphy", UNSET))

        def _parse_skill(data: object) -> None | ScorecardEntrySkillType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                skill_type_0 = ScorecardEntrySkillType0.from_dict(data)

                return skill_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ScorecardEntrySkillType0 | Unset, data)

        skill = _parse_skill(d.pop("skill", UNSET))

        def _parse_bias(data: object) -> None | ScorecardEntryBiasType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                bias_type_0 = ScorecardEntryBiasType0.from_dict(data)

                return bias_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ScorecardEntryBiasType0 | Unset, data)

        bias = _parse_bias(d.pop("bias", UNSET))

        def _parse_recalibration(
            data: object,
        ) -> None | ScorecardEntryRecalibrationType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                recalibration_type_0 = ScorecardEntryRecalibrationType0.from_dict(data)

                return recalibration_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ScorecardEntryRecalibrationType0 | Unset, data)

        recalibration = _parse_recalibration(d.pop("recalibration", UNSET))

        def _parse_ensemble_weight(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        ensemble_weight = _parse_ensemble_weight(d.pop("ensembleWeight", UNSET))

        scorecard_entry = cls(
            forecaster_id=forecaster_id,
            n_resolved=n_resolved,
            label=label,
            forecaster_type=forecaster_type,
            category=category,
            n_forecasts=n_forecasts,
            brier=brier,
            log_score=log_score,
            rps=rps,
            crps=crps,
            time_averaged_brier=time_averaged_brier,
            interval_coverage=interval_coverage,
            murphy=murphy,
            skill=skill,
            bias=bias,
            recalibration=recalibration,
            ensemble_weight=ensemble_weight,
        )

        scorecard_entry.additional_properties = d
        return scorecard_entry

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
