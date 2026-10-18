from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.maintenance_run_steps_item_status import MaintenanceRunStepsItemStatus
from ..models.maintenance_run_steps_item_step import MaintenanceRunStepsItemStep
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.maintenance_run_steps_item_result_type_0 import (
        MaintenanceRunStepsItemResultType0,
    )


T = TypeVar("T", bound="MaintenanceRunStepsItem")


@_attrs_define
class MaintenanceRunStepsItem:
    """One maintenance step

    Attributes:
        step (MaintenanceRunStepsItemStep):
        status (MaintenanceRunStepsItemStatus):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        job_id (None | str | Unset):
        result (MaintenanceRunStepsItemResultType0 | None | Unset):
        error (None | str | Unset):
    """

    step: MaintenanceRunStepsItemStep
    status: MaintenanceRunStepsItemStatus
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    job_id: None | str | Unset = UNSET
    result: MaintenanceRunStepsItemResultType0 | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.maintenance_run_steps_item_result_type_0 import (
            MaintenanceRunStepsItemResultType0,
        )

        step = self.step.value

        status = self.status.value

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        job_id: None | str | Unset
        if isinstance(self.job_id, Unset):
            job_id = UNSET
        else:
            job_id = self.job_id

        result: dict[str, Any] | None | Unset
        if isinstance(self.result, Unset):
            result = UNSET
        elif isinstance(self.result, MaintenanceRunStepsItemResultType0):
            result = self.result.to_dict()
        else:
            result = self.result

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "step": step,
                "status": status,
            }
        )
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if job_id is not UNSET:
            field_dict["jobId"] = job_id
        if result is not UNSET:
            field_dict["result"] = result
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.maintenance_run_steps_item_result_type_0 import (
            MaintenanceRunStepsItemResultType0,
        )

        d = dict(src_dict)
        step = MaintenanceRunStepsItemStep(d.pop("step"))

        status = MaintenanceRunStepsItemStatus(d.pop("status"))

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

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

        def _parse_job_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        job_id = _parse_job_id(d.pop("jobId", UNSET))

        def _parse_result(
            data: object,
        ) -> MaintenanceRunStepsItemResultType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_0 = MaintenanceRunStepsItemResultType0.from_dict(data)

                return result_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MaintenanceRunStepsItemResultType0 | None | Unset, data)

        result = _parse_result(d.pop("result", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        maintenance_run_steps_item = cls(
            step=step,
            status=status,
            started_at=started_at,
            finished_at=finished_at,
            job_id=job_id,
            result=result,
            error=error,
        )

        maintenance_run_steps_item.additional_properties = d
        return maintenance_run_steps_item

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
