from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.anomaly_baseline_slot_model import AnomalyBaselineSlotModel

T = TypeVar("T", bound="AnomalyBaselineSlot")


@_attrs_define
class AnomalyBaselineSlot:
    """Baseline statistics for one slot (hour-of-week or hour-of-day) of one group

    Attributes:
        group_key (str):
        slot (int): 0–167 (hour-of-week) or 0–23 (hour-of-day)
        n (int): Coverage-ok, unflagged samples
        mean (float):
        variance (float):
        model (AnomalyBaselineSlotModel):
    """

    group_key: str
    slot: int
    n: int
    mean: float
    variance: float
    model: AnomalyBaselineSlotModel
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        group_key = self.group_key

        slot = self.slot

        n = self.n

        mean = self.mean

        variance = self.variance

        model = self.model.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "groupKey": group_key,
                "slot": slot,
                "n": n,
                "mean": mean,
                "variance": variance,
                "model": model,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        group_key = d.pop("groupKey")

        slot = d.pop("slot")

        n = d.pop("n")

        mean = d.pop("mean")

        variance = d.pop("variance")

        model = AnomalyBaselineSlotModel(d.pop("model"))

        anomaly_baseline_slot = cls(
            group_key=group_key,
            slot=slot,
            n=n,
            mean=mean,
            variance=variance,
            model=model,
        )

        anomaly_baseline_slot.additional_properties = d
        return anomaly_baseline_slot

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
