from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.backfill_job_status import BackfillJobStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="BackfillJob")


@_attrs_define
class BackfillJob:
    """A resumable, budget-limited history backfill job (117)

    Attributes:
        id (str):
        source_id (str):
        status (BackfillJobStatus):
        from_ (datetime.date):
        created_at (datetime.datetime):
        cursor (datetime.date | None | Unset): Oldest report date fetched so far
        pages_fetched (int | Unset):
        versions_written (int | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        error (None | str | Unset):
    """

    id: str
    source_id: str
    status: BackfillJobStatus
    from_: datetime.date
    created_at: datetime.datetime
    cursor: datetime.date | None | Unset = UNSET
    pages_fetched: int | Unset = UNSET
    versions_written: int | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        source_id = self.source_id

        status = self.status.value

        from_ = self.from_.isoformat()

        created_at = self.created_at.isoformat()

        cursor: None | str | Unset
        if isinstance(self.cursor, Unset):
            cursor = UNSET
        elif isinstance(self.cursor, datetime.date):
            cursor = self.cursor.isoformat()
        else:
            cursor = self.cursor

        pages_fetched = self.pages_fetched

        versions_written = self.versions_written

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
                "sourceId": source_id,
                "status": status,
                "from": from_,
                "createdAt": created_at,
            }
        )
        if cursor is not UNSET:
            field_dict["cursor"] = cursor
        if pages_fetched is not UNSET:
            field_dict["pagesFetched"] = pages_fetched
        if versions_written is not UNSET:
            field_dict["versionsWritten"] = versions_written
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        source_id = d.pop("sourceId")

        status = BackfillJobStatus(d.pop("status"))

        from_ = datetime.date.fromisoformat(d.pop("from"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_cursor(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                cursor_type_0 = datetime.date.fromisoformat(data)

                return cursor_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        cursor = _parse_cursor(d.pop("cursor", UNSET))

        pages_fetched = d.pop("pagesFetched", UNSET)

        versions_written = d.pop("versionsWritten", UNSET)

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

        backfill_job = cls(
            id=id,
            source_id=source_id,
            status=status,
            from_=from_,
            created_at=created_at,
            cursor=cursor,
            pages_fetched=pages_fetched,
            versions_written=versions_written,
            started_at=started_at,
            finished_at=finished_at,
            error=error,
        )

        backfill_job.additional_properties = d
        return backfill_job

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
