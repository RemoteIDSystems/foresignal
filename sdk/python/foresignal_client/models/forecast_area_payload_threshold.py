from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_area_payload_threshold_op import ForecastAreaPayloadThresholdOp
from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastAreaPayloadThreshold")


@_attrs_define
class ForecastAreaPayloadThreshold:
    """Threshold

    Attributes:
        op (ForecastAreaPayloadThresholdOp | Unset):
        value (float | Unset):
        units (str | Unset):
    """

    op: ForecastAreaPayloadThresholdOp | Unset = UNSET
    value: float | Unset = UNSET
    units: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        op: str | Unset = UNSET
        if not isinstance(self.op, Unset):
            op = self.op.value

        value = self.value

        units = self.units

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if op is not UNSET:
            field_dict["op"] = op
        if value is not UNSET:
            field_dict["value"] = value
        if units is not UNSET:
            field_dict["units"] = units

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _op = d.pop("op", UNSET)
        op: ForecastAreaPayloadThresholdOp | Unset
        if isinstance(_op, Unset):
            op = UNSET
        else:
            op = ForecastAreaPayloadThresholdOp(_op)

        value = d.pop("value", UNSET)

        units = d.pop("units", UNSET)

        forecast_area_payload_threshold = cls(
            op=op,
            value=value,
            units=units,
        )

        forecast_area_payload_threshold.additional_properties = d
        return forecast_area_payload_threshold

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
