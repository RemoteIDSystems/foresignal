from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.maintenance_run_status import MaintenanceRunStatus
from ..models.maintenance_run_trigger import MaintenanceRunTrigger
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.maintenance_run_steps_item import MaintenanceRunStepsItem


T = TypeVar("T", bound="MaintenanceRun")


@_attrs_define
class MaintenanceRun:
    """Monthly maintenance: ordered jobs, each outcome recorded without stopping later steps

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        trigger (MaintenanceRunTrigger): monthly-cycle (the monthly cycle run), manual (runAnalysisMaintenance) or
            schedule (the 1st at 03:30 UTC when no monthly cycle is enabled)
        status (MaintenanceRunStatus):
        started_at (datetime.datetime):
        steps (list[MaintenanceRunStepsItem]):
        finished_at (datetime.datetime | None | Unset):
    """

    id: str
    trigger: MaintenanceRunTrigger
    status: MaintenanceRunStatus
    started_at: datetime.datetime
    steps: list[MaintenanceRunStepsItem]
    finished_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        trigger = self.trigger.value

        status = self.status.value

        started_at = self.started_at.isoformat()

        steps = []
        for steps_item_data in self.steps:
            steps_item = steps_item_data.to_dict()
            steps.append(steps_item)

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "trigger": trigger,
                "status": status,
                "startedAt": started_at,
                "steps": steps,
            }
        )
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.maintenance_run_steps_item import MaintenanceRunStepsItem

        d = dict(src_dict)
        id = d.pop("id")

        trigger = MaintenanceRunTrigger(d.pop("trigger"))

        status = MaintenanceRunStatus(d.pop("status"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        steps = []
        _steps = d.pop("steps")
        for steps_item_data in _steps:
            steps_item = MaintenanceRunStepsItem.from_dict(steps_item_data)

            steps.append(steps_item)

        def _parse_finished_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                finished_at_type_0 = datetime.datetime.fromisoformat(data)

                return finished_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        finished_at = _parse_finished_at(d.pop("finishedAt", UNSET))

        maintenance_run = cls(
            id=id,
            trigger=trigger,
            status=status,
            started_at=started_at,
            steps=steps,
            finished_at=finished_at,
        )

        maintenance_run.additional_properties = d
        return maintenance_run

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
