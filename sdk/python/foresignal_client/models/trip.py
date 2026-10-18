from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.trip_schedule import TripSchedule


T = TypeVar("T", bound="Trip")


@_attrs_define
class Trip:
    """A trip or commute corridor watch (114)

    Attributes:
        name (str):
        schedule (TripSchedule): One-off departure or recurring commute schedule (114)
        expected_duration (str): Trip duration; the watch window is [departAt, departAt + expectedDuration]. ISO-8601
            duration (P7D, PT15M) or shorthand (7d, 15m, 12h). Example: 15m.
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        corridor (Geometry): GeoJSON geometry (RFC 7946), WGS84
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
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
        next_occurrence (datetime.datetime | None | Unset):
        created_by (str | Unset):
    """

    name: str
    schedule: TripSchedule
    expected_duration: str
    id: str
    corridor: Geometry
    created_at: datetime.datetime
    updated_at: datetime.datetime
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
    next_occurrence: datetime.datetime | None | Unset = UNSET
    created_by: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        name = self.name

        schedule = self.schedule.to_dict()

        expected_duration = self.expected_duration

        id = self.id

        corridor = self.corridor.to_dict()

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

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

        next_occurrence: None | str | Unset
        if isinstance(self.next_occurrence, Unset):
            next_occurrence = UNSET
        elif isinstance(self.next_occurrence, datetime.datetime):
            next_occurrence = self.next_occurrence.isoformat()
        else:
            next_occurrence = self.next_occurrence

        created_by = self.created_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "schedule": schedule,
                "expectedDuration": expected_duration,
                "id": id,
                "corridor": corridor,
                "createdAt": created_at,
                "updatedAt": updated_at,
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
        if next_occurrence is not UNSET:
            field_dict["nextOccurrence"] = next_occurrence
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.trip_schedule import TripSchedule

        d = dict(src_dict)
        name = d.pop("name")

        schedule = TripSchedule.from_dict(d.pop("schedule"))

        expected_duration = d.pop("expectedDuration")

        id = d.pop("id")

        corridor = Geometry.from_dict(d.pop("corridor"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

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

        def _parse_next_occurrence(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_occurrence_type_0 = datetime.datetime.fromisoformat(data)

                return next_occurrence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_occurrence = _parse_next_occurrence(d.pop("nextOccurrence", UNSET))

        created_by = d.pop("createdBy", UNSET)

        trip = cls(
            name=name,
            schedule=schedule,
            expected_duration=expected_duration,
            id=id,
            corridor=corridor,
            created_at=created_at,
            updated_at=updated_at,
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
            next_occurrence=next_occurrence,
            created_by=created_by,
        )

        trip.additional_properties = d
        return trip

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
