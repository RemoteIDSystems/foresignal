from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.assessment_published_event_cadence import AssessmentPublishedEventCadence
from ..models.assessment_published_event_status import AssessmentPublishedEventStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="AssessmentPublishedEvent")


@_attrs_define
class AssessmentPublishedEvent:
    """SSE payload for `assessment.published` (topic `assessments`)

    Attributes:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cadence (AssessmentPublishedEventCadence):
        status (AssessmentPublishedEventStatus):
        at (datetime.datetime):
        cycle_id (None | str | Unset):
        situation_id (None | str | Unset):
        period_start (datetime.datetime | Unset):
        period_end (datetime.datetime | Unset):
        headline (None | str | Unset):
        private (bool | Unset):
    """

    assessment_id: str
    cadence: AssessmentPublishedEventCadence
    status: AssessmentPublishedEventStatus
    at: datetime.datetime
    cycle_id: None | str | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    period_start: datetime.datetime | Unset = UNSET
    period_end: datetime.datetime | Unset = UNSET
    headline: None | str | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        assessment_id = self.assessment_id

        cadence = self.cadence.value

        status = self.status.value

        at = self.at.isoformat()

        cycle_id: None | str | Unset
        if isinstance(self.cycle_id, Unset):
            cycle_id = UNSET
        else:
            cycle_id = self.cycle_id

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        period_start: str | Unset = UNSET
        if not isinstance(self.period_start, Unset):
            period_start = self.period_start.isoformat()

        period_end: str | Unset = UNSET
        if not isinstance(self.period_end, Unset):
            period_end = self.period_end.isoformat()

        headline: None | str | Unset
        if isinstance(self.headline, Unset):
            headline = UNSET
        else:
            headline = self.headline

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "assessmentId": assessment_id,
                "cadence": cadence,
                "status": status,
                "at": at,
            }
        )
        if cycle_id is not UNSET:
            field_dict["cycleId"] = cycle_id
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if period_start is not UNSET:
            field_dict["periodStart"] = period_start
        if period_end is not UNSET:
            field_dict["periodEnd"] = period_end
        if headline is not UNSET:
            field_dict["headline"] = headline
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        assessment_id = d.pop("assessmentId")

        cadence = AssessmentPublishedEventCadence(d.pop("cadence"))

        status = AssessmentPublishedEventStatus(d.pop("status"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_cycle_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cycle_id = _parse_cycle_id(d.pop("cycleId", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        _period_start = d.pop("periodStart", UNSET)
        period_start: datetime.datetime | Unset
        if isinstance(_period_start, Unset):
            period_start = UNSET
        else:
            period_start = datetime.datetime.fromisoformat(_period_start)

        _period_end = d.pop("periodEnd", UNSET)
        period_end: datetime.datetime | Unset
        if isinstance(_period_end, Unset):
            period_end = UNSET
        else:
            period_end = datetime.datetime.fromisoformat(_period_end)

        def _parse_headline(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        headline = _parse_headline(d.pop("headline", UNSET))

        private = d.pop("private", UNSET)

        assessment_published_event = cls(
            assessment_id=assessment_id,
            cadence=cadence,
            status=status,
            at=at,
            cycle_id=cycle_id,
            situation_id=situation_id,
            period_start=period_start,
            period_end=period_end,
            headline=headline,
            private=private,
        )

        assessment_published_event.additional_properties = d
        return assessment_published_event

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
