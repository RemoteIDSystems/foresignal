from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.replay_result_outcome_type_1 import ReplayResultOutcomeType1
from ..models.replay_result_outcome_type_2_type_1 import ReplayResultOutcomeType2Type1
from ..models.replay_result_outcome_type_3_type_1 import ReplayResultOutcomeType3Type1
from ..models.replay_result_reason_type_1 import ReplayResultReasonType1
from ..models.replay_result_reason_type_2_type_1 import ReplayResultReasonType2Type1
from ..models.replay_result_reason_type_3_type_1 import ReplayResultReasonType3Type1
from ..models.replay_result_status import ReplayResultStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="ReplayResult")


@_attrs_define
class ReplayResult:
    """Replay of a manifest at its asOfSeq with the recorded configuration and inputs (116)

    Attributes:
        job_id (str):
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ReplayResultStatus):
        outcome (None | ReplayResultOutcomeType1 | ReplayResultOutcomeType2Type1 | ReplayResultOutcomeType3Type1 |
            Unset):
        reason (None | ReplayResultReasonType1 | ReplayResultReasonType2Type1 | ReplayResultReasonType3Type1 | Unset):
            When not-replayable
        diff_summary (None | str | Unset): When differs
        message (None | str | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
    """

    job_id: str
    manifest_id: str
    status: ReplayResultStatus
    outcome: (
        None
        | ReplayResultOutcomeType1
        | ReplayResultOutcomeType2Type1
        | ReplayResultOutcomeType3Type1
        | Unset
    ) = UNSET
    reason: (
        None
        | ReplayResultReasonType1
        | ReplayResultReasonType2Type1
        | ReplayResultReasonType3Type1
        | Unset
    ) = UNSET
    diff_summary: None | str | Unset = UNSET
    message: None | str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        job_id = self.job_id

        manifest_id = self.manifest_id

        status = self.status.value

        outcome: None | str | Unset
        if isinstance(self.outcome, Unset):
            outcome = UNSET
        elif (
            isinstance(self.outcome, ReplayResultOutcomeType1)
            or isinstance(self.outcome, ReplayResultOutcomeType2Type1)
            or isinstance(self.outcome, ReplayResultOutcomeType3Type1)
        ):
            outcome = self.outcome.value
        else:
            outcome = self.outcome

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        elif (
            isinstance(self.reason, ReplayResultReasonType1)
            or isinstance(self.reason, ReplayResultReasonType2Type1)
            or isinstance(self.reason, ReplayResultReasonType3Type1)
        ):
            reason = self.reason.value
        else:
            reason = self.reason

        diff_summary: None | str | Unset
        if isinstance(self.diff_summary, Unset):
            diff_summary = UNSET
        else:
            diff_summary = self.diff_summary

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "jobId": job_id,
                "manifestId": manifest_id,
                "status": status,
            }
        )
        if outcome is not UNSET:
            field_dict["outcome"] = outcome
        if reason is not UNSET:
            field_dict["reason"] = reason
        if diff_summary is not UNSET:
            field_dict["diffSummary"] = diff_summary
        if message is not UNSET:
            field_dict["message"] = message
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        job_id = d.pop("jobId")

        manifest_id = d.pop("manifestId")

        status = ReplayResultStatus(d.pop("status"))

        def _parse_outcome(
            data: object,
        ) -> (
            None
            | ReplayResultOutcomeType1
            | ReplayResultOutcomeType2Type1
            | ReplayResultOutcomeType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                outcome_type_1 = ReplayResultOutcomeType1(data)

                return outcome_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                outcome_type_2_type_1 = ReplayResultOutcomeType2Type1(data)

                return outcome_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                outcome_type_3_type_1 = ReplayResultOutcomeType3Type1(data)

                return outcome_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | ReplayResultOutcomeType1
                | ReplayResultOutcomeType2Type1
                | ReplayResultOutcomeType3Type1
                | Unset,
                data,
            )

        outcome = _parse_outcome(d.pop("outcome", UNSET))

        def _parse_reason(
            data: object,
        ) -> (
            None
            | ReplayResultReasonType1
            | ReplayResultReasonType2Type1
            | ReplayResultReasonType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reason_type_1 = ReplayResultReasonType1(data)

                return reason_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reason_type_2_type_1 = ReplayResultReasonType2Type1(data)

                return reason_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reason_type_3_type_1 = ReplayResultReasonType3Type1(data)

                return reason_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | ReplayResultReasonType1
                | ReplayResultReasonType2Type1
                | ReplayResultReasonType3Type1
                | Unset,
                data,
            )

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_diff_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        diff_summary = _parse_diff_summary(d.pop("diffSummary", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

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

        replay_result = cls(
            job_id=job_id,
            manifest_id=manifest_id,
            status=status,
            outcome=outcome,
            reason=reason,
            diff_summary=diff_summary,
            message=message,
            started_at=started_at,
            finished_at=finished_at,
        )

        replay_result.additional_properties = d
        return replay_result

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
