from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..models.situation_event_event import SituationEventEvent
from ..models.situation_status import SituationStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_summary import SituationSummary


T = TypeVar("T", bound="SituationEvent")


@_attrs_define
class SituationEvent:
    """SSE payload on topic `situations`: the situation summary plus its version

    Attributes:
        event (SituationEventEvent):
        situation (SituationSummary): Compact situation for lists and SSE events
        version (int):
        at (datetime.datetime):
        previous_status (None | SituationStatus | Unset):
        previous_severity (None | Severity | Unset):
        merged_into (None | str | Unset):
    """

    event: SituationEventEvent
    situation: SituationSummary
    version: int
    at: datetime.datetime
    previous_status: None | SituationStatus | Unset = UNSET
    previous_severity: None | Severity | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event.value

        situation = self.situation.to_dict()

        version = self.version

        at = self.at.isoformat()

        previous_status: None | str | Unset
        if isinstance(self.previous_status, Unset):
            previous_status = UNSET
        elif isinstance(self.previous_status, SituationStatus):
            previous_status = self.previous_status.value
        else:
            previous_status = self.previous_status

        previous_severity: None | str | Unset
        if isinstance(self.previous_severity, Unset):
            previous_severity = UNSET
        elif isinstance(self.previous_severity, Severity):
            previous_severity = self.previous_severity.value
        else:
            previous_severity = self.previous_severity

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "situation": situation,
                "version": version,
                "at": at,
            }
        )
        if previous_status is not UNSET:
            field_dict["previousStatus"] = previous_status
        if previous_severity is not UNSET:
            field_dict["previousSeverity"] = previous_severity
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_summary import SituationSummary

        d = dict(src_dict)
        event = SituationEventEvent(d.pop("event"))

        situation = SituationSummary.from_dict(d.pop("situation"))

        version = d.pop("version")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_previous_status(data: object) -> None | SituationStatus | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_0 = SituationStatus(data)

                return previous_status_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationStatus | Unset, data)

        previous_status = _parse_previous_status(d.pop("previousStatus", UNSET))

        def _parse_previous_severity(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_severity_type_0 = Severity(data)

                return previous_severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        previous_severity = _parse_previous_severity(d.pop("previousSeverity", UNSET))

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        situation_event = cls(
            event=event,
            situation=situation,
            version=version,
            at=at,
            previous_status=previous_status,
            previous_severity=previous_severity,
            merged_into=merged_into,
        )

        situation_event.additional_properties = d
        return situation_event

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
