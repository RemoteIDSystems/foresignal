from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_run_effort_type_1 import AnalystRunEffortType1
from ..models.analyst_run_effort_type_2_type_1 import AnalystRunEffortType2Type1
from ..models.analyst_run_effort_type_3_type_1 import AnalystRunEffortType3Type1
from ..models.analyst_run_status import AnalystRunStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analyst_cursor import AnalystCursor
    from ..models.analyst_run_content_type_0 import AnalystRunContentType0
    from ..models.analyst_run_input_counts import AnalystRunInputCounts


T = TypeVar("T", bound="AnalystRun")


@_attrs_define
class AnalystRun:
    """One analyst run with budgets used, outcomes and validation counts (the evaluation log)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        profile_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (AnalystRunStatus): queued/running until finished; `noop` made no LLM call
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        cursor_from (AnalystCursor | Unset): Change-detection cursor of a profile
        cursor_to (AnalystCursor | Unset): Change-detection cursor of a profile
        input_counts (AnalystRunInputCounts | Unset): Inputs by type
        provider (None | str | Unset):
        model (None | str | Unset):
        effort (AnalystRunEffortType1 | AnalystRunEffortType2Type1 | AnalystRunEffortType3Type1 | None | Unset):
        tokens_in (int | Unset):
        tokens_out (int | Unset):
        cached_tokens (int | Unset):
        tool_calls (int | Unset):
        duration_ms (int | Unset):
        notes (int | Unset):
        proposals (int | Unset):
        auto_executed (int | Unset):
        citations_dropped (int | Unset):
        claims_downgraded (int | Unset):
        warnings (list[str] | Unset):
        reason (None | str | Unset): Failure, refusal or budget reason
        prompt_hash (None | str | Unset): sha256 of system prompt + tools + instructions
        detector_version (str | Unset):  Example: analyst-1.0.0.
        content (AnalystRunContentType0 | None | Unset): Prompt and response content, stored only when
            llm.logContent=true
        manifest_id (None | str | Unset): The analysis manifest of this output (prompt 116); null for outputs written
            before it
    """

    id: str
    profile_id: str
    status: AnalystRunStatus
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    cursor_from: AnalystCursor | Unset = UNSET
    cursor_to: AnalystCursor | Unset = UNSET
    input_counts: AnalystRunInputCounts | Unset = UNSET
    provider: None | str | Unset = UNSET
    model: None | str | Unset = UNSET
    effort: (
        AnalystRunEffortType1
        | AnalystRunEffortType2Type1
        | AnalystRunEffortType3Type1
        | None
        | Unset
    ) = UNSET
    tokens_in: int | Unset = UNSET
    tokens_out: int | Unset = UNSET
    cached_tokens: int | Unset = UNSET
    tool_calls: int | Unset = UNSET
    duration_ms: int | Unset = UNSET
    notes: int | Unset = UNSET
    proposals: int | Unset = UNSET
    auto_executed: int | Unset = UNSET
    citations_dropped: int | Unset = UNSET
    claims_downgraded: int | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    reason: None | str | Unset = UNSET
    prompt_hash: None | str | Unset = UNSET
    detector_version: str | Unset = UNSET
    content: AnalystRunContentType0 | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.analyst_run_content_type_0 import AnalystRunContentType0

        id = self.id

        profile_id = self.profile_id

        status = self.status.value

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

        cursor_from: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cursor_from, Unset):
            cursor_from = self.cursor_from.to_dict()

        cursor_to: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cursor_to, Unset):
            cursor_to = self.cursor_to.to_dict()

        input_counts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.input_counts, Unset):
            input_counts = self.input_counts.to_dict()

        provider: None | str | Unset
        if isinstance(self.provider, Unset):
            provider = UNSET
        else:
            provider = self.provider

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        effort: None | str | Unset
        if isinstance(self.effort, Unset):
            effort = UNSET
        elif (
            isinstance(self.effort, AnalystRunEffortType1)
            or isinstance(self.effort, AnalystRunEffortType2Type1)
            or isinstance(self.effort, AnalystRunEffortType3Type1)
        ):
            effort = self.effort.value
        else:
            effort = self.effort

        tokens_in = self.tokens_in

        tokens_out = self.tokens_out

        cached_tokens = self.cached_tokens

        tool_calls = self.tool_calls

        duration_ms = self.duration_ms

        notes = self.notes

        proposals = self.proposals

        auto_executed = self.auto_executed

        citations_dropped = self.citations_dropped

        claims_downgraded = self.claims_downgraded

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        prompt_hash: None | str | Unset
        if isinstance(self.prompt_hash, Unset):
            prompt_hash = UNSET
        else:
            prompt_hash = self.prompt_hash

        detector_version = self.detector_version

        content: dict[str, Any] | None | Unset
        if isinstance(self.content, Unset):
            content = UNSET
        elif isinstance(self.content, AnalystRunContentType0):
            content = self.content.to_dict()
        else:
            content = self.content

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "profileId": profile_id,
                "status": status,
            }
        )
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if cursor_from is not UNSET:
            field_dict["cursorFrom"] = cursor_from
        if cursor_to is not UNSET:
            field_dict["cursorTo"] = cursor_to
        if input_counts is not UNSET:
            field_dict["inputCounts"] = input_counts
        if provider is not UNSET:
            field_dict["provider"] = provider
        if model is not UNSET:
            field_dict["model"] = model
        if effort is not UNSET:
            field_dict["effort"] = effort
        if tokens_in is not UNSET:
            field_dict["tokensIn"] = tokens_in
        if tokens_out is not UNSET:
            field_dict["tokensOut"] = tokens_out
        if cached_tokens is not UNSET:
            field_dict["cachedTokens"] = cached_tokens
        if tool_calls is not UNSET:
            field_dict["toolCalls"] = tool_calls
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms
        if notes is not UNSET:
            field_dict["notes"] = notes
        if proposals is not UNSET:
            field_dict["proposals"] = proposals
        if auto_executed is not UNSET:
            field_dict["autoExecuted"] = auto_executed
        if citations_dropped is not UNSET:
            field_dict["citationsDropped"] = citations_dropped
        if claims_downgraded is not UNSET:
            field_dict["claimsDowngraded"] = claims_downgraded
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if reason is not UNSET:
            field_dict["reason"] = reason
        if prompt_hash is not UNSET:
            field_dict["promptHash"] = prompt_hash
        if detector_version is not UNSET:
            field_dict["detectorVersion"] = detector_version
        if content is not UNSET:
            field_dict["content"] = content
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_cursor import AnalystCursor
        from ..models.analyst_run_content_type_0 import AnalystRunContentType0
        from ..models.analyst_run_input_counts import AnalystRunInputCounts

        d = dict(src_dict)
        id = d.pop("id")

        profile_id = d.pop("profileId")

        status = AnalystRunStatus(d.pop("status"))

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

        _cursor_from = d.pop("cursorFrom", UNSET)
        cursor_from: AnalystCursor | Unset
        if isinstance(_cursor_from, Unset):
            cursor_from = UNSET
        else:
            cursor_from = AnalystCursor.from_dict(_cursor_from)

        _cursor_to = d.pop("cursorTo", UNSET)
        cursor_to: AnalystCursor | Unset
        if isinstance(_cursor_to, Unset):
            cursor_to = UNSET
        else:
            cursor_to = AnalystCursor.from_dict(_cursor_to)

        _input_counts = d.pop("inputCounts", UNSET)
        input_counts: AnalystRunInputCounts | Unset
        if isinstance(_input_counts, Unset):
            input_counts = UNSET
        else:
            input_counts = AnalystRunInputCounts.from_dict(_input_counts)

        def _parse_provider(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        provider = _parse_provider(d.pop("provider", UNSET))

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        def _parse_effort(
            data: object,
        ) -> (
            AnalystRunEffortType1
            | AnalystRunEffortType2Type1
            | AnalystRunEffortType3Type1
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
                effort_type_1 = AnalystRunEffortType1(data)

                return effort_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                effort_type_2_type_1 = AnalystRunEffortType2Type1(data)

                return effort_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                effort_type_3_type_1 = AnalystRunEffortType3Type1(data)

                return effort_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AnalystRunEffortType1
                | AnalystRunEffortType2Type1
                | AnalystRunEffortType3Type1
                | None
                | Unset,
                data,
            )

        effort = _parse_effort(d.pop("effort", UNSET))

        tokens_in = d.pop("tokensIn", UNSET)

        tokens_out = d.pop("tokensOut", UNSET)

        cached_tokens = d.pop("cachedTokens", UNSET)

        tool_calls = d.pop("toolCalls", UNSET)

        duration_ms = d.pop("durationMs", UNSET)

        notes = d.pop("notes", UNSET)

        proposals = d.pop("proposals", UNSET)

        auto_executed = d.pop("autoExecuted", UNSET)

        citations_dropped = d.pop("citationsDropped", UNSET)

        claims_downgraded = d.pop("claimsDowngraded", UNSET)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_prompt_hash(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        prompt_hash = _parse_prompt_hash(d.pop("promptHash", UNSET))

        detector_version = d.pop("detectorVersion", UNSET)

        def _parse_content(data: object) -> AnalystRunContentType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                content_type_0 = AnalystRunContentType0.from_dict(data)

                return content_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalystRunContentType0 | None | Unset, data)

        content = _parse_content(d.pop("content", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        analyst_run = cls(
            id=id,
            profile_id=profile_id,
            status=status,
            started_at=started_at,
            finished_at=finished_at,
            cursor_from=cursor_from,
            cursor_to=cursor_to,
            input_counts=input_counts,
            provider=provider,
            model=model,
            effort=effort,
            tokens_in=tokens_in,
            tokens_out=tokens_out,
            cached_tokens=cached_tokens,
            tool_calls=tool_calls,
            duration_ms=duration_ms,
            notes=notes,
            proposals=proposals,
            auto_executed=auto_executed,
            citations_dropped=citations_dropped,
            claims_downgraded=claims_downgraded,
            warnings=warnings,
            reason=reason,
            prompt_hash=prompt_hash,
            detector_version=detector_version,
            content=content,
            manifest_id=manifest_id,
        )

        analyst_run.additional_properties = d
        return analyst_run

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
