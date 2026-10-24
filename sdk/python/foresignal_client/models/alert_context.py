from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alert_context_anchor import AlertContextAnchor
    from ..models.alert_context_near import AlertContextNear
    from ..models.anomaly_observation import AnomalyObservation
    from ..models.weather_context import WeatherContext


T = TypeVar("T", bound="AlertContext")


@_attrs_define
class AlertContext:
    """Computed alert context (`alert.context`); `weather` (056) is the deterministic weather context of a geolocated
    alert, stored before dispatch; list responses carry only `weather.status` and `weather.summary` unless
    `include=context`

        Attributes:
            weather (WeatherContext | Unset): Deterministic weather facts around an alert geometry (056), computed from
                stored frames and record heads at the alert's observed_at within a 500 ms budget (a section that misses it is
                unavailable); non-ok sections carry statusText and no values and mean unknown, never clear
            near (AlertContextNear | Unset): Places a near rule matched (036): their ids and the versions its fence was
                materialized from
            anomaly (AnomalyObservation | None | Unset): Anomaly rule alerts: the evaluated bucket (100)
            anchor (AlertContextAnchor | Unset): Moving-anchor ("near me") rules (036): the anchor record, its seq and the
                km-rounded distance to the target (never coordinates)
    """

    weather: WeatherContext | Unset = UNSET
    near: AlertContextNear | Unset = UNSET
    anomaly: AnomalyObservation | None | Unset = UNSET
    anchor: AlertContextAnchor | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.anomaly_observation import AnomalyObservation

        weather: dict[str, Any] | Unset = UNSET
        if not isinstance(self.weather, Unset):
            weather = self.weather.to_dict()

        near: dict[str, Any] | Unset = UNSET
        if not isinstance(self.near, Unset):
            near = self.near.to_dict()

        anomaly: dict[str, Any] | None | Unset
        if isinstance(self.anomaly, Unset):
            anomaly = UNSET
        elif isinstance(self.anomaly, AnomalyObservation):
            anomaly = self.anomaly.to_dict()
        else:
            anomaly = self.anomaly

        anchor: dict[str, Any] | Unset = UNSET
        if not isinstance(self.anchor, Unset):
            anchor = self.anchor.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if weather is not UNSET:
            field_dict["weather"] = weather
        if near is not UNSET:
            field_dict["near"] = near
        if anomaly is not UNSET:
            field_dict["anomaly"] = anomaly
        if anchor is not UNSET:
            field_dict["anchor"] = anchor

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alert_context_anchor import AlertContextAnchor
        from ..models.alert_context_near import AlertContextNear
        from ..models.anomaly_observation import AnomalyObservation
        from ..models.weather_context import WeatherContext

        d = dict(src_dict)
        _weather = d.pop("weather", UNSET)
        weather: WeatherContext | Unset
        if isinstance(_weather, Unset):
            weather = UNSET
        else:
            weather = WeatherContext.from_dict(_weather)

        _near = d.pop("near", UNSET)
        near: AlertContextNear | Unset
        if isinstance(_near, Unset):
            near = UNSET
        else:
            near = AlertContextNear.from_dict(_near)

        def _parse_anomaly(data: object) -> AnomalyObservation | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                anomaly_type_0 = AnomalyObservation.from_dict(data)

                return anomaly_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnomalyObservation | None | Unset, data)

        anomaly = _parse_anomaly(d.pop("anomaly", UNSET))

        _anchor = d.pop("anchor", UNSET)
        anchor: AlertContextAnchor | Unset
        if isinstance(_anchor, Unset):
            anchor = UNSET
        else:
            anchor = AlertContextAnchor.from_dict(_anchor)

        alert_context = cls(
            weather=weather,
            near=near,
            anomaly=anomaly,
            anchor=anchor,
        )

        alert_context.additional_properties = d
        return alert_context

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
