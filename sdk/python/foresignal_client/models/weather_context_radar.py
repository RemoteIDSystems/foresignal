from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.weather_context_radar_nearest_cell_type_0 import (
        WeatherContextRadarNearestCellType0,
    )


T = TypeVar("T", bound="WeatherContextRadar")


@_attrs_define
class WeatherContextRadar:
    """Radar section

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        max_dbz (float | Unset): Max within 10 km of the geometry
        nearest_cell (None | Unset | WeatherContextRadarNearestCellType0):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    max_dbz: float | Unset = UNSET
    nearest_cell: None | Unset | WeatherContextRadarNearestCellType0 = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.weather_context_radar_nearest_cell_type_0 import (
            WeatherContextRadarNearestCellType0,
        )

        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        max_dbz = self.max_dbz

        nearest_cell: dict[str, Any] | None | Unset
        if isinstance(self.nearest_cell, Unset):
            nearest_cell = UNSET
        elif isinstance(self.nearest_cell, WeatherContextRadarNearestCellType0):
            nearest_cell = self.nearest_cell.to_dict()
        else:
            nearest_cell = self.nearest_cell

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if status_text is not UNSET:
            field_dict["statusText"] = status_text
        if max_dbz is not UNSET:
            field_dict["maxDbz"] = max_dbz
        if nearest_cell is not UNSET:
            field_dict["nearestCell"] = nearest_cell

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.weather_context_radar_nearest_cell_type_0 import (
            WeatherContextRadarNearestCellType0,
        )

        d = dict(src_dict)
        status = WeatherSectionStatus(d.pop("status"))

        _as_of = d.pop("asOf", UNSET)
        as_of: datetime.datetime | Unset
        if isinstance(_as_of, Unset):
            as_of = UNSET
        else:
            as_of = datetime.datetime.fromisoformat(_as_of)

        status_text = d.pop("statusText", UNSET)

        max_dbz = d.pop("maxDbz", UNSET)

        def _parse_nearest_cell(
            data: object,
        ) -> None | Unset | WeatherContextRadarNearestCellType0:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                nearest_cell_type_0 = WeatherContextRadarNearestCellType0.from_dict(
                    data
                )

                return nearest_cell_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | WeatherContextRadarNearestCellType0, data)

        nearest_cell = _parse_nearest_cell(d.pop("nearestCell", UNSET))

        weather_context_radar = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            max_dbz=max_dbz,
            nearest_cell=nearest_cell,
        )

        weather_context_radar.additional_properties = d
        return weather_context_radar

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
