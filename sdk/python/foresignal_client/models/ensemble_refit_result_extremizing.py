from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EnsembleRefitResultExtremizing")


@_attrs_define
class EnsembleRefitResultExtremizing:
    """Extremizing factor a and whether it is enabled

    Attributes:
        a (float):
        enabled (bool):
        n_resolutions (int):
        distinct_forecaster_types (int):
        log_score_improvement (float | None | Unset): Out-of-sample log-score improvement; enabled only when positive
            over ≥ 100 resolutions
    """

    a: float
    enabled: bool
    n_resolutions: int
    distinct_forecaster_types: int
    log_score_improvement: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        a = self.a

        enabled = self.enabled

        n_resolutions = self.n_resolutions

        distinct_forecaster_types = self.distinct_forecaster_types

        log_score_improvement: float | None | Unset
        if isinstance(self.log_score_improvement, Unset):
            log_score_improvement = UNSET
        else:
            log_score_improvement = self.log_score_improvement

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "a": a,
                "enabled": enabled,
                "nResolutions": n_resolutions,
                "distinctForecasterTypes": distinct_forecaster_types,
            }
        )
        if log_score_improvement is not UNSET:
            field_dict["logScoreImprovement"] = log_score_improvement

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        a = d.pop("a")

        enabled = d.pop("enabled")

        n_resolutions = d.pop("nResolutions")

        distinct_forecaster_types = d.pop("distinctForecasterTypes")

        def _parse_log_score_improvement(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        log_score_improvement = _parse_log_score_improvement(
            d.pop("logScoreImprovement", UNSET)
        )

        ensemble_refit_result_extremizing = cls(
            a=a,
            enabled=enabled,
            n_resolutions=n_resolutions,
            distinct_forecaster_types=distinct_forecaster_types,
            log_score_improvement=log_score_improvement,
        )

        ensemble_refit_result_extremizing.additional_properties = d
        return ensemble_refit_result_extremizing

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
