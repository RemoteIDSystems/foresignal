from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.intel_event_input_origin import IntelEventInputOrigin
from ..models.intel_event_input_type import IntelEventInputType
from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.intel_event_member_input import IntelEventMemberInput


T = TypeVar("T", bound="IntelEventInput")


@_attrs_define
class IntelEventInput:
    """Manual or agent-created intelligence event. Confidence is computed by EventScorer; a client-supplied confidence is
    rejected (400).

        Attributes:
            type_ (IntelEventInputType):
            title (str):
            members (list[IntelEventMemberInput]):
            severity (Severity | Unset):
            origin (IntelEventInputOrigin | Unset):  Default: IntelEventInputOrigin.AGENT.
            geometry (Geometry | None | Unset):
            start_time (datetime.datetime | Unset):
            end_time (datetime.datetime | None | Unset):
            notes (str | Unset):
    """

    type_: IntelEventInputType
    title: str
    members: list[IntelEventMemberInput]
    severity: Severity | Unset = UNSET
    origin: IntelEventInputOrigin | Unset = IntelEventInputOrigin.AGENT
    geometry: Geometry | None | Unset = UNSET
    start_time: datetime.datetime | Unset = UNSET
    end_time: datetime.datetime | None | Unset = UNSET
    notes: str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        type_ = self.type_.value

        title = self.title

        members = []
        for members_item_data in self.members:
            members_item = members_item_data.to_dict()
            members.append(members_item)

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        origin: str | Unset = UNSET
        if not isinstance(self.origin, Unset):
            origin = self.origin.value

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        start_time: str | Unset = UNSET
        if not isinstance(self.start_time, Unset):
            start_time = self.start_time.isoformat()

        end_time: None | str | Unset
        if isinstance(self.end_time, Unset):
            end_time = UNSET
        elif isinstance(self.end_time, datetime.datetime):
            end_time = self.end_time.isoformat()
        else:
            end_time = self.end_time

        notes = self.notes

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "type": type_,
                "title": title,
                "members": members,
            }
        )
        if severity is not UNSET:
            field_dict["severity"] = severity
        if origin is not UNSET:
            field_dict["origin"] = origin
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if start_time is not UNSET:
            field_dict["startTime"] = start_time
        if end_time is not UNSET:
            field_dict["endTime"] = end_time
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.intel_event_member_input import IntelEventMemberInput

        d = dict(src_dict)
        type_ = IntelEventInputType(d.pop("type"))

        title = d.pop("title")

        members = []
        _members = d.pop("members")
        for members_item_data in _members:
            members_item = IntelEventMemberInput.from_dict(members_item_data)

            members.append(members_item)

        _severity = d.pop("severity", UNSET)
        severity: Severity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = Severity(_severity)

        _origin = d.pop("origin", UNSET)
        origin: IntelEventInputOrigin | Unset
        if isinstance(_origin, Unset):
            origin = UNSET
        else:
            origin = IntelEventInputOrigin(_origin)

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        _start_time = d.pop("startTime", UNSET)
        start_time: datetime.datetime | Unset
        if isinstance(_start_time, Unset):
            start_time = UNSET
        else:
            start_time = datetime.datetime.fromisoformat(_start_time)

        def _parse_end_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                end_time_type_0 = datetime.datetime.fromisoformat(data)

                return end_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        end_time = _parse_end_time(d.pop("endTime", UNSET))

        notes = d.pop("notes", UNSET)

        intel_event_input = cls(
            type_=type_,
            title=title,
            members=members,
            severity=severity,
            origin=origin,
            geometry=geometry,
            start_time=start_time,
            end_time=end_time,
            notes=notes,
        )

        return intel_event_input
