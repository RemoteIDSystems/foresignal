from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.trip_schedule import TripSchedule


T = TypeVar("T", bound="TripInput")


@_attrs_define
class TripInput:
    """Create or replace a trip. The route comes from the client's own router; Foresignal never routes (114)

    Attributes:
        name (str):
        schedule (TripSchedule): One-off departure or recurring commute schedule (114)
        expected_duration (str): Trip duration; the watch window is [departAt, departAt + expectedDuration]. ISO-8601
            duration (P7D, PT15M) or shorthand (7d, 15m, 12h). Example: 15m.
        route_place_id (None | str | Unset): A 036 route place (LineString + bufferMeters); or give `route`
        route (Geometry | None | Unset): GeoJSON LineString, at most 5,000 vertices
        buffer_meters (int | Unset):  Default: 2000.
        lead_time (str | Unset): Pre-trip check runs at departAt − leadTime. ISO-8601 duration (P7D, PT15M) or shorthand
            (7d, 15m, 12h). Default: '60m'. Example: 60m.
        hazard_kinds (list[str] | Unset):
        min_severity (None | Severity | Unset):
        channel_ids (list[str] | Unset):
        anchor (None | str | Unset): Moving-anchor record `{sourceId}/{externalId}` (036), e.g. an OwnTracks device
        notify_when_clear (bool | Unset): Deliver a 'route clear' briefing too Default: True.
        enabled (bool | Unset):  Default: True.
    """

    name: str
    schedule: TripSchedule
    expected_duration: str
    route_place_id: None | str | Unset = UNSET
    route: Geometry | None | Unset = UNSET
    buffer_meters: int | Unset = 2000
    lead_time: str | Unset = "60m"
    hazard_kinds: list[str] | Unset = UNSET
    min_severity: None | Severity | Unset = UNSET
    channel_ids: list[str] | Unset = UNSET
    anchor: None | str | Unset = UNSET
    notify_when_clear: bool | Unset = True
    enabled: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        name = self.name

        schedule = self.schedule.to_dict()

        expected_duration = self.expected_duration

        route_place_id: None | str | Unset
        if isinstance(self.route_place_id, Unset):
            route_place_id = UNSET
        else:
            route_place_id = self.route_place_id

        route: dict[str, Any] | None | Unset
        if isinstance(self.route, Unset):
            route = UNSET
        elif isinstance(self.route, Geometry):
            route = self.route.to_dict()
        else:
            route = self.route

        buffer_meters = self.buffer_meters

        lead_time = self.lead_time

        hazard_kinds: list[str] | Unset = UNSET
        if not isinstance(self.hazard_kinds, Unset):
            hazard_kinds = self.hazard_kinds

        min_severity: None | str | Unset
        if isinstance(self.min_severity, Unset):
            min_severity = UNSET
        elif isinstance(self.min_severity, Severity):
            min_severity = self.min_severity.value
        else:
            min_severity = self.min_severity

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = self.channel_ids

        anchor: None | str | Unset
        if isinstance(self.anchor, Unset):
            anchor = UNSET
        else:
            anchor = self.anchor

        notify_when_clear = self.notify_when_clear

        enabled = self.enabled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "schedule": schedule,
                "expectedDuration": expected_duration,
            }
        )
        if route_place_id is not UNSET:
            field_dict["routePlaceId"] = route_place_id
        if route is not UNSET:
            field_dict["route"] = route
        if buffer_meters is not UNSET:
            field_dict["bufferMeters"] = buffer_meters
        if lead_time is not UNSET:
            field_dict["leadTime"] = lead_time
        if hazard_kinds is not UNSET:
            field_dict["hazardKinds"] = hazard_kinds
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if anchor is not UNSET:
            field_dict["anchor"] = anchor
        if notify_when_clear is not UNSET:
            field_dict["notifyWhenClear"] = notify_when_clear
        if enabled is not UNSET:
            field_dict["enabled"] = enabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.trip_schedule import TripSchedule

        d = dict(src_dict)
        name = d.pop("name")

        schedule = TripSchedule.from_dict(d.pop("schedule"))

        expected_duration = d.pop("expectedDuration")

        def _parse_route_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        route_place_id = _parse_route_place_id(d.pop("routePlaceId", UNSET))

        def _parse_route(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                route_type_0 = Geometry.from_dict(data)

                return route_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        route = _parse_route(d.pop("route", UNSET))

        buffer_meters = d.pop("bufferMeters", UNSET)

        lead_time = d.pop("leadTime", UNSET)

        hazard_kinds = cast(list[str], d.pop("hazardKinds", UNSET))

        def _parse_min_severity(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_0 = Severity(data)

                return min_severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        min_severity = _parse_min_severity(d.pop("minSeverity", UNSET))

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        def _parse_anchor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        anchor = _parse_anchor(d.pop("anchor", UNSET))

        notify_when_clear = d.pop("notifyWhenClear", UNSET)

        enabled = d.pop("enabled", UNSET)

        trip_input = cls(
            name=name,
            schedule=schedule,
            expected_duration=expected_duration,
            route_place_id=route_place_id,
            route=route,
            buffer_meters=buffer_meters,
            lead_time=lead_time,
            hazard_kinds=hazard_kinds,
            min_severity=min_severity,
            channel_ids=channel_ids,
            anchor=anchor,
            notify_when_clear=notify_when_clear,
            enabled=enabled,
        )

        trip_input.additional_properties = d
        return trip_input

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
