from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.place_weather_digest_now_alerts_item import (
        PlaceWeatherDigestNowAlertsItem,
    )
    from ..models.place_weather_digest_now_alerts_status import (
        PlaceWeatherDigestNowAlertsStatus,
    )
    from ..models.place_weather_digest_now_lightning import (
        PlaceWeatherDigestNowLightning,
    )
    from ..models.place_weather_digest_now_radar import PlaceWeatherDigestNowRadar


T = TypeVar("T", bound="PlaceWeatherDigestNow")


@_attrs_define
class PlaceWeatherDigestNow:
    """Current conditions

    Attributes:
        radar (PlaceWeatherDigestNowRadar | Unset): Radar now
        lightning (PlaceWeatherDigestNowLightning | Unset): Lightning now
        alerts (list[PlaceWeatherDigestNowAlertsItem] | Unset):
        alerts_status (PlaceWeatherDigestNowAlertsStatus | Unset): Status of the NWS alerts list (056): not ok when the
            nws-alerts source is disabled, failing or not visible, so an empty list is unknown, not quiet
    """

    radar: PlaceWeatherDigestNowRadar | Unset = UNSET
    lightning: PlaceWeatherDigestNowLightning | Unset = UNSET
    alerts: list[PlaceWeatherDigestNowAlertsItem] | Unset = UNSET
    alerts_status: PlaceWeatherDigestNowAlertsStatus | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        radar: dict[str, Any] | Unset = UNSET
        if not isinstance(self.radar, Unset):
            radar = self.radar.to_dict()

        lightning: dict[str, Any] | Unset = UNSET
        if not isinstance(self.lightning, Unset):
            lightning = self.lightning.to_dict()

        alerts: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.alerts, Unset):
            alerts = []
            for alerts_item_data in self.alerts:
                alerts_item = alerts_item_data.to_dict()
                alerts.append(alerts_item)

        alerts_status: dict[str, Any] | Unset = UNSET
        if not isinstance(self.alerts_status, Unset):
            alerts_status = self.alerts_status.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if radar is not UNSET:
            field_dict["radar"] = radar
        if lightning is not UNSET:
            field_dict["lightning"] = lightning
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if alerts_status is not UNSET:
            field_dict["alertsStatus"] = alerts_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.place_weather_digest_now_alerts_item import (
            PlaceWeatherDigestNowAlertsItem,
        )
        from ..models.place_weather_digest_now_alerts_status import (
            PlaceWeatherDigestNowAlertsStatus,
        )
        from ..models.place_weather_digest_now_lightning import (
            PlaceWeatherDigestNowLightning,
        )
        from ..models.place_weather_digest_now_radar import PlaceWeatherDigestNowRadar

        d = dict(src_dict)
        _radar = d.pop("radar", UNSET)
        radar: PlaceWeatherDigestNowRadar | Unset
        if isinstance(_radar, Unset):
            radar = UNSET
        else:
            radar = PlaceWeatherDigestNowRadar.from_dict(_radar)

        _lightning = d.pop("lightning", UNSET)
        lightning: PlaceWeatherDigestNowLightning | Unset
        if isinstance(_lightning, Unset):
            lightning = UNSET
        else:
            lightning = PlaceWeatherDigestNowLightning.from_dict(_lightning)

        _alerts = d.pop("alerts", UNSET)
        alerts: list[PlaceWeatherDigestNowAlertsItem] | Unset = UNSET
        if _alerts is not UNSET:
            alerts = []
            for alerts_item_data in _alerts:
                alerts_item = PlaceWeatherDigestNowAlertsItem.from_dict(
                    alerts_item_data
                )

                alerts.append(alerts_item)

        _alerts_status = d.pop("alertsStatus", UNSET)
        alerts_status: PlaceWeatherDigestNowAlertsStatus | Unset
        if isinstance(_alerts_status, Unset):
            alerts_status = UNSET
        else:
            alerts_status = PlaceWeatherDigestNowAlertsStatus.from_dict(_alerts_status)

        place_weather_digest_now = cls(
            radar=radar,
            lightning=lightning,
            alerts=alerts,
            alerts_status=alerts_status,
        )

        place_weather_digest_now.additional_properties = d
        return place_weather_digest_now

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
