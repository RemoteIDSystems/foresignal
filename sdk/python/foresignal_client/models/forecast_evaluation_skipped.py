from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastEvaluationSkipped")


@_attrs_define
class ForecastEvaluationSkipped:
    """Skipped questions by reason

    Attributes:
        leakage_risk (int): Opened before the model's knowledgeCutoff (`LEAKAGE_RISK`)
        other (int | Unset):
    """

    leakage_risk: int
    other: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        leakage_risk = self.leakage_risk

        other = self.other

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "leakageRisk": leakage_risk,
            }
        )
        if other is not UNSET:
            field_dict["other"] = other

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        leakage_risk = d.pop("leakageRisk")

        other = d.pop("other", UNSET)

        forecast_evaluation_skipped = cls(
            leakage_risk=leakage_risk,
            other=other,
        )

        forecast_evaluation_skipped.additional_properties = d
        return forecast_evaluation_skipped

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
