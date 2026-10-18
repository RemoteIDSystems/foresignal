from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.fetch_log_entry_status import FetchLogEntryStatus
from ..models.fetch_log_entry_trigger import FetchLogEntryTrigger
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.coverage import Coverage


T = TypeVar("T", bound="FetchLogEntry")


@_attrs_define
class FetchLogEntry:
    """Provenance of one fetch, stream flush or push batch (`fetch_log` row); never contains bodies

    Attributes:
        id (int): Fetch id (= `batchId` on versions)
        trigger (FetchLogEntryTrigger):
        status (FetchLogEntryStatus):
        started_at (datetime.datetime):
        received_at (datetime.datetime | None | Unset):
        ingested_at (datetime.datetime | None | Unset): Set only when the writer inserted the row
        http_status (int | None | Unset):
        request_count (int | Unset):
        url_redacted (None | str | Unset): Last request URL with credentials redacted
        etag (None | str | Unset):
        bytes_ (int | None | Unset):
        body_sha_256 (None | str | Unset):
        adapter_version (None | str | Unset):
        licence (None | str | Unset):
        principal (None | str | Unset): Pushing principal (PUSH rows)
        records_seen (int | Unset):
        rejected (int | Unset):
        late (int | Unset):
        skipped (int | Unset):
        versions_written (int | Unset):
        live_updates (int | Unset):
        touches (int | Unset):
        from_seq (int | None | Unset):
        to_seq (int | None | Unset):
        coverage (Coverage | None | Unset):
        error (None | str | Unset): Redacted error (≤ 2000 chars)
        run_id (None | str | Unset): Shared by the chunks of one chunked fetch run (094)
    """

    id: int
    trigger: FetchLogEntryTrigger
    status: FetchLogEntryStatus
    started_at: datetime.datetime
    received_at: datetime.datetime | None | Unset = UNSET
    ingested_at: datetime.datetime | None | Unset = UNSET
    http_status: int | None | Unset = UNSET
    request_count: int | Unset = UNSET
    url_redacted: None | str | Unset = UNSET
    etag: None | str | Unset = UNSET
    bytes_: int | None | Unset = UNSET
    body_sha_256: None | str | Unset = UNSET
    adapter_version: None | str | Unset = UNSET
    licence: None | str | Unset = UNSET
    principal: None | str | Unset = UNSET
    records_seen: int | Unset = UNSET
    rejected: int | Unset = UNSET
    late: int | Unset = UNSET
    skipped: int | Unset = UNSET
    versions_written: int | Unset = UNSET
    live_updates: int | Unset = UNSET
    touches: int | Unset = UNSET
    from_seq: int | None | Unset = UNSET
    to_seq: int | None | Unset = UNSET
    coverage: Coverage | None | Unset = UNSET
    error: None | str | Unset = UNSET
    run_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.coverage import Coverage

        id = self.id

        trigger = self.trigger.value

        status = self.status.value

        started_at = self.started_at.isoformat()

        received_at: None | str | Unset
        if isinstance(self.received_at, Unset):
            received_at = UNSET
        elif isinstance(self.received_at, datetime.datetime):
            received_at = self.received_at.isoformat()
        else:
            received_at = self.received_at

        ingested_at: None | str | Unset
        if isinstance(self.ingested_at, Unset):
            ingested_at = UNSET
        elif isinstance(self.ingested_at, datetime.datetime):
            ingested_at = self.ingested_at.isoformat()
        else:
            ingested_at = self.ingested_at

        http_status: int | None | Unset
        if isinstance(self.http_status, Unset):
            http_status = UNSET
        else:
            http_status = self.http_status

        request_count = self.request_count

        url_redacted: None | str | Unset
        if isinstance(self.url_redacted, Unset):
            url_redacted = UNSET
        else:
            url_redacted = self.url_redacted

        etag: None | str | Unset
        if isinstance(self.etag, Unset):
            etag = UNSET
        else:
            etag = self.etag

        bytes_: int | None | Unset
        if isinstance(self.bytes_, Unset):
            bytes_ = UNSET
        else:
            bytes_ = self.bytes_

        body_sha_256: None | str | Unset
        if isinstance(self.body_sha_256, Unset):
            body_sha_256 = UNSET
        else:
            body_sha_256 = self.body_sha_256

        adapter_version: None | str | Unset
        if isinstance(self.adapter_version, Unset):
            adapter_version = UNSET
        else:
            adapter_version = self.adapter_version

        licence: None | str | Unset
        if isinstance(self.licence, Unset):
            licence = UNSET
        else:
            licence = self.licence

        principal: None | str | Unset
        if isinstance(self.principal, Unset):
            principal = UNSET
        else:
            principal = self.principal

        records_seen = self.records_seen

        rejected = self.rejected

        late = self.late

        skipped = self.skipped

        versions_written = self.versions_written

        live_updates = self.live_updates

        touches = self.touches

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

        coverage: dict[str, Any] | None | Unset
        if isinstance(self.coverage, Unset):
            coverage = UNSET
        elif isinstance(self.coverage, Coverage):
            coverage = self.coverage.to_dict()
        else:
            coverage = self.coverage

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        run_id: None | str | Unset
        if isinstance(self.run_id, Unset):
            run_id = UNSET
        else:
            run_id = self.run_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "trigger": trigger,
                "status": status,
                "startedAt": started_at,
            }
        )
        if received_at is not UNSET:
            field_dict["receivedAt"] = received_at
        if ingested_at is not UNSET:
            field_dict["ingestedAt"] = ingested_at
        if http_status is not UNSET:
            field_dict["httpStatus"] = http_status
        if request_count is not UNSET:
            field_dict["requestCount"] = request_count
        if url_redacted is not UNSET:
            field_dict["urlRedacted"] = url_redacted
        if etag is not UNSET:
            field_dict["etag"] = etag
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if body_sha_256 is not UNSET:
            field_dict["bodySha256"] = body_sha_256
        if adapter_version is not UNSET:
            field_dict["adapterVersion"] = adapter_version
        if licence is not UNSET:
            field_dict["licence"] = licence
        if principal is not UNSET:
            field_dict["principal"] = principal
        if records_seen is not UNSET:
            field_dict["recordsSeen"] = records_seen
        if rejected is not UNSET:
            field_dict["rejected"] = rejected
        if late is not UNSET:
            field_dict["late"] = late
        if skipped is not UNSET:
            field_dict["skipped"] = skipped
        if versions_written is not UNSET:
            field_dict["versionsWritten"] = versions_written
        if live_updates is not UNSET:
            field_dict["liveUpdates"] = live_updates
        if touches is not UNSET:
            field_dict["touches"] = touches
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if error is not UNSET:
            field_dict["error"] = error
        if run_id is not UNSET:
            field_dict["runId"] = run_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coverage import Coverage

        d = dict(src_dict)
        id = d.pop("id")

        trigger = FetchLogEntryTrigger(d.pop("trigger"))

        status = FetchLogEntryStatus(d.pop("status"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        def _parse_received_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                received_at_type_0 = datetime.datetime.fromisoformat(data)

                return received_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        received_at = _parse_received_at(d.pop("receivedAt", UNSET))

        def _parse_ingested_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                ingested_at_type_0 = datetime.datetime.fromisoformat(data)

                return ingested_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        ingested_at = _parse_ingested_at(d.pop("ingestedAt", UNSET))

        def _parse_http_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        http_status = _parse_http_status(d.pop("httpStatus", UNSET))

        request_count = d.pop("requestCount", UNSET)

        def _parse_url_redacted(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url_redacted = _parse_url_redacted(d.pop("urlRedacted", UNSET))

        def _parse_etag(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        etag = _parse_etag(d.pop("etag", UNSET))

        def _parse_bytes_(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_ = _parse_bytes_(d.pop("bytes", UNSET))

        def _parse_body_sha_256(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        body_sha_256 = _parse_body_sha_256(d.pop("bodySha256", UNSET))

        def _parse_adapter_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        adapter_version = _parse_adapter_version(d.pop("adapterVersion", UNSET))

        def _parse_licence(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence = _parse_licence(d.pop("licence", UNSET))

        def _parse_principal(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal = _parse_principal(d.pop("principal", UNSET))

        records_seen = d.pop("recordsSeen", UNSET)

        rejected = d.pop("rejected", UNSET)

        late = d.pop("late", UNSET)

        skipped = d.pop("skipped", UNSET)

        versions_written = d.pop("versionsWritten", UNSET)

        live_updates = d.pop("liveUpdates", UNSET)

        touches = d.pop("touches", UNSET)

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

        def _parse_coverage(data: object) -> Coverage | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coverage_type_0 = Coverage.from_dict(data)

                return coverage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Coverage | None | Unset, data)

        coverage = _parse_coverage(d.pop("coverage", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_run_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run_id = _parse_run_id(d.pop("runId", UNSET))

        fetch_log_entry = cls(
            id=id,
            trigger=trigger,
            status=status,
            started_at=started_at,
            received_at=received_at,
            ingested_at=ingested_at,
            http_status=http_status,
            request_count=request_count,
            url_redacted=url_redacted,
            etag=etag,
            bytes_=bytes_,
            body_sha_256=body_sha_256,
            adapter_version=adapter_version,
            licence=licence,
            principal=principal,
            records_seen=records_seen,
            rejected=rejected,
            late=late,
            skipped=skipped,
            versions_written=versions_written,
            live_updates=live_updates,
            touches=touches,
            from_seq=from_seq,
            to_seq=to_seq,
            coverage=coverage,
            error=error,
            run_id=run_id,
        )

        fetch_log_entry.additional_properties = d
        return fetch_log_entry

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
