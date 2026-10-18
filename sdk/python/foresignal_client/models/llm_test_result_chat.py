from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_test_result_chat_stop_reason import LlmTestResultChatStopReason
from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmTestResultChat")


@_attrs_define
class LlmTestResultChat:
    """Chat probe result

    Attributes:
        model (str | Unset):
        latency_ms (int | Unset):
        input_tokens (int | Unset):
        output_tokens (int | Unset):
        text (str | Unset):
        cached_tokens (int | Unset):
        stop_reason (LlmTestResultChatStopReason | Unset):
    """

    model: str | Unset = UNSET
    latency_ms: int | Unset = UNSET
    input_tokens: int | Unset = UNSET
    output_tokens: int | Unset = UNSET
    text: str | Unset = UNSET
    cached_tokens: int | Unset = UNSET
    stop_reason: LlmTestResultChatStopReason | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        model = self.model

        latency_ms = self.latency_ms

        input_tokens = self.input_tokens

        output_tokens = self.output_tokens

        text = self.text

        cached_tokens = self.cached_tokens

        stop_reason: str | Unset = UNSET
        if not isinstance(self.stop_reason, Unset):
            stop_reason = self.stop_reason.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if model is not UNSET:
            field_dict["model"] = model
        if latency_ms is not UNSET:
            field_dict["latencyMs"] = latency_ms
        if input_tokens is not UNSET:
            field_dict["inputTokens"] = input_tokens
        if output_tokens is not UNSET:
            field_dict["outputTokens"] = output_tokens
        if text is not UNSET:
            field_dict["text"] = text
        if cached_tokens is not UNSET:
            field_dict["cachedTokens"] = cached_tokens
        if stop_reason is not UNSET:
            field_dict["stopReason"] = stop_reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        model = d.pop("model", UNSET)

        latency_ms = d.pop("latencyMs", UNSET)

        input_tokens = d.pop("inputTokens", UNSET)

        output_tokens = d.pop("outputTokens", UNSET)

        text = d.pop("text", UNSET)

        cached_tokens = d.pop("cachedTokens", UNSET)

        _stop_reason = d.pop("stopReason", UNSET)
        stop_reason: LlmTestResultChatStopReason | Unset
        if isinstance(_stop_reason, Unset):
            stop_reason = UNSET
        else:
            stop_reason = LlmTestResultChatStopReason(_stop_reason)

        llm_test_result_chat = cls(
            model=model,
            latency_ms=latency_ms,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            text=text,
            cached_tokens=cached_tokens,
            stop_reason=stop_reason,
        )

        llm_test_result_chat.additional_properties = d
        return llm_test_result_chat

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
