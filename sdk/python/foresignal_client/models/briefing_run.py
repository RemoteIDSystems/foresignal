from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.briefing_run_mode import BriefingRunMode
from ..models.briefing_run_status import BriefingRunStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_citation import AskCitation
    from ..models.briefing_run_audio_type_0 import BriefingRunAudioType0
    from ..models.briefing_run_usage_type_0 import BriefingRunUsageType0
    from ..models.digest_stats import DigestStats


T = TypeVar("T", bound="BriefingRun")


@_attrs_define
class BriefingRun:
    """
    Attributes:
        status (BriefingRunStatus):
        started_at (datetime.datetime):
        period_from (datetime.datetime):
        period_to (datetime.datetime):
        content_markdown (str):
        id (None | Unset | UUID): null for previews
        briefing_id (None | Unset | UUID):
        finished_at (datetime.datetime | None | Unset):
        mode (BriefingRunMode | Unset):
        citations (list[AskCitation] | Unset):
        stats (DigestStats | Unset): Digest statistics shared by briefing runs and GET /v1/digest (029; counts served
            from 038 stats afterwards)
        error (None | str | Unset):
        usage (BriefingRunUsageType0 | None | Unset):
        deliveries (int | Unset):
        warnings (list[str] | Unset): Citation-verifier warnings (LLM mode) and fallback notes
        signature (None | str | Unset): Detached JWS (`header..signature`, Ed25519, b64:false) over the stored markdown
            bytes; verify against /.well-known/jwks.json
        audio (BriefingRunAudioType0 | None | Unset):
    """

    status: BriefingRunStatus
    started_at: datetime.datetime
    period_from: datetime.datetime
    period_to: datetime.datetime
    content_markdown: str
    id: None | Unset | UUID = UNSET
    briefing_id: None | Unset | UUID = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    mode: BriefingRunMode | Unset = UNSET
    citations: list[AskCitation] | Unset = UNSET
    stats: DigestStats | Unset = UNSET
    error: None | str | Unset = UNSET
    usage: BriefingRunUsageType0 | None | Unset = UNSET
    deliveries: int | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    signature: None | str | Unset = UNSET
    audio: BriefingRunAudioType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.briefing_run_audio_type_0 import BriefingRunAudioType0
        from ..models.briefing_run_usage_type_0 import BriefingRunUsageType0

        status = self.status.value

        started_at = self.started_at.isoformat()

        period_from = self.period_from.isoformat()

        period_to = self.period_to.isoformat()

        content_markdown = self.content_markdown

        id: None | str | Unset
        if isinstance(self.id, Unset):
            id = UNSET
        elif isinstance(self.id, UUID):
            id = str(self.id)
        else:
            id = self.id

        briefing_id: None | str | Unset
        if isinstance(self.briefing_id, Unset):
            briefing_id = UNSET
        elif isinstance(self.briefing_id, UUID):
            briefing_id = str(self.briefing_id)
        else:
            briefing_id = self.briefing_id

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        citations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = []
            for citations_item_data in self.citations:
                citations_item = citations_item_data.to_dict()
                citations.append(citations_item)

        stats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stats, Unset):
            stats = self.stats.to_dict()

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        usage: dict[str, Any] | None | Unset
        if isinstance(self.usage, Unset):
            usage = UNSET
        elif isinstance(self.usage, BriefingRunUsageType0):
            usage = self.usage.to_dict()
        else:
            usage = self.usage

        deliveries = self.deliveries

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        signature: None | str | Unset
        if isinstance(self.signature, Unset):
            signature = UNSET
        else:
            signature = self.signature

        audio: dict[str, Any] | None | Unset
        if isinstance(self.audio, Unset):
            audio = UNSET
        elif isinstance(self.audio, BriefingRunAudioType0):
            audio = self.audio.to_dict()
        else:
            audio = self.audio

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
                "startedAt": started_at,
                "periodFrom": period_from,
                "periodTo": period_to,
                "contentMarkdown": content_markdown,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if briefing_id is not UNSET:
            field_dict["briefingId"] = briefing_id
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if mode is not UNSET:
            field_dict["mode"] = mode
        if citations is not UNSET:
            field_dict["citations"] = citations
        if stats is not UNSET:
            field_dict["stats"] = stats
        if error is not UNSET:
            field_dict["error"] = error
        if usage is not UNSET:
            field_dict["usage"] = usage
        if deliveries is not UNSET:
            field_dict["deliveries"] = deliveries
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if signature is not UNSET:
            field_dict["signature"] = signature
        if audio is not UNSET:
            field_dict["audio"] = audio

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_citation import AskCitation
        from ..models.briefing_run_audio_type_0 import BriefingRunAudioType0
        from ..models.briefing_run_usage_type_0 import BriefingRunUsageType0
        from ..models.digest_stats import DigestStats

        d = dict(src_dict)
        status = BriefingRunStatus(d.pop("status"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        period_from = datetime.datetime.fromisoformat(d.pop("periodFrom"))

        period_to = datetime.datetime.fromisoformat(d.pop("periodTo"))

        content_markdown = d.pop("contentMarkdown")

        def _parse_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                id_type_0 = UUID(data)

                return id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        id = _parse_id(d.pop("id", UNSET))

        def _parse_briefing_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                briefing_id_type_0 = UUID(data)

                return briefing_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        briefing_id = _parse_briefing_id(d.pop("briefingId", UNSET))

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

        _mode = d.pop("mode", UNSET)
        mode: BriefingRunMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = BriefingRunMode(_mode)

        _citations = d.pop("citations", UNSET)
        citations: list[AskCitation] | Unset = UNSET
        if _citations is not UNSET:
            citations = []
            for citations_item_data in _citations:
                citations_item = AskCitation.from_dict(citations_item_data)

                citations.append(citations_item)

        _stats = d.pop("stats", UNSET)
        stats: DigestStats | Unset
        if isinstance(_stats, Unset):
            stats = UNSET
        else:
            stats = DigestStats.from_dict(_stats)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_usage(data: object) -> BriefingRunUsageType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                usage_type_0 = BriefingRunUsageType0.from_dict(data)

                return usage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(BriefingRunUsageType0 | None | Unset, data)

        usage = _parse_usage(d.pop("usage", UNSET))

        deliveries = d.pop("deliveries", UNSET)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        def _parse_signature(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        signature = _parse_signature(d.pop("signature", UNSET))

        def _parse_audio(data: object) -> BriefingRunAudioType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                audio_type_0 = BriefingRunAudioType0.from_dict(data)

                return audio_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(BriefingRunAudioType0 | None | Unset, data)

        audio = _parse_audio(d.pop("audio", UNSET))

        briefing_run = cls(
            status=status,
            started_at=started_at,
            period_from=period_from,
            period_to=period_to,
            content_markdown=content_markdown,
            id=id,
            briefing_id=briefing_id,
            finished_at=finished_at,
            mode=mode,
            citations=citations,
            stats=stats,
            error=error,
            usage=usage,
            deliveries=deliveries,
            warnings=warnings,
            signature=signature,
            audio=audio,
        )

        briefing_run.additional_properties = d
        return briefing_run

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
