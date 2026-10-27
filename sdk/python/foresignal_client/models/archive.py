from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.archive_files_item import ArchiveFilesItem


T = TypeVar("T", bound="Archive")


@_attrs_define
class Archive:
    """A cold-storage archive segment: data phased out of the hot DB, verified, then purged locally (B3/179). Restored rows
    go to the restore area `record_version_restored`, not back into record_version.

        Attributes:
            id (str):
            status (str): pending (accepted), running (exporting), verifying (offloading and checking each file on the
                tier), complete (verified and recorded), purged (and gone from the hot store), failed (nothing was purged),
                restoring, restored
            created_at (datetime.datetime):
            target (str): Archive tier target
            cutoff (datetime.datetime | None | Unset): Data older than this was archived; null when the run took only what
                retention removed anyway
            location (None | str | Unset): The segment's directory (`local`) or `s3://bucket/prefix/segments/{id}`
            size_bytes (int | None | Unset): Compressed size of every file
            item_count (int | None | Unset): Rows archived (for a dry run: rows that would be)
            checksum (None | str | Unset): SHA-256 over each file's `key:sha256` line, in key order; every file's own
                SHA-256 is in `files`
            purged_from_hot (bool | Unset): True once the archived data was purged from the hot store Default: False.
            restored_at (datetime.datetime | None | Unset):
            trigger (str | Unset): What started the run: runArchive, `archive.schedule`, or the retention size cap (R2-015)
            domain (str | Unset): What was archived: `record_version`
            sources (list[str] | Unset): Sources in the segment
            from_seq (int | None | Unset):
            to_seq (int | None | Unset):
            from_time (datetime.datetime | None | Unset): Earliest ingested_at archived
            to_time (datetime.datetime | None | Unset): Latest ingested_at archived
            files (list[ArchiveFilesItem] | Unset): One gzip-compressed NDJSON file per source, one record_version row per
                line
            error (None | str | Unset): Why it failed, or a note (rows the purge kept back, a pending Glacier thaw, rows
                restored)
            updated_at (datetime.datetime | Unset):
            completed_at (datetime.datetime | None | Unset):
            purged_at (datetime.datetime | None | Unset):
    """

    id: str
    status: str
    created_at: datetime.datetime
    target: str
    cutoff: datetime.datetime | None | Unset = UNSET
    location: None | str | Unset = UNSET
    size_bytes: int | None | Unset = UNSET
    item_count: int | None | Unset = UNSET
    checksum: None | str | Unset = UNSET
    purged_from_hot: bool | Unset = False
    restored_at: datetime.datetime | None | Unset = UNSET
    trigger: str | Unset = UNSET
    domain: str | Unset = UNSET
    sources: list[str] | Unset = UNSET
    from_seq: int | None | Unset = UNSET
    to_seq: int | None | Unset = UNSET
    from_time: datetime.datetime | None | Unset = UNSET
    to_time: datetime.datetime | None | Unset = UNSET
    files: list[ArchiveFilesItem] | Unset = UNSET
    error: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    completed_at: datetime.datetime | None | Unset = UNSET
    purged_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        status = self.status

        created_at = self.created_at.isoformat()

        target = self.target

        cutoff: None | str | Unset
        if isinstance(self.cutoff, Unset):
            cutoff = UNSET
        elif isinstance(self.cutoff, datetime.datetime):
            cutoff = self.cutoff.isoformat()
        else:
            cutoff = self.cutoff

        location: None | str | Unset
        if isinstance(self.location, Unset):
            location = UNSET
        else:
            location = self.location

        size_bytes: int | None | Unset
        if isinstance(self.size_bytes, Unset):
            size_bytes = UNSET
        else:
            size_bytes = self.size_bytes

        item_count: int | None | Unset
        if isinstance(self.item_count, Unset):
            item_count = UNSET
        else:
            item_count = self.item_count

        checksum: None | str | Unset
        if isinstance(self.checksum, Unset):
            checksum = UNSET
        else:
            checksum = self.checksum

        purged_from_hot = self.purged_from_hot

        restored_at: None | str | Unset
        if isinstance(self.restored_at, Unset):
            restored_at = UNSET
        elif isinstance(self.restored_at, datetime.datetime):
            restored_at = self.restored_at.isoformat()
        else:
            restored_at = self.restored_at

        trigger = self.trigger

        domain = self.domain

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        from_seq: int | None | Unset
        if isinstance(self.from_seq, Unset):
            from_seq = UNSET
        else:
            from_seq = self.from_seq

        to_seq: int | None | Unset
        if isinstance(self.to_seq, Unset):
            to_seq = UNSET
        else:
            to_seq = self.to_seq

        from_time: None | str | Unset
        if isinstance(self.from_time, Unset):
            from_time = UNSET
        elif isinstance(self.from_time, datetime.datetime):
            from_time = self.from_time.isoformat()
        else:
            from_time = self.from_time

        to_time: None | str | Unset
        if isinstance(self.to_time, Unset):
            to_time = UNSET
        elif isinstance(self.to_time, datetime.datetime):
            to_time = self.to_time.isoformat()
        else:
            to_time = self.to_time

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

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        completed_at: None | str | Unset
        if isinstance(self.completed_at, Unset):
            completed_at = UNSET
        elif isinstance(self.completed_at, datetime.datetime):
            completed_at = self.completed_at.isoformat()
        else:
            completed_at = self.completed_at

        purged_at: None | str | Unset
        if isinstance(self.purged_at, Unset):
            purged_at = UNSET
        elif isinstance(self.purged_at, datetime.datetime):
            purged_at = self.purged_at.isoformat()
        else:
            purged_at = self.purged_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "status": status,
                "createdAt": created_at,
                "target": target,
            }
        )
        if cutoff is not UNSET:
            field_dict["cutoff"] = cutoff
        if location is not UNSET:
            field_dict["location"] = location
        if size_bytes is not UNSET:
            field_dict["sizeBytes"] = size_bytes
        if item_count is not UNSET:
            field_dict["itemCount"] = item_count
        if checksum is not UNSET:
            field_dict["checksum"] = checksum
        if purged_from_hot is not UNSET:
            field_dict["purgedFromHot"] = purged_from_hot
        if restored_at is not UNSET:
            field_dict["restoredAt"] = restored_at
        if trigger is not UNSET:
            field_dict["trigger"] = trigger
        if domain is not UNSET:
            field_dict["domain"] = domain
        if sources is not UNSET:
            field_dict["sources"] = sources
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if from_time is not UNSET:
            field_dict["fromTime"] = from_time
        if to_time is not UNSET:
            field_dict["toTime"] = to_time
        if files is not UNSET:
            field_dict["files"] = files
        if error is not UNSET:
            field_dict["error"] = error
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if completed_at is not UNSET:
            field_dict["completedAt"] = completed_at
        if purged_at is not UNSET:
            field_dict["purgedAt"] = purged_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.archive_files_item import ArchiveFilesItem

        d = dict(src_dict)
        id = d.pop("id")

        status = d.pop("status")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        target = d.pop("target")

        def _parse_cutoff(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                cutoff_type_0 = datetime.datetime.fromisoformat(data)

                return cutoff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        cutoff = _parse_cutoff(d.pop("cutoff", UNSET))

        def _parse_location(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        location = _parse_location(d.pop("location", UNSET))

        def _parse_size_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        size_bytes = _parse_size_bytes(d.pop("sizeBytes", UNSET))

        def _parse_item_count(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        item_count = _parse_item_count(d.pop("itemCount", UNSET))

        def _parse_checksum(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        checksum = _parse_checksum(d.pop("checksum", UNSET))

        purged_from_hot = d.pop("purgedFromHot", UNSET)

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

        trigger = d.pop("trigger", UNSET)

        domain = d.pop("domain", UNSET)

        sources = cast(list[str], d.pop("sources", UNSET))

        def _parse_from_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        from_seq = _parse_from_seq(d.pop("fromSeq", UNSET))

        def _parse_to_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        to_seq = _parse_to_seq(d.pop("toSeq", UNSET))

        def _parse_from_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_time_type_0 = datetime.datetime.fromisoformat(data)

                return from_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_time = _parse_from_time(d.pop("fromTime", UNSET))

        def _parse_to_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_time_type_0 = datetime.datetime.fromisoformat(data)

                return to_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to_time = _parse_to_time(d.pop("toTime", UNSET))

        _files = d.pop("files", UNSET)
        files: list[ArchiveFilesItem] | Unset = UNSET
        if _files is not UNSET:
            files = []
            for files_item_data in _files:
                files_item = ArchiveFilesItem.from_dict(files_item_data)

                files.append(files_item)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        def _parse_completed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                completed_at_type_0 = datetime.datetime.fromisoformat(data)

                return completed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        completed_at = _parse_completed_at(d.pop("completedAt", UNSET))

        def _parse_purged_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                purged_at_type_0 = datetime.datetime.fromisoformat(data)

                return purged_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        purged_at = _parse_purged_at(d.pop("purgedAt", UNSET))

        archive = cls(
            id=id,
            status=status,
            created_at=created_at,
            target=target,
            cutoff=cutoff,
            location=location,
            size_bytes=size_bytes,
            item_count=item_count,
            checksum=checksum,
            purged_from_hot=purged_from_hot,
            restored_at=restored_at,
            trigger=trigger,
            domain=domain,
            sources=sources,
            from_seq=from_seq,
            to_seq=to_seq,
            from_time=from_time,
            to_time=to_time,
            files=files,
            error=error,
            updated_at=updated_at,
            completed_at=completed_at,
            purged_at=purged_at,
        )

        archive.additional_properties = d
        return archive

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
