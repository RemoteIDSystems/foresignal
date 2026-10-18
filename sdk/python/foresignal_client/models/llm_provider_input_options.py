from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_provider_input_options_effort import LlmProviderInputOptionsEffort
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_provider_input_options_extra_headers import (
        LlmProviderInputOptionsExtraHeaders,
    )


T = TypeVar("T", bound="LlmProviderInputOptions")


@_attrs_define
class LlmProviderInputOptions:
    """
    Attributes:
        effort (LlmProviderInputOptionsEffort | Unset):
        max_tokens (int | Unset):
        temperature (float | None | Unset): Sampling temperature 0–2 for openai-compatible and ollama providers; refused
            for anthropic (current Claude models control depth with effort)
        extra_headers (LlmProviderInputOptionsExtraHeaders | Unset): Extra request headers (for example OpenRouter
            `HTTP-Referer`); Authorization, x-api-key, Content-Type and the anthropic-* headers are reserved
        local (bool | Unset): Treat this provider as local for LLM egress (private-source records may be sent to it,
            026/039); `ollama` is always local Default: False.
        tts_model (str | Unset): Speech model for the `tts` role (141), sent as `model` to `{baseUrl}/audio/speech`;
            openai-compatible only (for example `tts-1`, `gpt-4o-mini-tts`, `kokoro`) Default: 'tts-1'.
        tts_characters_per_day (int | Unset): Daily character budget of the `tts` role (141), per UTC day Default:
            100000.
    """

    effort: LlmProviderInputOptionsEffort | Unset = UNSET
    max_tokens: int | Unset = UNSET
    temperature: float | None | Unset = UNSET
    extra_headers: LlmProviderInputOptionsExtraHeaders | Unset = UNSET
    local: bool | Unset = False
    tts_model: str | Unset = "tts-1"
    tts_characters_per_day: int | Unset = 100000
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        effort: str | Unset = UNSET
        if not isinstance(self.effort, Unset):
            effort = self.effort.value

        max_tokens = self.max_tokens

        temperature: float | None | Unset
        if isinstance(self.temperature, Unset):
            temperature = UNSET
        else:
            temperature = self.temperature

        extra_headers: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extra_headers, Unset):
            extra_headers = self.extra_headers.to_dict()

        local = self.local

        tts_model = self.tts_model

        tts_characters_per_day = self.tts_characters_per_day

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if effort is not UNSET:
            field_dict["effort"] = effort
        if max_tokens is not UNSET:
            field_dict["maxTokens"] = max_tokens
        if temperature is not UNSET:
            field_dict["temperature"] = temperature
        if extra_headers is not UNSET:
            field_dict["extraHeaders"] = extra_headers
        if local is not UNSET:
            field_dict["local"] = local
        if tts_model is not UNSET:
            field_dict["ttsModel"] = tts_model
        if tts_characters_per_day is not UNSET:
            field_dict["ttsCharactersPerDay"] = tts_characters_per_day

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_provider_input_options_extra_headers import (
            LlmProviderInputOptionsExtraHeaders,
        )

        d = dict(src_dict)
        _effort = d.pop("effort", UNSET)
        effort: LlmProviderInputOptionsEffort | Unset
        if isinstance(_effort, Unset):
            effort = UNSET
        else:
            effort = LlmProviderInputOptionsEffort(_effort)

        max_tokens = d.pop("maxTokens", UNSET)

        def _parse_temperature(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        temperature = _parse_temperature(d.pop("temperature", UNSET))

        _extra_headers = d.pop("extraHeaders", UNSET)
        extra_headers: LlmProviderInputOptionsExtraHeaders | Unset
        if isinstance(_extra_headers, Unset):
            extra_headers = UNSET
        else:
            extra_headers = LlmProviderInputOptionsExtraHeaders.from_dict(
                _extra_headers
            )

        local = d.pop("local", UNSET)

        tts_model = d.pop("ttsModel", UNSET)

        tts_characters_per_day = d.pop("ttsCharactersPerDay", UNSET)

        llm_provider_input_options = cls(
            effort=effort,
            max_tokens=max_tokens,
            temperature=temperature,
            extra_headers=extra_headers,
            local=local,
            tts_model=tts_model,
            tts_characters_per_day=tts_characters_per_day,
        )

        llm_provider_input_options.additional_properties = d
        return llm_provider_input_options

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
