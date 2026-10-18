from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..models.trip_hazard_section import TripHazardSection
from ..types import UNSET, Unset

T = TypeVar("T", bound="TripHazard")


@_attrs_define
class TripHazard:
    """A hazard found on or near a trip corridor (114)

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        external_id (str):
        kind (str):
        section (TripHazardSection): on route, near route (within 2 × buffer), or a forecast hazard whose validFrom
            falls in the window
        distance_along_route_m (float):
        title (None | str | Unset):
        severity (None | Severity | Unset):
        distance_from_route_m (float | Unset):
        valid_from (datetime.datetime | None | Unset):
        valid_until (datetime.datetime | None | Unset):
        ahead (bool | None | Unset): With an anchor: hazard is ahead of the anchor's projection
        eta_seconds (float | None | Unset):
    """

    ref: str
    source_id: str
    external_id: str
    kind: str
    section: TripHazardSection
    distance_along_route_m: float
    title: None | str | Unset = UNSET
    severity: None | Severity | Unset = UNSET
    distance_from_route_m: float | Unset = UNSET
    valid_from: datetime.datetime | None | Unset = UNSET
    valid_until: datetime.datetime | None | Unset = UNSET
    ahead: bool | None | Unset = UNSET
    eta_seconds: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        source_id = self.source_id

        external_id = self.external_id

        kind = self.kind

        section = self.section.value

        distance_along_route_m = self.distance_along_route_m

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        elif isinstance(self.severity, Severity):
            severity = self.severity.value
        else:
            severity = self.severity

        distance_from_route_m = self.distance_from_route_m

        valid_from: None | str | Unset
        if isinstance(self.valid_from, Unset):
            valid_from = UNSET
        elif isinstance(self.valid_from, datetime.datetime):
            valid_from = self.valid_from.isoformat()
        else:
            valid_from = self.valid_from

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

        ahead: bool | None | Unset
        if isinstance(self.ahead, Unset):
            ahead = UNSET
        else:
            ahead = self.ahead

        eta_seconds: float | None | Unset
        if isinstance(self.eta_seconds, Unset):
            eta_seconds = UNSET
        else:
            eta_seconds = self.eta_seconds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "sourceId": source_id,
                "externalId": external_id,
                "kind": kind,
                "section": section,
                "distanceAlongRouteM": distance_along_route_m,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if severity is not UNSET:
            field_dict["severity"] = severity
        if distance_from_route_m is not UNSET:
            field_dict["distanceFromRouteM"] = distance_from_route_m
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if ahead is not UNSET:
            field_dict["ahead"] = ahead
        if eta_seconds is not UNSET:
            field_dict["etaSeconds"] = eta_seconds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        kind = d.pop("kind")

        section = TripHazardSection(d.pop("section"))

        distance_along_route_m = d.pop("distanceAlongRouteM")

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_severity(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                severity_type_0 = Severity(data)

                return severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        distance_from_route_m = d.pop("distanceFromRouteM", UNSET)

        def _parse_valid_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_from_type_0 = datetime.datetime.fromisoformat(data)

                return valid_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_from = _parse_valid_from(d.pop("validFrom", UNSET))

        def _parse_valid_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_until_type_0 = datetime.datetime.fromisoformat(data)

                return valid_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_until = _parse_valid_until(d.pop("validUntil", UNSET))

        def _parse_ahead(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        ahead = _parse_ahead(d.pop("ahead", UNSET))

        def _parse_eta_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        eta_seconds = _parse_eta_seconds(d.pop("etaSeconds", UNSET))

        trip_hazard = cls(
            ref=ref,
            source_id=source_id,
            external_id=external_id,
            kind=kind,
            section=section,
            distance_along_route_m=distance_along_route_m,
            title=title,
            severity=severity,
            distance_from_route_m=distance_from_route_m,
            valid_from=valid_from,
            valid_until=valid_until,
            ahead=ahead,
            eta_seconds=eta_seconds,
        )

        trip_hazard.additional_properties = d
        return trip_hazard

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
