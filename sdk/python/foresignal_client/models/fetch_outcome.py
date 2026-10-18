from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.persist_reason_counts import PersistReasonCounts


T = TypeVar("T", bound="FetchOutcome")


@_attrs_define
class FetchOutcome:
    """
    Attributes:
        source_id (str):
        success (bool):
        records_seen (int):
        records_changed (int): Alias of `persisted` (kept for v1.1 clients)
        started_at (datetime.datetime | Unset):
        duration_ms (int | Unset):
        from_seq (int | None | Unset):
        to_seq (int | None | Unset):
        error (None | str | Unset):
        fetch_id (int | None | Unset): fetch_log row of this fetch
        persisted (int | Unset):
        live (int | Unset): Live (non-persisted) updates
        touched (int | Unset): Touch rows written
        touches_throttled (int | Unset):
        late (int | Unset):
        skipped (int | Unset):
        rejected (int | Unset):
        by_reason (PersistReasonCounts | Unset): Persisted-version counts per persist reason; all nine reasons are
            always present (zeros included)
    """

    source_id: str
    success: bool
    records_seen: int
    records_changed: int
    started_at: datetime.datetime | Unset = UNSET
    duration_ms: int | Unset = UNSET
    from_seq: int | None | Unset = UNSET
    to_seq: int | None | Unset = UNSET
    error: None | str | Unset = UNSET
    fetch_id: int | None | Unset = UNSET
    persisted: int | Unset = UNSET
    live: int | Unset = UNSET
    touched: int | Unset = UNSET
    touches_throttled: int | Unset = UNSET
    late: int | Unset = UNSET
    skipped: int | Unset = UNSET
    rejected: int | Unset = UNSET
    by_reason: PersistReasonCounts | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        success = self.success

        records_seen = self.records_seen

        records_changed = self.records_changed

        started_at: str | Unset = UNSET
        if not isinstance(self.started_at, Unset):
            started_at = self.started_at.isoformat()

        duration_ms = self.duration_ms

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

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        fetch_id: int | None | Unset
        if isinstance(self.fetch_id, Unset):
            fetch_id = UNSET
        else:
            fetch_id = self.fetch_id

        persisted = self.persisted

        live = self.live

        touched = self.touched

        touches_throttled = self.touches_throttled

        late = self.late

        skipped = self.skipped

        rejected = self.rejected

        by_reason: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_reason, Unset):
            by_reason = self.by_reason.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "success": success,
                "recordsSeen": records_seen,
                "recordsChanged": records_changed,
            }
        )
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if error is not UNSET:
            field_dict["error"] = error
        if fetch_id is not UNSET:
            field_dict["fetchId"] = fetch_id
        if persisted is not UNSET:
            field_dict["persisted"] = persisted
        if live is not UNSET:
            field_dict["live"] = live
        if touched is not UNSET:
            field_dict["touched"] = touched
        if touches_throttled is not UNSET:
            field_dict["touchesThrottled"] = touches_throttled
        if late is not UNSET:
            field_dict["late"] = late
        if skipped is not UNSET:
            field_dict["skipped"] = skipped
        if rejected is not UNSET:
            field_dict["rejected"] = rejected
        if by_reason is not UNSET:
            field_dict["byReason"] = by_reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.persist_reason_counts import PersistReasonCounts

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        success = d.pop("success")

        records_seen = d.pop("recordsSeen")

        records_changed = d.pop("recordsChanged")

        _started_at = d.pop("startedAt", UNSET)
        started_at: datetime.datetime | Unset
        if isinstance(_started_at, Unset):
            started_at = UNSET
        else:
            started_at = datetime.datetime.fromisoformat(_started_at)

        duration_ms = d.pop("durationMs", UNSET)

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

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_fetch_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        fetch_id = _parse_fetch_id(d.pop("fetchId", UNSET))

        persisted = d.pop("persisted", UNSET)

        live = d.pop("live", UNSET)

        touched = d.pop("touched", UNSET)

        touches_throttled = d.pop("touchesThrottled", UNSET)

        late = d.pop("late", UNSET)

        skipped = d.pop("skipped", UNSET)

        rejected = d.pop("rejected", UNSET)

        _by_reason = d.pop("byReason", UNSET)
        by_reason: PersistReasonCounts | Unset
        if isinstance(_by_reason, Unset):
            by_reason = UNSET
        else:
            by_reason = PersistReasonCounts.from_dict(_by_reason)

        fetch_outcome = cls(
            source_id=source_id,
            success=success,
            records_seen=records_seen,
            records_changed=records_changed,
            started_at=started_at,
            duration_ms=duration_ms,
            from_seq=from_seq,
            to_seq=to_seq,
            error=error,
            fetch_id=fetch_id,
            persisted=persisted,
            live=live,
            touched=touched,
            touches_throttled=touches_throttled,
            late=late,
            skipped=skipped,
            rejected=rejected,
            by_reason=by_reason,
        )

        fetch_outcome.additional_properties = d
        return fetch_outcome

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
