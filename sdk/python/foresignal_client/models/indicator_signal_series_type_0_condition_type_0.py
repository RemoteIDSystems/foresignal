from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_signal_series_type_0_condition_type_0_op import (
    IndicatorSignalSeriesType0ConditionType0Op,
)

T = TypeVar("T", bound="IndicatorSignalSeriesType0ConditionType0")


@_attrs_define
class IndicatorSignalSeriesType0ConditionType0:
    """Threshold on the series value

    Attributes:
        op (IndicatorSignalSeriesType0ConditionType0Op):
        value (float):
    """

    op: IndicatorSignalSeriesType0ConditionType0Op
    value: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        op = self.op.value

        value = self.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "op": op,
                "value": value,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        op = IndicatorSignalSeriesType0ConditionType0Op(d.pop("op"))

        value = d.pop("value")

        indicator_signal_series_type_0_condition_type_0 = cls(
            op=op,
            value=value,
        )

        indicator_signal_series_type_0_condition_type_0.additional_properties = d
        return indicator_signal_series_type_0_condition_type_0

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
