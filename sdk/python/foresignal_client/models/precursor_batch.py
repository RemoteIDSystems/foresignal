from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_batch_method import PrecursorBatchMethod
from ..models.precursor_batch_mode import PrecursorBatchMode
from ..models.precursor_batch_status import PrecursorBatchStatus
from ..models.precursor_batch_trigger import PrecursorBatchTrigger
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precursor_batch_windows import PrecursorBatchWindows


T = TypeVar("T", bound="PrecursorBatch")


@_attrs_define
class PrecursorBatch:
    """One mining or re-test run with its multiple-testing accounting

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        mode (PrecursorBatchMode):
        status (PrecursorBatchStatus):
        started_at (datetime.datetime):
        candidates_generated (int):
        tested (int):
        capped (int): Candidates dropped by `analysis.precursors.maxTestsPerRun`
        survivors (int):
        validated (int):
        q (float):
        method (PrecursorBatchMethod):
        job_id (None | str | Unset): 103 job (`precursor-mining`)
        trigger (PrecursorBatchTrigger | Unset):
        finished_at (datetime.datetime | None | Unset):
        insufficient (int | Unset): Candidates skipped as insufficient (range under 60 days, coverage gaps over 30%,
            fewer than 10 antecedents or too little series overlap)
        windows (PrecursorBatchWindows | Unset): The global range and its split (`start`, `split`, `end`,
            `discoveryFraction`); for a re-test, `split` starts the re-test window
        error (None | str | Unset):
        manifest_id (None | str | Unset):
    """

    id: str
    mode: PrecursorBatchMode
    status: PrecursorBatchStatus
    started_at: datetime.datetime
    candidates_generated: int
    tested: int
    capped: int
    survivors: int
    validated: int
    q: float
    method: PrecursorBatchMethod
    job_id: None | str | Unset = UNSET
    trigger: PrecursorBatchTrigger | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    insufficient: int | Unset = UNSET
    windows: PrecursorBatchWindows | Unset = UNSET
    error: None | str | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        mode = self.mode.value

        status = self.status.value

        started_at = self.started_at.isoformat()

        candidates_generated = self.candidates_generated

        tested = self.tested

        capped = self.capped

        survivors = self.survivors

        validated = self.validated

        q = self.q

        method = self.method.value

        job_id: None | str | Unset
        if isinstance(self.job_id, Unset):
            job_id = UNSET
        else:
            job_id = self.job_id

        trigger: str | Unset = UNSET
        if not isinstance(self.trigger, Unset):
            trigger = self.trigger.value

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        insufficient = self.insufficient

        windows: dict[str, Any] | Unset = UNSET
        if not isinstance(self.windows, Unset):
            windows = self.windows.to_dict()

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "mode": mode,
                "status": status,
                "startedAt": started_at,
                "candidatesGenerated": candidates_generated,
                "tested": tested,
                "capped": capped,
                "survivors": survivors,
                "validated": validated,
                "q": q,
                "method": method,
            }
        )
        if job_id is not UNSET:
            field_dict["jobId"] = job_id
        if trigger is not UNSET:
            field_dict["trigger"] = trigger
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if insufficient is not UNSET:
            field_dict["insufficient"] = insufficient
        if windows is not UNSET:
            field_dict["windows"] = windows
        if error is not UNSET:
            field_dict["error"] = error
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_batch_windows import PrecursorBatchWindows

        d = dict(src_dict)
        id = d.pop("id")

        mode = PrecursorBatchMode(d.pop("mode"))

        status = PrecursorBatchStatus(d.pop("status"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        candidates_generated = d.pop("candidatesGenerated")

        tested = d.pop("tested")

        capped = d.pop("capped")

        survivors = d.pop("survivors")

        validated = d.pop("validated")

        q = d.pop("q")

        method = PrecursorBatchMethod(d.pop("method"))

        def _parse_job_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        job_id = _parse_job_id(d.pop("jobId", UNSET))

        _trigger = d.pop("trigger", UNSET)
        trigger: PrecursorBatchTrigger | Unset
        if isinstance(_trigger, Unset):
            trigger = UNSET
        else:
            trigger = PrecursorBatchTrigger(_trigger)

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

        insufficient = d.pop("insufficient", UNSET)

        _windows = d.pop("windows", UNSET)
        windows: PrecursorBatchWindows | Unset
        if isinstance(_windows, Unset):
            windows = UNSET
        else:
            windows = PrecursorBatchWindows.from_dict(_windows)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        precursor_batch = cls(
            id=id,
            mode=mode,
            status=status,
            started_at=started_at,
            candidates_generated=candidates_generated,
            tested=tested,
            capped=capped,
            survivors=survivors,
            validated=validated,
            q=q,
            method=method,
            job_id=job_id,
            trigger=trigger,
            finished_at=finished_at,
            insufficient=insufficient,
            windows=windows,
            error=error,
            manifest_id=manifest_id,
        )

        precursor_batch.additional_properties = d
        return precursor_batch

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
