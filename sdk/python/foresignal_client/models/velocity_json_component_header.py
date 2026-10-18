from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.velocity_json_component_header_parameter_number import (
    VelocityJsonComponentHeaderParameterNumber,
)

T = TypeVar("T", bound="VelocityJsonComponentHeader")


@_attrs_define
class VelocityJsonComponentHeader:
    """GRIB-like header

    Attributes:
        parameter_category (Literal[2]):
        parameter_number (VelocityJsonComponentHeaderParameterNumber): 2 = U, 3 = V
        lo1 (float):
        la1 (float):
        lo2 (float):
        la2 (float):
        dx (float):
        dy (float):
        nx (int):
        ny (int):
        ref_time (datetime.datetime):
        forecast_time (int): Hours after refTime
    """

    parameter_category: Literal[2]
    parameter_number: VelocityJsonComponentHeaderParameterNumber
    lo1: float
    la1: float
    lo2: float
    la2: float
    dx: float
    dy: float
    nx: int
    ny: int
    ref_time: datetime.datetime
    forecast_time: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        parameter_category = self.parameter_category

        parameter_number = self.parameter_number.value

        lo1 = self.lo1

        la1 = self.la1

        lo2 = self.lo2

        la2 = self.la2

        dx = self.dx

        dy = self.dy

        nx = self.nx

        ny = self.ny

        ref_time = self.ref_time.isoformat()

        forecast_time = self.forecast_time

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "parameterCategory": parameter_category,
                "parameterNumber": parameter_number,
                "lo1": lo1,
                "la1": la1,
                "lo2": lo2,
                "la2": la2,
                "dx": dx,
                "dy": dy,
                "nx": nx,
                "ny": ny,
                "refTime": ref_time,
                "forecastTime": forecast_time,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        parameter_category = cast(Literal[2], d.pop("parameterCategory"))
        if parameter_category != 2:
            raise ValueError(
                f"parameterCategory must match const 2, got '{parameter_category}'"
            )

        parameter_number = VelocityJsonComponentHeaderParameterNumber(
            d.pop("parameterNumber")
        )

        lo1 = d.pop("lo1")

        la1 = d.pop("la1")

        lo2 = d.pop("lo2")

        la2 = d.pop("la2")

        dx = d.pop("dx")

        dy = d.pop("dy")

        nx = d.pop("nx")

        ny = d.pop("ny")

        ref_time = datetime.datetime.fromisoformat(d.pop("refTime"))

        forecast_time = d.pop("forecastTime")

        velocity_json_component_header = cls(
            parameter_category=parameter_category,
            parameter_number=parameter_number,
            lo1=lo1,
            la1=la1,
            lo2=lo2,
            la2=la2,
            dx=dx,
            dy=dy,
            nx=nx,
            ny=ny,
            ref_time=ref_time,
            forecast_time=forecast_time,
        )

        velocity_json_component_header.additional_properties = d
        return velocity_json_component_header

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
