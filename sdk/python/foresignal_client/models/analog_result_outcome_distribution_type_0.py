from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stat_interval import StatInterval


T = TypeVar("T", bound="AnalogResultOutcomeDistributionType0")


@_attrs_define
class AnalogResultOutcomeDistributionType0:
    """Outcome frequency among analogs, shrunk toward the base rate by k ÷ (k + 10)

    Attributes:
        outcome (str): Target outcome: `critical` (situation reaches critical) or `{family}/P{n}D` (a family within n
            days), e.g. `conflict:battles/P30D`
        analog_count (int):
        hits (int):
        probability (float):
        interval (StatInterval): A two-sided statistical interval for a rate, proportion or difference
        frequency (float | None | Unset):
        base_rate (float | None | Unset):
        base_rate_id (None | str | Unset):
    """

    outcome: str
    analog_count: int
    hits: int
    probability: float
    interval: StatInterval
    frequency: float | None | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    base_rate_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        outcome = self.outcome

        analog_count = self.analog_count

        hits = self.hits

        probability = self.probability

        interval = self.interval.to_dict()

        frequency: float | None | Unset
        if isinstance(self.frequency, Unset):
            frequency = UNSET
        else:
            frequency = self.frequency

        base_rate: float | None | Unset
        if isinstance(self.base_rate, Unset):
            base_rate = UNSET
        else:
            base_rate = self.base_rate

        base_rate_id: None | str | Unset
        if isinstance(self.base_rate_id, Unset):
            base_rate_id = UNSET
        else:
            base_rate_id = self.base_rate_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "outcome": outcome,
                "analogCount": analog_count,
                "hits": hits,
                "probability": probability,
                "interval": interval,
            }
        )
        if frequency is not UNSET:
            field_dict["frequency"] = frequency
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate
        if base_rate_id is not UNSET:
            field_dict["baseRateId"] = base_rate_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stat_interval import StatInterval

        d = dict(src_dict)
        outcome = d.pop("outcome")

        analog_count = d.pop("analogCount")

        hits = d.pop("hits")

        probability = d.pop("probability")

        interval = StatInterval.from_dict(d.pop("interval"))

        def _parse_frequency(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        frequency = _parse_frequency(d.pop("frequency", UNSET))

        def _parse_base_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_rate = _parse_base_rate(d.pop("baseRate", UNSET))

        def _parse_base_rate_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        base_rate_id = _parse_base_rate_id(d.pop("baseRateId", UNSET))

        analog_result_outcome_distribution_type_0 = cls(
            outcome=outcome,
            analog_count=analog_count,
            hits=hits,
            probability=probability,
            interval=interval,
            frequency=frequency,
            base_rate=base_rate,
            base_rate_id=base_rate_id,
        )

        analog_result_outcome_distribution_type_0.additional_properties = d
        return analog_result_outcome_distribution_type_0

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
