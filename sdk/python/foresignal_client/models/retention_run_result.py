from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.retention_run_result_consumer_clamp import (
        RetentionRunResultConsumerClamp,
    )
    from ..models.retention_run_result_db_cap_type_0 import RetentionRunResultDbCapType0
    from ..models.retention_run_result_per_source import RetentionRunResultPerSource


T = TypeVar("T", bound="RetentionRunResult")


@_attrs_define
class RetentionRunResult:
    """
    Attributes:
        versions_deleted (int):
        heads_deleted (int): Alias of headsPurged
        started_at (datetime.datetime | Unset):
        duration_ms (int | Unset):
        alerts_deleted (int | Unset):
        usage_rows_deleted (int | Unset):
        partitions_dropped (int | Unset):
        heads_purged (int | Unset):
        fetch_log_deleted (int | Unset):
        seq_clock_deleted (int | Unset):
        notifications_deleted (int | Unset):
        skipped_sources (list[str] | Unset): Sources skipped this run after a failed delete (no HeadPurge)
        consumer_clamp (RetentionRunResultConsumerClamp | Unset):
        per_source (RetentionRunResultPerSource | Unset): Keyed by source id
        cap_window_days (int | None | Unset): The cap-history window this run applied, in days (R2-015); null when
            history is not capped by age
        db_cap (None | RetentionRunResultDbCapType0 | Unset): What the database-size cap did this run (R2-015); null
            when it is off
    """

    versions_deleted: int
    heads_deleted: int
    started_at: datetime.datetime | Unset = UNSET
    duration_ms: int | Unset = UNSET
    alerts_deleted: int | Unset = UNSET
    usage_rows_deleted: int | Unset = UNSET
    partitions_dropped: int | Unset = UNSET
    heads_purged: int | Unset = UNSET
    fetch_log_deleted: int | Unset = UNSET
    seq_clock_deleted: int | Unset = UNSET
    notifications_deleted: int | Unset = UNSET
    skipped_sources: list[str] | Unset = UNSET
    consumer_clamp: RetentionRunResultConsumerClamp | Unset = UNSET
    per_source: RetentionRunResultPerSource | Unset = UNSET
    cap_window_days: int | None | Unset = UNSET
    db_cap: None | RetentionRunResultDbCapType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.retention_run_result_db_cap_type_0 import (
            RetentionRunResultDbCapType0,
        )

        versions_deleted = self.versions_deleted

        heads_deleted = self.heads_deleted

        started_at: str | Unset = UNSET
        if not isinstance(self.started_at, Unset):
            started_at = self.started_at.isoformat()

        duration_ms = self.duration_ms

        alerts_deleted = self.alerts_deleted

        usage_rows_deleted = self.usage_rows_deleted

        partitions_dropped = self.partitions_dropped

        heads_purged = self.heads_purged

        fetch_log_deleted = self.fetch_log_deleted

        seq_clock_deleted = self.seq_clock_deleted

        notifications_deleted = self.notifications_deleted

        skipped_sources: list[str] | Unset = UNSET
        if not isinstance(self.skipped_sources, Unset):
            skipped_sources = self.skipped_sources

        consumer_clamp: dict[str, Any] | Unset = UNSET
        if not isinstance(self.consumer_clamp, Unset):
            consumer_clamp = self.consumer_clamp.to_dict()

        per_source: dict[str, Any] | Unset = UNSET
        if not isinstance(self.per_source, Unset):
            per_source = self.per_source.to_dict()

        cap_window_days: int | None | Unset
        if isinstance(self.cap_window_days, Unset):
            cap_window_days = UNSET
        else:
            cap_window_days = self.cap_window_days

        db_cap: dict[str, Any] | None | Unset
        if isinstance(self.db_cap, Unset):
            db_cap = UNSET
        elif isinstance(self.db_cap, RetentionRunResultDbCapType0):
            db_cap = self.db_cap.to_dict()
        else:
            db_cap = self.db_cap

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "versionsDeleted": versions_deleted,
                "headsDeleted": heads_deleted,
            }
        )
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms
        if alerts_deleted is not UNSET:
            field_dict["alertsDeleted"] = alerts_deleted
        if usage_rows_deleted is not UNSET:
            field_dict["usageRowsDeleted"] = usage_rows_deleted
        if partitions_dropped is not UNSET:
            field_dict["partitionsDropped"] = partitions_dropped
        if heads_purged is not UNSET:
            field_dict["headsPurged"] = heads_purged
        if fetch_log_deleted is not UNSET:
            field_dict["fetchLogDeleted"] = fetch_log_deleted
        if seq_clock_deleted is not UNSET:
            field_dict["seqClockDeleted"] = seq_clock_deleted
        if notifications_deleted is not UNSET:
            field_dict["notificationsDeleted"] = notifications_deleted
        if skipped_sources is not UNSET:
            field_dict["skippedSources"] = skipped_sources
        if consumer_clamp is not UNSET:
            field_dict["consumerClamp"] = consumer_clamp
        if per_source is not UNSET:
            field_dict["perSource"] = per_source
        if cap_window_days is not UNSET:
            field_dict["capWindowDays"] = cap_window_days
        if db_cap is not UNSET:
            field_dict["dbCap"] = db_cap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.retention_run_result_consumer_clamp import (
            RetentionRunResultConsumerClamp,
        )
        from ..models.retention_run_result_db_cap_type_0 import (
            RetentionRunResultDbCapType0,
        )
        from ..models.retention_run_result_per_source import RetentionRunResultPerSource

        d = dict(src_dict)
        versions_deleted = d.pop("versionsDeleted")

        heads_deleted = d.pop("headsDeleted")

        _started_at = d.pop("startedAt", UNSET)
        started_at: datetime.datetime | Unset
        if isinstance(_started_at, Unset):
            started_at = UNSET
        else:
            started_at = datetime.datetime.fromisoformat(_started_at)

        duration_ms = d.pop("durationMs", UNSET)

        alerts_deleted = d.pop("alertsDeleted", UNSET)

        usage_rows_deleted = d.pop("usageRowsDeleted", UNSET)

        partitions_dropped = d.pop("partitionsDropped", UNSET)

        heads_purged = d.pop("headsPurged", UNSET)

        fetch_log_deleted = d.pop("fetchLogDeleted", UNSET)

        seq_clock_deleted = d.pop("seqClockDeleted", UNSET)

        notifications_deleted = d.pop("notificationsDeleted", UNSET)

        skipped_sources = cast(list[str], d.pop("skippedSources", UNSET))

        _consumer_clamp = d.pop("consumerClamp", UNSET)
        consumer_clamp: RetentionRunResultConsumerClamp | Unset
        if isinstance(_consumer_clamp, Unset):
            consumer_clamp = UNSET
        else:
            consumer_clamp = RetentionRunResultConsumerClamp.from_dict(_consumer_clamp)

        _per_source = d.pop("perSource", UNSET)
        per_source: RetentionRunResultPerSource | Unset
        if isinstance(_per_source, Unset):
            per_source = UNSET
        else:
            per_source = RetentionRunResultPerSource.from_dict(_per_source)

        def _parse_cap_window_days(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        cap_window_days = _parse_cap_window_days(d.pop("capWindowDays", UNSET))

        def _parse_db_cap(data: object) -> None | RetentionRunResultDbCapType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                db_cap_type_0 = RetentionRunResultDbCapType0.from_dict(data)

                return db_cap_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RetentionRunResultDbCapType0 | Unset, data)

        db_cap = _parse_db_cap(d.pop("dbCap", UNSET))

        retention_run_result = cls(
            versions_deleted=versions_deleted,
            heads_deleted=heads_deleted,
            started_at=started_at,
            duration_ms=duration_ms,
            alerts_deleted=alerts_deleted,
            usage_rows_deleted=usage_rows_deleted,
            partitions_dropped=partitions_dropped,
            heads_purged=heads_purged,
            fetch_log_deleted=fetch_log_deleted,
            seq_clock_deleted=seq_clock_deleted,
            notifications_deleted=notifications_deleted,
            skipped_sources=skipped_sources,
            consumer_clamp=consumer_clamp,
            per_source=per_source,
            cap_window_days=cap_window_days,
            db_cap=db_cap,
        )

        retention_run_result.additional_properties = d
        return retention_run_result

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
