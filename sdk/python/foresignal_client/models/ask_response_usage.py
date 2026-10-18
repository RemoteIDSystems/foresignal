from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AskResponseUsage")


@_attrs_define
class AskResponseUsage:
    """
    Attributes:
        provider (str | Unset):
        model (str | Unset):
        input_tokens (int | Unset):
        output_tokens (int | Unset):
        tool_calls (int | Unset):
        duration_ms (int | Unset):
    """

    provider: str | Unset = UNSET
    model: str | Unset = UNSET
    input_tokens: int | Unset = UNSET
    output_tokens: int | Unset = UNSET
    tool_calls: int | Unset = UNSET
    duration_ms: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider = self.provider

        model = self.model

        input_tokens = self.input_tokens

        output_tokens = self.output_tokens

        tool_calls = self.tool_calls

        duration_ms = self.duration_ms

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if provider is not UNSET:
            field_dict["provider"] = provider
        if model is not UNSET:
            field_dict["model"] = model
        if input_tokens is not UNSET:
            field_dict["inputTokens"] = input_tokens
        if output_tokens is not UNSET:
            field_dict["outputTokens"] = output_tokens
        if tool_calls is not UNSET:
            field_dict["toolCalls"] = tool_calls
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        provider = d.pop("provider", UNSET)

        model = d.pop("model", UNSET)

        input_tokens = d.pop("inputTokens", UNSET)

        output_tokens = d.pop("outputTokens", UNSET)

        tool_calls = d.pop("toolCalls", UNSET)

        duration_ms = d.pop("durationMs", UNSET)

        ask_response_usage = cls(
            provider=provider,
            model=model,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            tool_calls=tool_calls,
            duration_ms=duration_ms,
        )

        ask_response_usage.additional_properties = d
        return ask_response_usage

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
