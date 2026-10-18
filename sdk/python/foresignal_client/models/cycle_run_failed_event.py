from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.cycle_run_failed_event_cadence import CycleRunFailedEventCadence
from ..types import UNSET, Unset

T = TypeVar("T", bound="CycleRunFailedEvent")


@_attrs_define
class CycleRunFailedEvent:
    """SSE payload for `cycle.run_failed` (topic `assessments`)

    Attributes:
        run_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cadence (CycleRunFailedEventCadence):
        reason (str):
        at (datetime.datetime):
        cycle_id (None | str | Unset):
        situation_id (None | str | Unset):
    """

    run_id: str
    cadence: CycleRunFailedEventCadence
    reason: str
    at: datetime.datetime
    cycle_id: None | str | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        run_id = self.run_id

        cadence = self.cadence.value

        reason = self.reason

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "runId": run_id,
                "cadence": cadence,
                "reason": reason,
                "at": at,
            }
        )
        if cycle_id is not UNSET:
            field_dict["cycleId"] = cycle_id
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        run_id = d.pop("runId")

        cadence = CycleRunFailedEventCadence(d.pop("cadence"))

        reason = d.pop("reason")

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

        cycle_run_failed_event = cls(
            run_id=run_id,
            cadence=cadence,
            reason=reason,
            at=at,
            cycle_id=cycle_id,
            situation_id=situation_id,
        )

        cycle_run_failed_event.additional_properties = d
        return cycle_run_failed_event

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
