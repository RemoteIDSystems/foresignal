from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.regional_outlook_skill_type_0_label import RegionalOutlookSkillType0Label
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.regional_outlook_skill_type_0_backtest_type_0 import (
        RegionalOutlookSkillType0BacktestType0,
    )


T = TypeVar("T", bound="RegionalOutlookSkillType0")


@_attrs_define
class RegionalOutlookSkillType0:
    """Latest out-of-sample accuracy from the ledger

    Attributes:
        n (int):
        label (RegionalOutlookSkillType0Label):
        evaluated_weeks (int | Unset):  Example: 26.
        mean_pinball_loss (float | None | Unset):
        crps (float | None | Unset):
        skill_vs_seasonal_naive (float | None | Unset):
        skill_vs_climatology (float | None | Unset):
        interval_coverage_p10p90 (float | None | Unset):
        backtest (None | RegionalOutlookSkillType0BacktestType0 | Unset): The monthly rolling-origin evaluation (last 26
            weeks) against seasonal naive and climatology
    """

    n: int
    label: RegionalOutlookSkillType0Label
    evaluated_weeks: int | Unset = UNSET
    mean_pinball_loss: float | None | Unset = UNSET
    crps: float | None | Unset = UNSET
    skill_vs_seasonal_naive: float | None | Unset = UNSET
    skill_vs_climatology: float | None | Unset = UNSET
    interval_coverage_p10p90: float | None | Unset = UNSET
    backtest: None | RegionalOutlookSkillType0BacktestType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.regional_outlook_skill_type_0_backtest_type_0 import (
            RegionalOutlookSkillType0BacktestType0,
        )

        n = self.n

        label = self.label.value

        evaluated_weeks = self.evaluated_weeks

        mean_pinball_loss: float | None | Unset
        if isinstance(self.mean_pinball_loss, Unset):
            mean_pinball_loss = UNSET
        else:
            mean_pinball_loss = self.mean_pinball_loss

        crps: float | None | Unset
        if isinstance(self.crps, Unset):
            crps = UNSET
        else:
            crps = self.crps

        skill_vs_seasonal_naive: float | None | Unset
        if isinstance(self.skill_vs_seasonal_naive, Unset):
            skill_vs_seasonal_naive = UNSET
        else:
            skill_vs_seasonal_naive = self.skill_vs_seasonal_naive

        skill_vs_climatology: float | None | Unset
        if isinstance(self.skill_vs_climatology, Unset):
            skill_vs_climatology = UNSET
        else:
            skill_vs_climatology = self.skill_vs_climatology

        interval_coverage_p10p90: float | None | Unset
        if isinstance(self.interval_coverage_p10p90, Unset):
            interval_coverage_p10p90 = UNSET
        else:
            interval_coverage_p10p90 = self.interval_coverage_p10p90

        backtest: dict[str, Any] | None | Unset
        if isinstance(self.backtest, Unset):
            backtest = UNSET
        elif isinstance(self.backtest, RegionalOutlookSkillType0BacktestType0):
            backtest = self.backtest.to_dict()
        else:
            backtest = self.backtest

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "n": n,
                "label": label,
            }
        )
        if evaluated_weeks is not UNSET:
            field_dict["evaluatedWeeks"] = evaluated_weeks
        if mean_pinball_loss is not UNSET:
            field_dict["meanPinballLoss"] = mean_pinball_loss
        if crps is not UNSET:
            field_dict["crps"] = crps
        if skill_vs_seasonal_naive is not UNSET:
            field_dict["skillVsSeasonalNaive"] = skill_vs_seasonal_naive
        if skill_vs_climatology is not UNSET:
            field_dict["skillVsClimatology"] = skill_vs_climatology
        if interval_coverage_p10p90 is not UNSET:
            field_dict["intervalCoverageP10P90"] = interval_coverage_p10p90
        if backtest is not UNSET:
            field_dict["backtest"] = backtest

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.regional_outlook_skill_type_0_backtest_type_0 import (
            RegionalOutlookSkillType0BacktestType0,
        )

        d = dict(src_dict)
        n = d.pop("n")

        label = RegionalOutlookSkillType0Label(d.pop("label"))

        evaluated_weeks = d.pop("evaluatedWeeks", UNSET)

        def _parse_mean_pinball_loss(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_pinball_loss = _parse_mean_pinball_loss(d.pop("meanPinballLoss", UNSET))

        def _parse_crps(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        crps = _parse_crps(d.pop("crps", UNSET))

        def _parse_skill_vs_seasonal_naive(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        skill_vs_seasonal_naive = _parse_skill_vs_seasonal_naive(
            d.pop("skillVsSeasonalNaive", UNSET)
        )

        def _parse_skill_vs_climatology(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        skill_vs_climatology = _parse_skill_vs_climatology(
            d.pop("skillVsClimatology", UNSET)
        )

        def _parse_interval_coverage_p10p90(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        interval_coverage_p10p90 = _parse_interval_coverage_p10p90(
            d.pop("intervalCoverageP10P90", UNSET)
        )

        def _parse_backtest(
            data: object,
        ) -> None | RegionalOutlookSkillType0BacktestType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                backtest_type_0 = RegionalOutlookSkillType0BacktestType0.from_dict(data)

                return backtest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RegionalOutlookSkillType0BacktestType0 | Unset, data)

        backtest = _parse_backtest(d.pop("backtest", UNSET))

        regional_outlook_skill_type_0 = cls(
            n=n,
            label=label,
            evaluated_weeks=evaluated_weeks,
            mean_pinball_loss=mean_pinball_loss,
            crps=crps,
            skill_vs_seasonal_naive=skill_vs_seasonal_naive,
            skill_vs_climatology=skill_vs_climatology,
            interval_coverage_p10p90=interval_coverage_p10p90,
            backtest=backtest,
        )

        regional_outlook_skill_type_0.additional_properties = d
        return regional_outlook_skill_type_0

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
