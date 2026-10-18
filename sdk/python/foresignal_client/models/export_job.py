from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.export_job_format import ExportJobFormat
from ..models.export_job_kind import ExportJobKind
from ..models.export_job_status import ExportJobStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.export_file import ExportFile
    from ..models.export_job_spec import ExportJobSpec
    from ..models.export_licence_summary import ExportLicenceSummary


T = TypeVar("T", bound="ExportJob")


@_attrs_define
class ExportJob:
    """An async export, snapshot or offline pack job (111)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (ExportJobKind):
        format_ (ExportJobFormat):
        status (ExportJobStatus):
        progress (int):
        created_at (datetime.datetime):
        spec (ExportJobSpec | Unset): The submitted ExportSpec, SnapshotInput or PackSpec
        principal (str | Unset): Token id that created the job
        row_count (int | None | Unset):
        bytes_ (int | None | Unset):
        manifest_sha_256 (None | str | Unset):
        licence_summary (ExportLicenceSummary | None | Unset):
        watermark_seq (int | None | Unset):
        personal_use (bool | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        expires_at (datetime.datetime | None | Unset): Default created + exports.ttl (7 d); null for snapshots without
            expiresAt
        error (None | str | Unset):
        download_url (None | str | Unset): /v1/exports/{id}/download once DONE
        manifest_url (None | str | Unset):
        files (list[ExportFile] | Unset):
        product_id (None | str | Unset):
    """

    id: str
    kind: ExportJobKind
    format_: ExportJobFormat
    status: ExportJobStatus
    progress: int
    created_at: datetime.datetime
    spec: ExportJobSpec | Unset = UNSET
    principal: str | Unset = UNSET
    row_count: int | None | Unset = UNSET
    bytes_: int | None | Unset = UNSET
    manifest_sha_256: None | str | Unset = UNSET
    licence_summary: ExportLicenceSummary | None | Unset = UNSET
    watermark_seq: int | None | Unset = UNSET
    personal_use: bool | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    error: None | str | Unset = UNSET
    download_url: None | str | Unset = UNSET
    manifest_url: None | str | Unset = UNSET
    files: list[ExportFile] | Unset = UNSET
    product_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.export_licence_summary import ExportLicenceSummary

        id = self.id

        kind = self.kind.value

        format_ = self.format_.value

        status = self.status.value

        progress = self.progress

        created_at = self.created_at.isoformat()

        spec: dict[str, Any] | Unset = UNSET
        if not isinstance(self.spec, Unset):
            spec = self.spec.to_dict()

        principal = self.principal

        row_count: int | None | Unset
        if isinstance(self.row_count, Unset):
            row_count = UNSET
        else:
            row_count = self.row_count

        bytes_: int | None | Unset
        if isinstance(self.bytes_, Unset):
            bytes_ = UNSET
        else:
            bytes_ = self.bytes_

        manifest_sha_256: None | str | Unset
        if isinstance(self.manifest_sha_256, Unset):
            manifest_sha_256 = UNSET
        else:
            manifest_sha_256 = self.manifest_sha_256

        licence_summary: dict[str, Any] | None | Unset
        if isinstance(self.licence_summary, Unset):
            licence_summary = UNSET
        elif isinstance(self.licence_summary, ExportLicenceSummary):
            licence_summary = self.licence_summary.to_dict()
        else:
            licence_summary = self.licence_summary

        watermark_seq: int | None | Unset
        if isinstance(self.watermark_seq, Unset):
            watermark_seq = UNSET
        else:
            watermark_seq = self.watermark_seq

        personal_use = self.personal_use

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

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        download_url: None | str | Unset
        if isinstance(self.download_url, Unset):
            download_url = UNSET
        else:
            download_url = self.download_url

        manifest_url: None | str | Unset
        if isinstance(self.manifest_url, Unset):
            manifest_url = UNSET
        else:
            manifest_url = self.manifest_url

        files: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.files, Unset):
            files = []
            for files_item_data in self.files:
                files_item = files_item_data.to_dict()
                files.append(files_item)

        product_id: None | str | Unset
        if isinstance(self.product_id, Unset):
            product_id = UNSET
        else:
            product_id = self.product_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "format": format_,
                "status": status,
                "progress": progress,
                "createdAt": created_at,
            }
        )
        if spec is not UNSET:
            field_dict["spec"] = spec
        if principal is not UNSET:
            field_dict["principal"] = principal
        if row_count is not UNSET:
            field_dict["rowCount"] = row_count
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if manifest_sha_256 is not UNSET:
            field_dict["manifestSha256"] = manifest_sha_256
        if licence_summary is not UNSET:
            field_dict["licenceSummary"] = licence_summary
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq
        if personal_use is not UNSET:
            field_dict["personalUse"] = personal_use
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if error is not UNSET:
            field_dict["error"] = error
        if download_url is not UNSET:
            field_dict["downloadUrl"] = download_url
        if manifest_url is not UNSET:
            field_dict["manifestUrl"] = manifest_url
        if files is not UNSET:
            field_dict["files"] = files
        if product_id is not UNSET:
            field_dict["productId"] = product_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.export_file import ExportFile
        from ..models.export_job_spec import ExportJobSpec
        from ..models.export_licence_summary import ExportLicenceSummary

        d = dict(src_dict)
        id = d.pop("id")

        kind = ExportJobKind(d.pop("kind"))

        format_ = ExportJobFormat(d.pop("format"))

        status = ExportJobStatus(d.pop("status"))

        progress = d.pop("progress")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _spec = d.pop("spec", UNSET)
        spec: ExportJobSpec | Unset
        if isinstance(_spec, Unset):
            spec = UNSET
        else:
            spec = ExportJobSpec.from_dict(_spec)

        principal = d.pop("principal", UNSET)

        def _parse_row_count(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        row_count = _parse_row_count(d.pop("rowCount", UNSET))

        def _parse_bytes_(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_ = _parse_bytes_(d.pop("bytes", UNSET))

        def _parse_manifest_sha_256(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_sha_256 = _parse_manifest_sha_256(d.pop("manifestSha256", UNSET))

        def _parse_licence_summary(data: object) -> ExportLicenceSummary | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                licence_summary_type_0 = ExportLicenceSummary.from_dict(data)

                return licence_summary_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ExportLicenceSummary | None | Unset, data)

        licence_summary = _parse_licence_summary(d.pop("licenceSummary", UNSET))

        def _parse_watermark_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        watermark_seq = _parse_watermark_seq(d.pop("watermarkSeq", UNSET))

        personal_use = d.pop("personalUse", UNSET)

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

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_download_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        download_url = _parse_download_url(d.pop("downloadUrl", UNSET))

        def _parse_manifest_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_url = _parse_manifest_url(d.pop("manifestUrl", UNSET))

        _files = d.pop("files", UNSET)
        files: list[ExportFile] | Unset = UNSET
        if _files is not UNSET:
            files = []
            for files_item_data in _files:
                files_item = ExportFile.from_dict(files_item_data)

                files.append(files_item)

        def _parse_product_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        product_id = _parse_product_id(d.pop("productId", UNSET))

        export_job = cls(
            id=id,
            kind=kind,
            format_=format_,
            status=status,
            progress=progress,
            created_at=created_at,
            spec=spec,
            principal=principal,
            row_count=row_count,
            bytes_=bytes_,
            manifest_sha_256=manifest_sha_256,
            licence_summary=licence_summary,
            watermark_seq=watermark_seq,
            personal_use=personal_use,
            started_at=started_at,
            finished_at=finished_at,
            expires_at=expires_at,
            error=error,
            download_url=download_url,
            manifest_url=manifest_url,
            files=files,
            product_id=product_id,
        )

        export_job.additional_properties = d
        return export_job

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
