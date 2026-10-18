from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.anomaly_observation_baseline_model import AnomalyObservationBaselineModel
from ..models.anomaly_observation_baseline_slot_mode import (
    AnomalyObservationBaselineSlotMode,
)

T = TypeVar("T", bound="AnomalyObservationBaseline")


@_attrs_define
class AnomalyObservationBaseline:
    """Baseline summary used

    Attributes:
        weeks (float):
        slot_mode (AnomalyObservationBaselineSlotMode):
        mean (float):
        variance (float):
        model (AnomalyObservationBaselineModel):
    """

    weeks: float
    slot_mode: AnomalyObservationBaselineSlotMode
    mean: float
    variance: float
    model: AnomalyObservationBaselineModel
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        weeks = self.weeks

        slot_mode = self.slot_mode.value

        mean = self.mean

        variance = self.variance

        model = self.model.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "weeks": weeks,
                "slotMode": slot_mode,
                "mean": mean,
                "variance": variance,
                "model": model,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        weeks = d.pop("weeks")

        slot_mode = AnomalyObservationBaselineSlotMode(d.pop("slotMode"))

        mean = d.pop("mean")

        variance = d.pop("variance")

        model = AnomalyObservationBaselineModel(d.pop("model"))

        anomaly_observation_baseline = cls(
            weeks=weeks,
            slot_mode=slot_mode,
            mean=mean,
            variance=variance,
            model=model,
        )

        anomaly_observation_baseline.additional_properties = d
        return anomaly_observation_baseline

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
