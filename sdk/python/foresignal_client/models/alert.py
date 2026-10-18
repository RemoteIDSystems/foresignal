from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.alert_feedback_type_1 import AlertFeedbackType1
from ..models.alert_feedback_type_2_type_1 import AlertFeedbackType2Type1
from ..models.alert_feedback_type_3_type_1 import AlertFeedbackType3Type1
from ..models.alert_severity import AlertSeverity
from ..models.alert_state import AlertState
from ..models.alert_suppressed_reason_type_1 import AlertSuppressedReasonType1
from ..models.alert_suppressed_reason_type_2_type_1 import (
    AlertSuppressedReasonType2Type1,
)
from ..models.alert_suppressed_reason_type_3_type_1 import (
    AlertSuppressedReasonType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alert_context import AlertContext
    from ..models.alert_record_snapshot import AlertRecordSnapshot
    from ..models.alert_signal import AlertSignal
    from ..models.ask_citation import AskCitation
    from ..models.record import Record
    from ..models.region_tags import RegionTags


T = TypeVar("T", bound="Alert")


@_attrs_define
class Alert:
    """
    Attributes:
        id (int):
        rule_id (UUID):
        seq (int):
        source (str):
        external_id (str):
        fired_at (datetime.datetime):
        message (str):
        severity (AlertSeverity):
        rev (int): Monotonic revision (SSE id on /v1/alerts/stream); bumps on every change
        state (AlertState):
        rule_name (None | str | Unset): The rule's current name; null when the rule was deleted (its alerts are kept)
        kind (str | Unset):
        acknowledged_at (datetime.datetime | None | Unset):
        summary (None | str | Unset): LLM context summary when the rule has enrich=true (030): at most about 120 words
            with [source:externalId@seq] and [alert:id] citations (unverifiable ones removed); null until enriched, and when
            enrichment was skipped or failed
        enriched_at (datetime.datetime | None | Unset): When the summary was stored (030); null until enriched
        record (Record | Unset):
        observed_at (datetime.datetime | Unset): observedAt of the matching version (cooldown axis)
        acknowledged_by (None | str | Unset): Token name
        resolved_at (datetime.datetime | None | Unset):
        resolved_by (None | str | Unset): inverse_edge, tombstone, expiry, stale, admin or token:<name>
        resolved_seq (int | None | Unset):
        feedback (AlertFeedbackType1 | AlertFeedbackType2Type1 | AlertFeedbackType3Type1 | None | Unset):
        feedback_at (datetime.datetime | None | Unset):
        note (None | str | Unset):
        snoozed_until (datetime.datetime | None | Unset):
        suppressed (bool | Unset):  Default: False.
        suppressed_reason (AlertSuppressedReasonType1 | AlertSuppressedReasonType2Type1 |
            AlertSuppressedReasonType3Type1 | None | Unset):
        tags (list[str] | Unset):
        record_snapshot (AlertRecordSnapshot | None | Unset):
        summary_citations (list[AskCitation] | Unset): Verified citations of the enrichment summary (030); empty until
            enriched
        sensitive (bool | Unset): Alert from a rule referencing a watchlist (036): hidden from anonymous and feed-key
            principals
        region (None | RegionTags | Unset): Region tags snapshotted at fire time (040)
        context (AlertContext | None | Unset):
        exercise (bool | Unset): Exercise replay alert (never counted in real state or statistics) Default: False.
        exercise_run_id (None | str | Unset):
        signals (list[AlertSignal] | Unset): Implicit quality signals recorded for this alert
    """

    id: int
    rule_id: UUID
    seq: int
    source: str
    external_id: str
    fired_at: datetime.datetime
    message: str
    severity: AlertSeverity
    rev: int
    state: AlertState
    rule_name: None | str | Unset = UNSET
    kind: str | Unset = UNSET
    acknowledged_at: datetime.datetime | None | Unset = UNSET
    summary: None | str | Unset = UNSET
    enriched_at: datetime.datetime | None | Unset = UNSET
    record: Record | Unset = UNSET
    observed_at: datetime.datetime | Unset = UNSET
    acknowledged_by: None | str | Unset = UNSET
    resolved_at: datetime.datetime | None | Unset = UNSET
    resolved_by: None | str | Unset = UNSET
    resolved_seq: int | None | Unset = UNSET
    feedback: (
        AlertFeedbackType1
        | AlertFeedbackType2Type1
        | AlertFeedbackType3Type1
        | None
        | Unset
    ) = UNSET
    feedback_at: datetime.datetime | None | Unset = UNSET
    note: None | str | Unset = UNSET
    snoozed_until: datetime.datetime | None | Unset = UNSET
    suppressed: bool | Unset = False
    suppressed_reason: (
        AlertSuppressedReasonType1
        | AlertSuppressedReasonType2Type1
        | AlertSuppressedReasonType3Type1
        | None
        | Unset
    ) = UNSET
    tags: list[str] | Unset = UNSET
    record_snapshot: AlertRecordSnapshot | None | Unset = UNSET
    summary_citations: list[AskCitation] | Unset = UNSET
    sensitive: bool | Unset = UNSET
    region: None | RegionTags | Unset = UNSET
    context: AlertContext | None | Unset = UNSET
    exercise: bool | Unset = False
    exercise_run_id: None | str | Unset = UNSET
    signals: list[AlertSignal] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.alert_context import AlertContext
        from ..models.alert_record_snapshot import AlertRecordSnapshot
        from ..models.region_tags import RegionTags

        id = self.id

        rule_id = str(self.rule_id)

        seq = self.seq

        source = self.source

        external_id = self.external_id

        fired_at = self.fired_at.isoformat()

        message = self.message

        severity = self.severity.value

        rev = self.rev

        state = self.state.value

        rule_name: None | str | Unset
        if isinstance(self.rule_name, Unset):
            rule_name = UNSET
        else:
            rule_name = self.rule_name

        kind = self.kind

        acknowledged_at: None | str | Unset
        if isinstance(self.acknowledged_at, Unset):
            acknowledged_at = UNSET
        elif isinstance(self.acknowledged_at, datetime.datetime):
            acknowledged_at = self.acknowledged_at.isoformat()
        else:
            acknowledged_at = self.acknowledged_at

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

        enriched_at: None | str | Unset
        if isinstance(self.enriched_at, Unset):
            enriched_at = UNSET
        elif isinstance(self.enriched_at, datetime.datetime):
            enriched_at = self.enriched_at.isoformat()
        else:
            enriched_at = self.enriched_at

        record: dict[str, Any] | Unset = UNSET
        if not isinstance(self.record, Unset):
            record = self.record.to_dict()

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        acknowledged_by: None | str | Unset
        if isinstance(self.acknowledged_by, Unset):
            acknowledged_by = UNSET
        else:
            acknowledged_by = self.acknowledged_by

        resolved_at: None | str | Unset
        if isinstance(self.resolved_at, Unset):
            resolved_at = UNSET
        elif isinstance(self.resolved_at, datetime.datetime):
            resolved_at = self.resolved_at.isoformat()
        else:
            resolved_at = self.resolved_at

        resolved_by: None | str | Unset
        if isinstance(self.resolved_by, Unset):
            resolved_by = UNSET
        else:
            resolved_by = self.resolved_by

        resolved_seq: int | None | Unset
        if isinstance(self.resolved_seq, Unset):
            resolved_seq = UNSET
        else:
            resolved_seq = self.resolved_seq

        feedback: None | str | Unset
        if isinstance(self.feedback, Unset):
            feedback = UNSET
        elif (
            isinstance(self.feedback, AlertFeedbackType1)
            or isinstance(self.feedback, AlertFeedbackType2Type1)
            or isinstance(self.feedback, AlertFeedbackType3Type1)
        ):
            feedback = self.feedback.value
        else:
            feedback = self.feedback

        feedback_at: None | str | Unset
        if isinstance(self.feedback_at, Unset):
            feedback_at = UNSET
        elif isinstance(self.feedback_at, datetime.datetime):
            feedback_at = self.feedback_at.isoformat()
        else:
            feedback_at = self.feedback_at

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        snoozed_until: None | str | Unset
        if isinstance(self.snoozed_until, Unset):
            snoozed_until = UNSET
        elif isinstance(self.snoozed_until, datetime.datetime):
            snoozed_until = self.snoozed_until.isoformat()
        else:
            snoozed_until = self.snoozed_until

        suppressed = self.suppressed

        suppressed_reason: None | str | Unset
        if isinstance(self.suppressed_reason, Unset):
            suppressed_reason = UNSET
        elif (
            isinstance(self.suppressed_reason, AlertSuppressedReasonType1)
            or isinstance(self.suppressed_reason, AlertSuppressedReasonType2Type1)
            or isinstance(self.suppressed_reason, AlertSuppressedReasonType3Type1)
        ):
            suppressed_reason = self.suppressed_reason.value
        else:
            suppressed_reason = self.suppressed_reason

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        record_snapshot: dict[str, Any] | None | Unset
        if isinstance(self.record_snapshot, Unset):
            record_snapshot = UNSET
        elif isinstance(self.record_snapshot, AlertRecordSnapshot):
            record_snapshot = self.record_snapshot.to_dict()
        else:
            record_snapshot = self.record_snapshot

        summary_citations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.summary_citations, Unset):
            summary_citations = []
            for summary_citations_item_data in self.summary_citations:
                summary_citations_item = summary_citations_item_data.to_dict()
                summary_citations.append(summary_citations_item)

        sensitive = self.sensitive

        region: dict[str, Any] | None | Unset
        if isinstance(self.region, Unset):
            region = UNSET
        elif isinstance(self.region, RegionTags):
            region = self.region.to_dict()
        else:
            region = self.region

        context: dict[str, Any] | None | Unset
        if isinstance(self.context, Unset):
            context = UNSET
        elif isinstance(self.context, AlertContext):
            context = self.context.to_dict()
        else:
            context = self.context

        exercise = self.exercise

        exercise_run_id: None | str | Unset
        if isinstance(self.exercise_run_id, Unset):
            exercise_run_id = UNSET
        else:
            exercise_run_id = self.exercise_run_id

        signals: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.signals, Unset):
            signals = []
            for signals_item_data in self.signals:
                signals_item = signals_item_data.to_dict()
                signals.append(signals_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ruleId": rule_id,
                "seq": seq,
                "source": source,
                "externalId": external_id,
                "firedAt": fired_at,
                "message": message,
                "severity": severity,
                "rev": rev,
                "state": state,
            }
        )
        if rule_name is not UNSET:
            field_dict["ruleName"] = rule_name
        if kind is not UNSET:
            field_dict["kind"] = kind
        if acknowledged_at is not UNSET:
            field_dict["acknowledgedAt"] = acknowledged_at
        if summary is not UNSET:
            field_dict["summary"] = summary
        if enriched_at is not UNSET:
            field_dict["enrichedAt"] = enriched_at
        if record is not UNSET:
            field_dict["record"] = record
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if acknowledged_by is not UNSET:
            field_dict["acknowledgedBy"] = acknowledged_by
        if resolved_at is not UNSET:
            field_dict["resolvedAt"] = resolved_at
        if resolved_by is not UNSET:
            field_dict["resolvedBy"] = resolved_by
        if resolved_seq is not UNSET:
            field_dict["resolvedSeq"] = resolved_seq
        if feedback is not UNSET:
            field_dict["feedback"] = feedback
        if feedback_at is not UNSET:
            field_dict["feedbackAt"] = feedback_at
        if note is not UNSET:
            field_dict["note"] = note
        if snoozed_until is not UNSET:
            field_dict["snoozedUntil"] = snoozed_until
        if suppressed is not UNSET:
            field_dict["suppressed"] = suppressed
        if suppressed_reason is not UNSET:
            field_dict["suppressedReason"] = suppressed_reason
        if tags is not UNSET:
            field_dict["tags"] = tags
        if record_snapshot is not UNSET:
            field_dict["recordSnapshot"] = record_snapshot
        if summary_citations is not UNSET:
            field_dict["summaryCitations"] = summary_citations
        if sensitive is not UNSET:
            field_dict["sensitive"] = sensitive
        if region is not UNSET:
            field_dict["region"] = region
        if context is not UNSET:
            field_dict["context"] = context
        if exercise is not UNSET:
            field_dict["exercise"] = exercise
        if exercise_run_id is not UNSET:
            field_dict["exerciseRunId"] = exercise_run_id
        if signals is not UNSET:
            field_dict["signals"] = signals

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alert_context import AlertContext
        from ..models.alert_record_snapshot import AlertRecordSnapshot
        from ..models.alert_signal import AlertSignal
        from ..models.ask_citation import AskCitation
        from ..models.record import Record
        from ..models.region_tags import RegionTags

        d = dict(src_dict)
        id = d.pop("id")

        rule_id = UUID(d.pop("ruleId"))

        seq = d.pop("seq")

        source = d.pop("source")

        external_id = d.pop("externalId")

        fired_at = datetime.datetime.fromisoformat(d.pop("firedAt"))

        message = d.pop("message")

        severity = AlertSeverity(d.pop("severity"))

        rev = d.pop("rev")

        state = AlertState(d.pop("state"))

        def _parse_rule_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rule_name = _parse_rule_name(d.pop("ruleName", UNSET))

        kind = d.pop("kind", UNSET)

        def _parse_acknowledged_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                acknowledged_at_type_0 = datetime.datetime.fromisoformat(data)

                return acknowledged_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        acknowledged_at = _parse_acknowledged_at(d.pop("acknowledgedAt", UNSET))

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

        def _parse_enriched_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                enriched_at_type_0 = datetime.datetime.fromisoformat(data)

                return enriched_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        enriched_at = _parse_enriched_at(d.pop("enrichedAt", UNSET))

        _record = d.pop("record", UNSET)
        record: Record | Unset
        if isinstance(_record, Unset):
            record = UNSET
        else:
            record = Record.from_dict(_record)

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        def _parse_acknowledged_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        acknowledged_by = _parse_acknowledged_by(d.pop("acknowledgedBy", UNSET))

        def _parse_resolved_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                resolved_at_type_0 = datetime.datetime.fromisoformat(data)

                return resolved_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        resolved_at = _parse_resolved_at(d.pop("resolvedAt", UNSET))

        def _parse_resolved_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resolved_by = _parse_resolved_by(d.pop("resolvedBy", UNSET))

        def _parse_resolved_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        resolved_seq = _parse_resolved_seq(d.pop("resolvedSeq", UNSET))

        def _parse_feedback(
            data: object,
        ) -> (
            AlertFeedbackType1
            | AlertFeedbackType2Type1
            | AlertFeedbackType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_type_1 = AlertFeedbackType1(data)

                return feedback_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_type_2_type_1 = AlertFeedbackType2Type1(data)

                return feedback_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_type_3_type_1 = AlertFeedbackType3Type1(data)

                return feedback_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AlertFeedbackType1
                | AlertFeedbackType2Type1
                | AlertFeedbackType3Type1
                | None
                | Unset,
                data,
            )

        feedback = _parse_feedback(d.pop("feedback", UNSET))

        def _parse_feedback_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_at_type_0 = datetime.datetime.fromisoformat(data)

                return feedback_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        feedback_at = _parse_feedback_at(d.pop("feedbackAt", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        def _parse_snoozed_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                snoozed_until_type_0 = datetime.datetime.fromisoformat(data)

                return snoozed_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        snoozed_until = _parse_snoozed_until(d.pop("snoozedUntil", UNSET))

        suppressed = d.pop("suppressed", UNSET)

        def _parse_suppressed_reason(
            data: object,
        ) -> (
            AlertSuppressedReasonType1
            | AlertSuppressedReasonType2Type1
            | AlertSuppressedReasonType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suppressed_reason_type_1 = AlertSuppressedReasonType1(data)

                return suppressed_reason_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suppressed_reason_type_2_type_1 = AlertSuppressedReasonType2Type1(data)

                return suppressed_reason_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suppressed_reason_type_3_type_1 = AlertSuppressedReasonType3Type1(data)

                return suppressed_reason_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AlertSuppressedReasonType1
                | AlertSuppressedReasonType2Type1
                | AlertSuppressedReasonType3Type1
                | None
                | Unset,
                data,
            )

        suppressed_reason = _parse_suppressed_reason(d.pop("suppressedReason", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        def _parse_record_snapshot(data: object) -> AlertRecordSnapshot | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                record_snapshot_type_0 = AlertRecordSnapshot.from_dict(data)

                return record_snapshot_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AlertRecordSnapshot | None | Unset, data)

        record_snapshot = _parse_record_snapshot(d.pop("recordSnapshot", UNSET))

        _summary_citations = d.pop("summaryCitations", UNSET)
        summary_citations: list[AskCitation] | Unset = UNSET
        if _summary_citations is not UNSET:
            summary_citations = []
            for summary_citations_item_data in _summary_citations:
                summary_citations_item = AskCitation.from_dict(
                    summary_citations_item_data
                )

                summary_citations.append(summary_citations_item)

        sensitive = d.pop("sensitive", UNSET)

        def _parse_region(data: object) -> None | RegionTags | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                region_type_0 = RegionTags.from_dict(data)

                return region_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RegionTags | Unset, data)

        region = _parse_region(d.pop("region", UNSET))

        def _parse_context(data: object) -> AlertContext | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                context_type_0 = AlertContext.from_dict(data)

                return context_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AlertContext | None | Unset, data)

        context = _parse_context(d.pop("context", UNSET))

        exercise = d.pop("exercise", UNSET)

        def _parse_exercise_run_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        exercise_run_id = _parse_exercise_run_id(d.pop("exerciseRunId", UNSET))

        _signals = d.pop("signals", UNSET)
        signals: list[AlertSignal] | Unset = UNSET
        if _signals is not UNSET:
            signals = []
            for signals_item_data in _signals:
                signals_item = AlertSignal.from_dict(signals_item_data)

                signals.append(signals_item)

        alert = cls(
            id=id,
            rule_id=rule_id,
            seq=seq,
            source=source,
            external_id=external_id,
            fired_at=fired_at,
            message=message,
            severity=severity,
            rev=rev,
            state=state,
            rule_name=rule_name,
            kind=kind,
            acknowledged_at=acknowledged_at,
            summary=summary,
            enriched_at=enriched_at,
            record=record,
            observed_at=observed_at,
            acknowledged_by=acknowledged_by,
            resolved_at=resolved_at,
            resolved_by=resolved_by,
            resolved_seq=resolved_seq,
            feedback=feedback,
            feedback_at=feedback_at,
            note=note,
            snoozed_until=snoozed_until,
            suppressed=suppressed,
            suppressed_reason=suppressed_reason,
            tags=tags,
            record_snapshot=record_snapshot,
            summary_citations=summary_citations,
            sensitive=sensitive,
            region=region,
            context=context,
            exercise=exercise,
            exercise_run_id=exercise_run_id,
            signals=signals,
        )

        alert.additional_properties = d
        return alert

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
