from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.backup_files_item import BackupFilesItem
    from ..models.import_report import ImportReport


T = TypeVar("T", bound="Backup")


@_attrs_define
class Backup:
    """A single backup artifact (180; prompt R2-017): one `backup_run` row and its files on the target

    Attributes:
        id (str):
        type_ (str): `database`: a `pg_dump --format=custom` archive; `config`: the `exportSettings` bundle (no
            secrets); `full`: both
        status (str): pending → running → complete or failed; a restore goes restoring → restored (a failed restore puts
            the previous status back with `error`)
        created_at (datetime.datetime):
        size_bytes (int | None | Unset):
        target (str | Unset):
        location (None | str | Unset): The directory or `s3://` prefix holding the files
        checksum (None | str | Unset): SHA-256 of the one file, or of the `key:sha256` lines of both files sorted by key
        label (None | str | Unset):
        trigger (str | Unset): `manual` (runBackup) or `scheduled` (backup.schedule)
        files (list[BackupFilesItem] | Unset): The stored files
        error (None | str | Unset): Why the backup, or its last restore, failed
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        restored_at (datetime.datetime | None | Unset): When it was last restored
        import_report (ImportReport | None | Unset): Present on the answer of `restoreBackup` for a `config` backup:
            what the import applied
    """

    id: str
    type_: str
    status: str
    created_at: datetime.datetime
    size_bytes: int | None | Unset = UNSET
    target: str | Unset = UNSET
    location: None | str | Unset = UNSET
    checksum: None | str | Unset = UNSET
    label: None | str | Unset = UNSET
    trigger: str | Unset = UNSET
    files: list[BackupFilesItem] | Unset = UNSET
    error: None | str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    restored_at: datetime.datetime | None | Unset = UNSET
    import_report: ImportReport | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.import_report import ImportReport

        id = self.id

        type_ = self.type_

        status = self.status

        created_at = self.created_at.isoformat()

        size_bytes: int | None | Unset
        if isinstance(self.size_bytes, Unset):
            size_bytes = UNSET
        else:
            size_bytes = self.size_bytes

        target = self.target

        location: None | str | Unset
        if isinstance(self.location, Unset):
            location = UNSET
        else:
            location = self.location

        checksum: None | str | Unset
        if isinstance(self.checksum, Unset):
            checksum = UNSET
        else:
            checksum = self.checksum

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        trigger = self.trigger

        files: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.files, Unset):
            files = []
            for files_item_data in self.files:
                files_item = files_item_data.to_dict()
                files.append(files_item)

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

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

        restored_at: None | str | Unset
        if isinstance(self.restored_at, Unset):
            restored_at = UNSET
        elif isinstance(self.restored_at, datetime.datetime):
            restored_at = self.restored_at.isoformat()
        else:
            restored_at = self.restored_at

        import_report: dict[str, Any] | None | Unset
        if isinstance(self.import_report, Unset):
            import_report = UNSET
        elif isinstance(self.import_report, ImportReport):
            import_report = self.import_report.to_dict()
        else:
            import_report = self.import_report

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "status": status,
                "createdAt": created_at,
            }
        )
        if size_bytes is not UNSET:
            field_dict["sizeBytes"] = size_bytes
        if target is not UNSET:
            field_dict["target"] = target
        if location is not UNSET:
            field_dict["location"] = location
        if checksum is not UNSET:
            field_dict["checksum"] = checksum
        if label is not UNSET:
            field_dict["label"] = label
        if trigger is not UNSET:
            field_dict["trigger"] = trigger
        if files is not UNSET:
            field_dict["files"] = files
        if error is not UNSET:
            field_dict["error"] = error
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if restored_at is not UNSET:
            field_dict["restoredAt"] = restored_at
        if import_report is not UNSET:
            field_dict["importReport"] = import_report

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.backup_files_item import BackupFilesItem
        from ..models.import_report import ImportReport

        d = dict(src_dict)
        id = d.pop("id")

        type_ = d.pop("type")

        status = d.pop("status")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_size_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        size_bytes = _parse_size_bytes(d.pop("sizeBytes", UNSET))

        target = d.pop("target", UNSET)

        def _parse_location(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        location = _parse_location(d.pop("location", UNSET))

        def _parse_checksum(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        checksum = _parse_checksum(d.pop("checksum", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        trigger = d.pop("trigger", UNSET)

        _files = d.pop("files", UNSET)
        files: list[BackupFilesItem] | Unset = UNSET
        if _files is not UNSET:
            files = []
            for files_item_data in _files:
                files_item = BackupFilesItem.from_dict(files_item_data)

                files.append(files_item)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

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

        def _parse_restored_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                restored_at_type_0 = datetime.datetime.fromisoformat(data)

                return restored_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        restored_at = _parse_restored_at(d.pop("restoredAt", UNSET))

        def _parse_import_report(data: object) -> ImportReport | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                import_report_type_0 = ImportReport.from_dict(data)

                return import_report_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ImportReport | None | Unset, data)

        import_report = _parse_import_report(d.pop("importReport", UNSET))

        backup = cls(
            id=id,
            type_=type_,
            status=status,
            created_at=created_at,
            size_bytes=size_bytes,
            target=target,
            location=location,
            checksum=checksum,
            label=label,
            trigger=trigger,
            files=files,
            error=error,
            started_at=started_at,
            finished_at=finished_at,
            restored_at=restored_at,
            import_report=import_report,
        )

        backup.additional_properties = d
        return backup

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
