from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_report_llm_type_0_detail import DoctorReportLlmType0Detail
from ..models.doctor_report_llm_type_0_status import DoctorReportLlmType0Status
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.doctor_report_llm_type_0_tokens import DoctorReportLlmType0Tokens
    from ..models.proposed_patch import ProposedPatch


T = TypeVar("T", bound="DoctorReportLlmType0")


@_attrs_define
class DoctorReportLlmType0:
    """LLM explanation (optional; G11)

    Attributes:
        status (DoctorReportLlmType0Status):
        provider (None | str | Unset):
        model (None | str | Unset):
        tokens (DoctorReportLlmType0Tokens | Unset): Token usage
        summary (None | str | Unset):
        likely_cause (None | str | Unset):
        confidence (float | None | Unset):
        evidence (list[str] | Unset):
        suggested_actions (list[str] | Unset):
        patch (None | ProposedPatch | Unset):
        context_tokens (int | Unset): Estimated tokens of the context sent (4 characters a token)
        detail (DoctorReportLlmType0Detail | Unset):
        error (None | str | Unset): Why the LLM step gave no answer, when it did not
    """

    status: DoctorReportLlmType0Status
    provider: None | str | Unset = UNSET
    model: None | str | Unset = UNSET
    tokens: DoctorReportLlmType0Tokens | Unset = UNSET
    summary: None | str | Unset = UNSET
    likely_cause: None | str | Unset = UNSET
    confidence: float | None | Unset = UNSET
    evidence: list[str] | Unset = UNSET
    suggested_actions: list[str] | Unset = UNSET
    patch: None | ProposedPatch | Unset = UNSET
    context_tokens: int | Unset = UNSET
    detail: DoctorReportLlmType0Detail | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.proposed_patch import ProposedPatch

        status = self.status.value

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

        tokens: dict[str, Any] | Unset = UNSET
        if not isinstance(self.tokens, Unset):
            tokens = self.tokens.to_dict()

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

        likely_cause: None | str | Unset
        if isinstance(self.likely_cause, Unset):
            likely_cause = UNSET
        else:
            likely_cause = self.likely_cause

        confidence: float | None | Unset
        if isinstance(self.confidence, Unset):
            confidence = UNSET
        else:
            confidence = self.confidence

        evidence: list[str] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = self.evidence

        suggested_actions: list[str] | Unset = UNSET
        if not isinstance(self.suggested_actions, Unset):
            suggested_actions = self.suggested_actions

        patch: dict[str, Any] | None | Unset
        if isinstance(self.patch, Unset):
            patch = UNSET
        elif isinstance(self.patch, ProposedPatch):
            patch = self.patch.to_dict()
        else:
            patch = self.patch

        context_tokens = self.context_tokens

        detail: str | Unset = UNSET
        if not isinstance(self.detail, Unset):
            detail = self.detail.value

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if provider is not UNSET:
            field_dict["provider"] = provider
        if model is not UNSET:
            field_dict["model"] = model
        if tokens is not UNSET:
            field_dict["tokens"] = tokens
        if summary is not UNSET:
            field_dict["summary"] = summary
        if likely_cause is not UNSET:
            field_dict["likelyCause"] = likely_cause
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if suggested_actions is not UNSET:
            field_dict["suggestedActions"] = suggested_actions
        if patch is not UNSET:
            field_dict["patch"] = patch
        if context_tokens is not UNSET:
            field_dict["contextTokens"] = context_tokens
        if detail is not UNSET:
            field_dict["detail"] = detail
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.doctor_report_llm_type_0_tokens import DoctorReportLlmType0Tokens
        from ..models.proposed_patch import ProposedPatch

        d = dict(src_dict)
        status = DoctorReportLlmType0Status(d.pop("status"))

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

        _tokens = d.pop("tokens", UNSET)
        tokens: DoctorReportLlmType0Tokens | Unset
        if isinstance(_tokens, Unset):
            tokens = UNSET
        else:
            tokens = DoctorReportLlmType0Tokens.from_dict(_tokens)

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

        def _parse_likely_cause(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        likely_cause = _parse_likely_cause(d.pop("likelyCause", UNSET))

        def _parse_confidence(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        confidence = _parse_confidence(d.pop("confidence", UNSET))

        evidence = cast(list[str], d.pop("evidence", UNSET))

        suggested_actions = cast(list[str], d.pop("suggestedActions", UNSET))

        def _parse_patch(data: object) -> None | ProposedPatch | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                patch_type_0 = ProposedPatch.from_dict(data)

                return patch_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ProposedPatch | Unset, data)

        patch = _parse_patch(d.pop("patch", UNSET))

        context_tokens = d.pop("contextTokens", UNSET)

        _detail = d.pop("detail", UNSET)
        detail: DoctorReportLlmType0Detail | Unset
        if isinstance(_detail, Unset):
            detail = UNSET
        else:
            detail = DoctorReportLlmType0Detail(_detail)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        doctor_report_llm_type_0 = cls(
            status=status,
            provider=provider,
            model=model,
            tokens=tokens,
            summary=summary,
            likely_cause=likely_cause,
            confidence=confidence,
            evidence=evidence,
            suggested_actions=suggested_actions,
            patch=patch,
            context_tokens=context_tokens,
            detail=detail,
            error=error,
        )

        doctor_report_llm_type_0.additional_properties = d
        return doctor_report_llm_type_0

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
