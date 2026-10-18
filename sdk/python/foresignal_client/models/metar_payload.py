from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.metar_payload_flight_category import MetarPayloadFlightCategory
from ..models.metar_payload_metar_type import MetarPayloadMetarType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.metar_payload_sky_item import MetarPayloadSkyItem


T = TypeVar("T", bound="MetarPayload")


@_attrs_define
class MetarPayload:
    """Payload of `metar` records (awc-metars, externalId = station; empty fields omitted)

    Attributes:
        raw (str):
        station_id (str):
        metar_type (MetarPayloadMetarType | Unset):
        temp_c (float | Unset):
        dewpoint_c (float | Unset):
        wind_dir_deg (int | Unset):
        wind_variable (bool | Unset):
        wind_kt (int | Unset):
        gust_kt (int | Unset):
        visibility_sm (float | Unset):
        visibility_plus (bool | Unset):
        altim_in_hg (float | Unset):
        slp_mb (float | Unset):
        wx (str | Unset):
        sky (list[MetarPayloadSkyItem] | Unset):
        flight_category (MetarPayloadFlightCategory | Unset):
        elevation_m (float | Unset):
    """

    raw: str
    station_id: str
    metar_type: MetarPayloadMetarType | Unset = UNSET
    temp_c: float | Unset = UNSET
    dewpoint_c: float | Unset = UNSET
    wind_dir_deg: int | Unset = UNSET
    wind_variable: bool | Unset = UNSET
    wind_kt: int | Unset = UNSET
    gust_kt: int | Unset = UNSET
    visibility_sm: float | Unset = UNSET
    visibility_plus: bool | Unset = UNSET
    altim_in_hg: float | Unset = UNSET
    slp_mb: float | Unset = UNSET
    wx: str | Unset = UNSET
    sky: list[MetarPayloadSkyItem] | Unset = UNSET
    flight_category: MetarPayloadFlightCategory | Unset = UNSET
    elevation_m: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        raw = self.raw

        station_id = self.station_id

        metar_type: str | Unset = UNSET
        if not isinstance(self.metar_type, Unset):
            metar_type = self.metar_type.value

        temp_c = self.temp_c

        dewpoint_c = self.dewpoint_c

        wind_dir_deg = self.wind_dir_deg

        wind_variable = self.wind_variable

        wind_kt = self.wind_kt

        gust_kt = self.gust_kt

        visibility_sm = self.visibility_sm

        visibility_plus = self.visibility_plus

        altim_in_hg = self.altim_in_hg

        slp_mb = self.slp_mb

        wx = self.wx

        sky: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.sky, Unset):
            sky = []
            for sky_item_data in self.sky:
                sky_item = sky_item_data.to_dict()
                sky.append(sky_item)

        flight_category: str | Unset = UNSET
        if not isinstance(self.flight_category, Unset):
            flight_category = self.flight_category.value

        elevation_m = self.elevation_m

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "raw": raw,
                "stationId": station_id,
            }
        )
        if metar_type is not UNSET:
            field_dict["metarType"] = metar_type
        if temp_c is not UNSET:
            field_dict["tempC"] = temp_c
        if dewpoint_c is not UNSET:
            field_dict["dewpointC"] = dewpoint_c
        if wind_dir_deg is not UNSET:
            field_dict["windDirDeg"] = wind_dir_deg
        if wind_variable is not UNSET:
            field_dict["windVariable"] = wind_variable
        if wind_kt is not UNSET:
            field_dict["windKt"] = wind_kt
        if gust_kt is not UNSET:
            field_dict["gustKt"] = gust_kt
        if visibility_sm is not UNSET:
            field_dict["visibilitySm"] = visibility_sm
        if visibility_plus is not UNSET:
            field_dict["visibilityPlus"] = visibility_plus
        if altim_in_hg is not UNSET:
            field_dict["altimInHg"] = altim_in_hg
        if slp_mb is not UNSET:
            field_dict["slpMb"] = slp_mb
        if wx is not UNSET:
            field_dict["wx"] = wx
        if sky is not UNSET:
            field_dict["sky"] = sky
        if flight_category is not UNSET:
            field_dict["flightCategory"] = flight_category
        if elevation_m is not UNSET:
            field_dict["elevationM"] = elevation_m

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.metar_payload_sky_item import MetarPayloadSkyItem

        d = dict(src_dict)
        raw = d.pop("raw")

        station_id = d.pop("stationId")

        _metar_type = d.pop("metarType", UNSET)
        metar_type: MetarPayloadMetarType | Unset
        if isinstance(_metar_type, Unset):
            metar_type = UNSET
        else:
            metar_type = MetarPayloadMetarType(_metar_type)

        temp_c = d.pop("tempC", UNSET)

        dewpoint_c = d.pop("dewpointC", UNSET)

        wind_dir_deg = d.pop("windDirDeg", UNSET)

        wind_variable = d.pop("windVariable", UNSET)

        wind_kt = d.pop("windKt", UNSET)

        gust_kt = d.pop("gustKt", UNSET)

        visibility_sm = d.pop("visibilitySm", UNSET)

        visibility_plus = d.pop("visibilityPlus", UNSET)

        altim_in_hg = d.pop("altimInHg", UNSET)

        slp_mb = d.pop("slpMb", UNSET)

        wx = d.pop("wx", UNSET)

        _sky = d.pop("sky", UNSET)
        sky: list[MetarPayloadSkyItem] | Unset = UNSET
        if _sky is not UNSET:
            sky = []
            for sky_item_data in _sky:
                sky_item = MetarPayloadSkyItem.from_dict(sky_item_data)

                sky.append(sky_item)

        _flight_category = d.pop("flightCategory", UNSET)
        flight_category: MetarPayloadFlightCategory | Unset
        if isinstance(_flight_category, Unset):
            flight_category = UNSET
        else:
            flight_category = MetarPayloadFlightCategory(_flight_category)

        elevation_m = d.pop("elevationM", UNSET)

        metar_payload = cls(
            raw=raw,
            station_id=station_id,
            metar_type=metar_type,
            temp_c=temp_c,
            dewpoint_c=dewpoint_c,
            wind_dir_deg=wind_dir_deg,
            wind_variable=wind_variable,
            wind_kt=wind_kt,
            gust_kt=gust_kt,
            visibility_sm=visibility_sm,
            visibility_plus=visibility_plus,
            altim_in_hg=altim_in_hg,
            slp_mb=slp_mb,
            wx=wx,
            sky=sky,
            flight_category=flight_category,
            elevation_m=elevation_m,
        )

        metar_payload.additional_properties = d
        return metar_payload

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
