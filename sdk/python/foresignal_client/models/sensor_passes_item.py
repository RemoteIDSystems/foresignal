from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SensorPassesItem")


@_attrs_define
class SensorPassesItem:
    """Predicted pass (064 Orekit)

    Attributes:
        aos (datetime.datetime):
        los (datetime.datetime):
        max_elevation_deg (float):
        tca (datetime.datetime | Unset):
    """

    aos: datetime.datetime
    los: datetime.datetime
    max_elevation_deg: float
    tca: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        aos = self.aos.isoformat()

        los = self.los.isoformat()

        max_elevation_deg = self.max_elevation_deg

        tca: str | Unset = UNSET
        if not isinstance(self.tca, Unset):
            tca = self.tca.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "aos": aos,
                "los": los,
                "maxElevationDeg": max_elevation_deg,
            }
        )
        if tca is not UNSET:
            field_dict["tca"] = tca

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        aos = datetime.datetime.fromisoformat(d.pop("aos"))

        los = datetime.datetime.fromisoformat(d.pop("los"))

        max_elevation_deg = d.pop("maxElevationDeg")

        _tca = d.pop("tca", UNSET)
        tca: datetime.datetime | Unset
        if isinstance(_tca, Unset):
            tca = UNSET
        else:
            tca = datetime.datetime.fromisoformat(_tca)

        sensor_passes_item = cls(
            aos=aos,
            los=los,
            max_elevation_deg=max_elevation_deg,
            tca=tca,
        )

        sensor_passes_item.additional_properties = d
        return sensor_passes_item

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
