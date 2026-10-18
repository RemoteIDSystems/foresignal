from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.smoke_polygon_payload_density import SmokePolygonPayloadDensity
from ..types import UNSET, Unset

T = TypeVar("T", bound="SmokePolygonPayload")


@_attrs_define
class SmokePolygonPayload:
    """Payload of `smoke-polygon` records (hms-smoke analyst polygons)

    Attributes:
        density (SmokePolygonPayloadDensity):
        density_rank (int):
        label (str):
        start (datetime.datetime | Unset):
        end (datetime.datetime | Unset):
        satellite (str | Unset):
    """

    density: SmokePolygonPayloadDensity
    density_rank: int
    label: str
    start: datetime.datetime | Unset = UNSET
    end: datetime.datetime | Unset = UNSET
    satellite: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        density = self.density.value

        density_rank = self.density_rank

        label = self.label

        start: str | Unset = UNSET
        if not isinstance(self.start, Unset):
            start = self.start.isoformat()

        end: str | Unset = UNSET
        if not isinstance(self.end, Unset):
            end = self.end.isoformat()

        satellite = self.satellite

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "density": density,
                "densityRank": density_rank,
                "label": label,
            }
        )
        if start is not UNSET:
            field_dict["start"] = start
        if end is not UNSET:
            field_dict["end"] = end
        if satellite is not UNSET:
            field_dict["satellite"] = satellite

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        density = SmokePolygonPayloadDensity(d.pop("density"))

        density_rank = d.pop("densityRank")

        label = d.pop("label")

        _start = d.pop("start", UNSET)
        start: datetime.datetime | Unset
        if isinstance(_start, Unset):
            start = UNSET
        else:
            start = datetime.datetime.fromisoformat(_start)

        _end = d.pop("end", UNSET)
        end: datetime.datetime | Unset
        if isinstance(_end, Unset):
            end = UNSET
        else:
            end = datetime.datetime.fromisoformat(_end)

        satellite = d.pop("satellite", UNSET)

        smoke_polygon_payload = cls(
            density=density,
            density_rank=density_rank,
            label=label,
            start=start,
            end=end,
            satellite=satellite,
        )

        smoke_polygon_payload.additional_properties = d
        return smoke_polygon_payload

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
