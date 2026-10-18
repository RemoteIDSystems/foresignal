from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="HazardForecastProbabilityFieldItem")


@_attrs_define
class HazardForecastProbabilityFieldItem:
    """Published probability band

    Attributes:
        p_min (float):
        band (str | Unset):  Example: 40-50%.
        p_max (float | None | Unset):
        geometry (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
    """

    p_min: float
    band: str | Unset = UNSET
    p_max: float | None | Unset = UNSET
    geometry: Geometry | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p_min = self.p_min

        band = self.band

        p_max: float | None | Unset
        if isinstance(self.p_max, Unset):
            p_max = UNSET
        else:
            p_max = self.p_max

        geometry: dict[str, Any] | Unset = UNSET
        if not isinstance(self.geometry, Unset):
            geometry = self.geometry.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "pMin": p_min,
            }
        )
        if band is not UNSET:
            field_dict["band"] = band
        if p_max is not UNSET:
            field_dict["pMax"] = p_max
        if geometry is not UNSET:
            field_dict["geometry"] = geometry

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        p_min = d.pop("pMin")

        band = d.pop("band", UNSET)

        def _parse_p_max(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_max = _parse_p_max(d.pop("pMax", UNSET))

        _geometry = d.pop("geometry", UNSET)
        geometry: Geometry | Unset
        if isinstance(_geometry, Unset):
            geometry = UNSET
        else:
            geometry = Geometry.from_dict(_geometry)

        hazard_forecast_probability_field_item = cls(
            p_min=p_min,
            band=band,
            p_max=p_max,
            geometry=geometry,
        )

        hazard_forecast_probability_field_item.additional_properties = d
        return hazard_forecast_probability_field_item

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
