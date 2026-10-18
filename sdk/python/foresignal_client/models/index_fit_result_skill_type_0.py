from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IndexFitResultSkillType0")


@_attrs_define
class IndexFitResultSkillType0:
    """Latest out-of-sample skill of the index's implied count forecast

    Attributes:
        evaluated_at (datetime.datetime):
        n (int):
        pinball_loss (float | None | Unset):
        log_score (float | None | Unset):
        baseline_pinball_loss (float | None | Unset):
        baseline_log_score (float | None | Unset):
        skill_pinball (float | None | Unset):
        skill_log (float | None | Unset):
        baseline (str | Unset):  Default: 'seasonal-naive outlook'.
    """

    evaluated_at: datetime.datetime
    n: int
    pinball_loss: float | None | Unset = UNSET
    log_score: float | None | Unset = UNSET
    baseline_pinball_loss: float | None | Unset = UNSET
    baseline_log_score: float | None | Unset = UNSET
    skill_pinball: float | None | Unset = UNSET
    skill_log: float | None | Unset = UNSET
    baseline: str | Unset = "seasonal-naive outlook"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        evaluated_at = self.evaluated_at.isoformat()

        n = self.n

        pinball_loss: float | None | Unset
        if isinstance(self.pinball_loss, Unset):
            pinball_loss = UNSET
        else:
            pinball_loss = self.pinball_loss

        log_score: float | None | Unset
        if isinstance(self.log_score, Unset):
            log_score = UNSET
        else:
            log_score = self.log_score

        baseline_pinball_loss: float | None | Unset
        if isinstance(self.baseline_pinball_loss, Unset):
            baseline_pinball_loss = UNSET
        else:
            baseline_pinball_loss = self.baseline_pinball_loss

        baseline_log_score: float | None | Unset
        if isinstance(self.baseline_log_score, Unset):
            baseline_log_score = UNSET
        else:
            baseline_log_score = self.baseline_log_score

        skill_pinball: float | None | Unset
        if isinstance(self.skill_pinball, Unset):
            skill_pinball = UNSET
        else:
            skill_pinball = self.skill_pinball

        skill_log: float | None | Unset
        if isinstance(self.skill_log, Unset):
            skill_log = UNSET
        else:
            skill_log = self.skill_log

        baseline = self.baseline

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "evaluatedAt": evaluated_at,
                "n": n,
            }
        )
        if pinball_loss is not UNSET:
            field_dict["pinballLoss"] = pinball_loss
        if log_score is not UNSET:
            field_dict["logScore"] = log_score
        if baseline_pinball_loss is not UNSET:
            field_dict["baselinePinballLoss"] = baseline_pinball_loss
        if baseline_log_score is not UNSET:
            field_dict["baselineLogScore"] = baseline_log_score
        if skill_pinball is not UNSET:
            field_dict["skillPinball"] = skill_pinball
        if skill_log is not UNSET:
            field_dict["skillLog"] = skill_log
        if baseline is not UNSET:
            field_dict["baseline"] = baseline

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        evaluated_at = datetime.datetime.fromisoformat(d.pop("evaluatedAt"))

        n = d.pop("n")

        def _parse_pinball_loss(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        pinball_loss = _parse_pinball_loss(d.pop("pinballLoss", UNSET))

        def _parse_log_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        log_score = _parse_log_score(d.pop("logScore", UNSET))

        def _parse_baseline_pinball_loss(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        baseline_pinball_loss = _parse_baseline_pinball_loss(
            d.pop("baselinePinballLoss", UNSET)
        )

        def _parse_baseline_log_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        baseline_log_score = _parse_baseline_log_score(d.pop("baselineLogScore", UNSET))

        def _parse_skill_pinball(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        skill_pinball = _parse_skill_pinball(d.pop("skillPinball", UNSET))

        def _parse_skill_log(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        skill_log = _parse_skill_log(d.pop("skillLog", UNSET))

        baseline = d.pop("baseline", UNSET)

        index_fit_result_skill_type_0 = cls(
            evaluated_at=evaluated_at,
            n=n,
            pinball_loss=pinball_loss,
            log_score=log_score,
            baseline_pinball_loss=baseline_pinball_loss,
            baseline_log_score=baseline_log_score,
            skill_pinball=skill_pinball,
            skill_log=skill_log,
            baseline=baseline,
        )

        index_fit_result_skill_type_0.additional_properties = d
        return index_fit_result_skill_type_0

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
