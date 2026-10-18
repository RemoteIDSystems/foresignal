from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.nws_forecast_payload_hazards_item import NwsForecastPayloadHazardsItem


T = TypeVar("T", bound="NwsForecastPayload")


@_attrs_define
class NwsForecastPayload:
    """Payload of `nws-forecast` records (nws-gridpoints, externalId `place:{placeId}`; no geometry, since a place's
    position is sensitive)

        Attributes:
            place (str):
            wfo (str):
            grid_x (int):
            grid_y (int):
            forecast_updated (datetime.datetime):
            pop_max_24_h (int | Unset):
            qpf_24_h_mm (float | Unset):
            t_max_c24_h (float | Unset):
            t_min_c24_h (float | Unset):
            gust_max_ms_24_h (float | Unset):
            hazards (list[NwsForecastPayloadHazardsItem] | Unset):
    """

    place: str
    wfo: str
    grid_x: int
    grid_y: int
    forecast_updated: datetime.datetime
    pop_max_24_h: int | Unset = UNSET
    qpf_24_h_mm: float | Unset = UNSET
    t_max_c24_h: float | Unset = UNSET
    t_min_c24_h: float | Unset = UNSET
    gust_max_ms_24_h: float | Unset = UNSET
    hazards: list[NwsForecastPayloadHazardsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        place = self.place

        wfo = self.wfo

        grid_x = self.grid_x

        grid_y = self.grid_y

        forecast_updated = self.forecast_updated.isoformat()

        pop_max_24_h = self.pop_max_24_h

        qpf_24_h_mm = self.qpf_24_h_mm

        t_max_c24_h = self.t_max_c24_h

        t_min_c24_h = self.t_min_c24_h

        gust_max_ms_24_h = self.gust_max_ms_24_h

        hazards: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.hazards, Unset):
            hazards = []
            for hazards_item_data in self.hazards:
                hazards_item = hazards_item_data.to_dict()
                hazards.append(hazards_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "place": place,
                "wfo": wfo,
                "gridX": grid_x,
                "gridY": grid_y,
                "forecastUpdated": forecast_updated,
            }
        )
        if pop_max_24_h is not UNSET:
            field_dict["popMax24h"] = pop_max_24_h
        if qpf_24_h_mm is not UNSET:
            field_dict["qpf24hMm"] = qpf_24_h_mm
        if t_max_c24_h is not UNSET:
            field_dict["tMaxC24h"] = t_max_c24_h
        if t_min_c24_h is not UNSET:
            field_dict["tMinC24h"] = t_min_c24_h
        if gust_max_ms_24_h is not UNSET:
            field_dict["gustMaxMs24h"] = gust_max_ms_24_h
        if hazards is not UNSET:
            field_dict["hazards"] = hazards

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.nws_forecast_payload_hazards_item import (
            NwsForecastPayloadHazardsItem,
        )

        d = dict(src_dict)
        place = d.pop("place")

        wfo = d.pop("wfo")

        grid_x = d.pop("gridX")

        grid_y = d.pop("gridY")

        forecast_updated = datetime.datetime.fromisoformat(d.pop("forecastUpdated"))

        pop_max_24_h = d.pop("popMax24h", UNSET)

        qpf_24_h_mm = d.pop("qpf24hMm", UNSET)

        t_max_c24_h = d.pop("tMaxC24h", UNSET)

        t_min_c24_h = d.pop("tMinC24h", UNSET)

        gust_max_ms_24_h = d.pop("gustMaxMs24h", UNSET)

        _hazards = d.pop("hazards", UNSET)
        hazards: list[NwsForecastPayloadHazardsItem] | Unset = UNSET
        if _hazards is not UNSET:
            hazards = []
            for hazards_item_data in _hazards:
                hazards_item = NwsForecastPayloadHazardsItem.from_dict(
                    hazards_item_data
                )

                hazards.append(hazards_item)

        nws_forecast_payload = cls(
            place=place,
            wfo=wfo,
            grid_x=grid_x,
            grid_y=grid_y,
            forecast_updated=forecast_updated,
            pop_max_24_h=pop_max_24_h,
            qpf_24_h_mm=qpf_24_h_mm,
            t_max_c24_h=t_max_c24_h,
            t_min_c24_h=t_min_c24_h,
            gust_max_ms_24_h=gust_max_ms_24_h,
            hazards=hazards,
        )

        nws_forecast_payload.additional_properties = d
        return nws_forecast_payload

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
