from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.reference_import_dataset import ReferenceImportDataset
from ..models.reference_import_egress import ReferenceImportEgress
from ..models.reference_import_status import ReferenceImportStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.reference_import_params import ReferenceImportParams


T = TypeVar("T", bound="ReferenceImport")


@_attrs_define
class ReferenceImport:
    """A reference-history import job and its outcome

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        dataset (ReferenceImportDataset):
        status (ReferenceImportStatus):
        rows_read (int):
        rows_written (int):
        licence (str): e.g. `CC-BY-4.0`, `ACLED-EULA`, `public-domain`
        egress (ReferenceImportEgress):
        created_by (str):
        created_at (datetime.datetime):
        params (ReferenceImportParams | Unset):
        status_reason (None | str | Unset): e.g. `credentials absent` for ACLED
        progress (float | None | Unset):
        file_sha_256 (None | str | Unset):
        period_from (datetime.date | None | Unset):
        period_to (datetime.date | None | Unset):
        job_id (None | str | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        error (None | str | Unset):
    """

    id: str
    dataset: ReferenceImportDataset
    status: ReferenceImportStatus
    rows_read: int
    rows_written: int
    licence: str
    egress: ReferenceImportEgress
    created_by: str
    created_at: datetime.datetime
    params: ReferenceImportParams | Unset = UNSET
    status_reason: None | str | Unset = UNSET
    progress: float | None | Unset = UNSET
    file_sha_256: None | str | Unset = UNSET
    period_from: datetime.date | None | Unset = UNSET
    period_to: datetime.date | None | Unset = UNSET
    job_id: None | str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        dataset = self.dataset.value

        status = self.status.value

        rows_read = self.rows_read

        rows_written = self.rows_written

        licence = self.licence

        egress = self.egress.value

        created_by = self.created_by

        created_at = self.created_at.isoformat()

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        status_reason: None | str | Unset
        if isinstance(self.status_reason, Unset):
            status_reason = UNSET
        else:
            status_reason = self.status_reason

        progress: float | None | Unset
        if isinstance(self.progress, Unset):
            progress = UNSET
        else:
            progress = self.progress

        file_sha_256: None | str | Unset
        if isinstance(self.file_sha_256, Unset):
            file_sha_256 = UNSET
        else:
            file_sha_256 = self.file_sha_256

        period_from: None | str | Unset
        if isinstance(self.period_from, Unset):
            period_from = UNSET
        elif isinstance(self.period_from, datetime.date):
            period_from = self.period_from.isoformat()
        else:
            period_from = self.period_from

        period_to: None | str | Unset
        if isinstance(self.period_to, Unset):
            period_to = UNSET
        elif isinstance(self.period_to, datetime.date):
            period_to = self.period_to.isoformat()
        else:
            period_to = self.period_to

        job_id: None | str | Unset
        if isinstance(self.job_id, Unset):
            job_id = UNSET
        else:
            job_id = self.job_id

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

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "dataset": dataset,
                "status": status,
                "rowsRead": rows_read,
                "rowsWritten": rows_written,
                "licence": licence,
                "egress": egress,
                "createdBy": created_by,
                "createdAt": created_at,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params
        if status_reason is not UNSET:
            field_dict["statusReason"] = status_reason
        if progress is not UNSET:
            field_dict["progress"] = progress
        if file_sha_256 is not UNSET:
            field_dict["fileSha256"] = file_sha_256
        if period_from is not UNSET:
            field_dict["periodFrom"] = period_from
        if period_to is not UNSET:
            field_dict["periodTo"] = period_to
        if job_id is not UNSET:
            field_dict["jobId"] = job_id
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.reference_import_params import ReferenceImportParams

        d = dict(src_dict)
        id = d.pop("id")

        dataset = ReferenceImportDataset(d.pop("dataset"))

        status = ReferenceImportStatus(d.pop("status"))

        rows_read = d.pop("rowsRead")

        rows_written = d.pop("rowsWritten")

        licence = d.pop("licence")

        egress = ReferenceImportEgress(d.pop("egress"))

        created_by = d.pop("createdBy")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _params = d.pop("params", UNSET)
        params: ReferenceImportParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = ReferenceImportParams.from_dict(_params)

        def _parse_status_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_reason = _parse_status_reason(d.pop("statusReason", UNSET))

        def _parse_progress(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        progress = _parse_progress(d.pop("progress", UNSET))

        def _parse_file_sha_256(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        file_sha_256 = _parse_file_sha_256(d.pop("fileSha256", UNSET))

        def _parse_period_from(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                period_from_type_0 = datetime.date.fromisoformat(data)

                return period_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        period_from = _parse_period_from(d.pop("periodFrom", UNSET))

        def _parse_period_to(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                period_to_type_0 = datetime.date.fromisoformat(data)

                return period_to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        period_to = _parse_period_to(d.pop("periodTo", UNSET))

        def _parse_job_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        job_id = _parse_job_id(d.pop("jobId", UNSET))

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

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        reference_import = cls(
            id=id,
            dataset=dataset,
            status=status,
            rows_read=rows_read,
            rows_written=rows_written,
            licence=licence,
            egress=egress,
            created_by=created_by,
            created_at=created_at,
            params=params,
            status_reason=status_reason,
            progress=progress,
            file_sha_256=file_sha_256,
            period_from=period_from,
            period_to=period_to,
            job_id=job_id,
            started_at=started_at,
            finished_at=finished_at,
            error=error,
        )

        reference_import.additional_properties = d
        return reference_import

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
