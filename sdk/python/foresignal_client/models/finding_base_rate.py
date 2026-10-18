from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.finding_base_rate_method import FindingBaseRateMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="FindingBaseRate")


@_attrs_define
class FindingBaseRate:
    """How often a finding this strong happens by chance, and how that was measured

    Attributes:
        value (float | None):
        unit (str): e.g. `alarms-per-year`, `placebo-share`, `expected-joint-days-per-year`
        method (FindingBaseRateMethod):
        text (str): Plain-language statement, e.g. `a change this large was flagged once in the previous 700 days`
        n (int | None | Unset): Sample size behind the rate
        period (None | TimeRange | Unset):
    """

    value: float | None
    unit: str
    method: FindingBaseRateMethod
    text: str
    n: int | None | Unset = UNSET
    period: None | TimeRange | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.time_range import TimeRange

        value: float | None
        value = self.value

        unit = self.unit

        method = self.method.value

        text = self.text

        n: int | None | Unset
        if isinstance(self.n, Unset):
            n = UNSET
        else:
            n = self.n

        period: dict[str, Any] | None | Unset
        if isinstance(self.period, Unset):
            period = UNSET
        elif isinstance(self.period, TimeRange):
            period = self.period.to_dict()
        else:
            period = self.period

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "value": value,
                "unit": unit,
                "method": method,
                "text": text,
            }
        )
        if n is not UNSET:
            field_dict["n"] = n
        if period is not UNSET:
            field_dict["period"] = period

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.time_range import TimeRange

        d = dict(src_dict)

        def _parse_value(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        value = _parse_value(d.pop("value"))

        unit = d.pop("unit")

        method = FindingBaseRateMethod(d.pop("method"))

        text = d.pop("text")

        def _parse_n(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        n = _parse_n(d.pop("n", UNSET))

        def _parse_period(data: object) -> None | TimeRange | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                period_type_0 = TimeRange.from_dict(data)

                return period_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TimeRange | Unset, data)

        period = _parse_period(d.pop("period", UNSET))

        finding_base_rate = cls(
            value=value,
            unit=unit,
            method=method,
            text=text,
            n=n,
            period=period,
        )

        finding_base_rate.additional_properties = d
        return finding_base_rate

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
