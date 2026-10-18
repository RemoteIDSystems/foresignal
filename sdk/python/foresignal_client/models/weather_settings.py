from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_settings_alert_context import WeatherSettingsAlertContext
from ..types import UNSET, Unset

T = TypeVar("T", bound="WeatherSettings")


@_attrs_define
class WeatherSettings:
    """Instance weather settings (`weather.*` in app settings)

    Attributes:
        alert_context (WeatherSettingsAlertContext | Unset): Compute `context.weather` for geolocated alerts (056)
            Default: WeatherSettingsAlertContext.AUTO.
        default_aoi (str | Unset): minLon,minLat,maxLon,maxLat used for weather ingestion when no places exist (047); a
            health warning is raised while it applies Default: '-106.65,25.84,-93.51,36.50'.
        aoi_buffer_km (float | Unset): Minimum buffer around each place in the places AOI (036) Default: 150.0.
    """

    alert_context: WeatherSettingsAlertContext | Unset = (
        WeatherSettingsAlertContext.AUTO
    )
    default_aoi: str | Unset = "-106.65,25.84,-93.51,36.50"
    aoi_buffer_km: float | Unset = 150.0
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert_context: str | Unset = UNSET
        if not isinstance(self.alert_context, Unset):
            alert_context = self.alert_context.value

        default_aoi = self.default_aoi

        aoi_buffer_km = self.aoi_buffer_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if alert_context is not UNSET:
            field_dict["alertContext"] = alert_context
        if default_aoi is not UNSET:
            field_dict["defaultAoi"] = default_aoi
        if aoi_buffer_km is not UNSET:
            field_dict["aoiBufferKm"] = aoi_buffer_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _alert_context = d.pop("alertContext", UNSET)
        alert_context: WeatherSettingsAlertContext | Unset
        if isinstance(_alert_context, Unset):
            alert_context = UNSET
        else:
            alert_context = WeatherSettingsAlertContext(_alert_context)

        default_aoi = d.pop("defaultAoi", UNSET)

        aoi_buffer_km = d.pop("aoiBufferKm", UNSET)

        weather_settings = cls(
            alert_context=alert_context,
            default_aoi=default_aoi,
            aoi_buffer_km=aoi_buffer_km,
        )

        weather_settings.additional_properties = d
        return weather_settings

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
