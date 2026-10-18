from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ingest_outcome_by_reason import IngestOutcomeByReason
    from ..models.ingest_outcome_rejections_item import IngestOutcomeRejectionsItem


T = TypeVar("T", bound="IngestOutcome")


@_attrs_define
class IngestOutcome:
    """
    Attributes:
        seen (int):
        changed (int): Deprecated alias of `persisted`
        rejected (int):
        persisted (int): Versions written
        from_seq (int | None | Unset):
        to_seq (int | None | Unset):
        rejections (list[IngestOutcomeRejectionsItem] | Unset):
        live (int | Unset): Observations written as live-only updates, without a version
        touched (int | Unset): Observations written as touches (unchanged content, refreshed observedAt, lastReceivedAt
            and validity)
        touches_throttled (int | Unset):
        late (int | Unset):
        skipped (int | Unset):
        by_reason (IngestOutcomeByReason | Unset): Persisted versions per persist reason; every reason is present, zeros
            included: FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE, CHANGE, SAMPLE, KEYFRAME, HEARTBEAT, TRIGGER_EDGE
        version_bytes (int | Unset):
    """

    seen: int
    changed: int
    rejected: int
    persisted: int
    from_seq: int | None | Unset = UNSET
    to_seq: int | None | Unset = UNSET
    rejections: list[IngestOutcomeRejectionsItem] | Unset = UNSET
    live: int | Unset = UNSET
    touched: int | Unset = UNSET
    touches_throttled: int | Unset = UNSET
    late: int | Unset = UNSET
    skipped: int | Unset = UNSET
    by_reason: IngestOutcomeByReason | Unset = UNSET
    version_bytes: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        seen = self.seen

        changed = self.changed

        rejected = self.rejected

        persisted = self.persisted

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

        rejections: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.rejections, Unset):
            rejections = []
            for rejections_item_data in self.rejections:
                rejections_item = rejections_item_data.to_dict()
                rejections.append(rejections_item)

        live = self.live

        touched = self.touched

        touches_throttled = self.touches_throttled

        late = self.late

        skipped = self.skipped

        by_reason: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_reason, Unset):
            by_reason = self.by_reason.to_dict()

        version_bytes = self.version_bytes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "seen": seen,
                "changed": changed,
                "rejected": rejected,
                "persisted": persisted,
            }
        )
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if rejections is not UNSET:
            field_dict["rejections"] = rejections
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
        if by_reason is not UNSET:
            field_dict["byReason"] = by_reason
        if version_bytes is not UNSET:
            field_dict["versionBytes"] = version_bytes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ingest_outcome_by_reason import IngestOutcomeByReason
        from ..models.ingest_outcome_rejections_item import IngestOutcomeRejectionsItem

        d = dict(src_dict)
        seen = d.pop("seen")

        changed = d.pop("changed")

        rejected = d.pop("rejected")

        persisted = d.pop("persisted")

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

        _rejections = d.pop("rejections", UNSET)
        rejections: list[IngestOutcomeRejectionsItem] | Unset = UNSET
        if _rejections is not UNSET:
            rejections = []
            for rejections_item_data in _rejections:
                rejections_item = IngestOutcomeRejectionsItem.from_dict(
                    rejections_item_data
                )

                rejections.append(rejections_item)

        live = d.pop("live", UNSET)

        touched = d.pop("touched", UNSET)

        touches_throttled = d.pop("touchesThrottled", UNSET)

        late = d.pop("late", UNSET)

        skipped = d.pop("skipped", UNSET)

        _by_reason = d.pop("byReason", UNSET)
        by_reason: IngestOutcomeByReason | Unset
        if isinstance(_by_reason, Unset):
            by_reason = UNSET
        else:
            by_reason = IngestOutcomeByReason.from_dict(_by_reason)

        version_bytes = d.pop("versionBytes", UNSET)

        ingest_outcome = cls(
            seen=seen,
            changed=changed,
            rejected=rejected,
            persisted=persisted,
            from_seq=from_seq,
            to_seq=to_seq,
            rejections=rejections,
            live=live,
            touched=touched,
            touches_throttled=touches_throttled,
            late=late,
            skipped=skipped,
            by_reason=by_reason,
            version_bytes=version_bytes,
        )

        ingest_outcome.additional_properties = d
        return ingest_outcome

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
