from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmSettings")


@_attrs_define
class LlmSettings:
    """
    Attributes:
        chat_provider (None | str | Unset):
        embedding_provider (None | str | Unset):
        ask_enabled (bool | Unset):  Default: True.
        enrichment_enabled (bool | Unset):  Default: True.
        log_content (bool | Unset):  Default: False.
        ask_max_tokens (int | Unset):  Default: 200000.
        allow_private_local (bool | Unset): Allow private-source records in prompts for providers classified as local
            (039): Ollama or `options.local`, whose base URL resolves only to loopback or private addresses. Never for
            remote providers. Default false. Default: False.
        tts_provider (None | str | Unset): Provider of the `tts` role (141): an enabled `openai-compatible` provider
            whose `/audio/speech` endpoint synthesizes audio briefings (OpenAI, Kokoro-FastAPI, openedai-speech). Budgeted
            by characters (`options.ttsCharactersPerDay`) and by the provider's requests per minute.
    """

    chat_provider: None | str | Unset = UNSET
    embedding_provider: None | str | Unset = UNSET
    ask_enabled: bool | Unset = True
    enrichment_enabled: bool | Unset = True
    log_content: bool | Unset = False
    ask_max_tokens: int | Unset = 200000
    allow_private_local: bool | Unset = False
    tts_provider: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chat_provider: None | str | Unset
        if isinstance(self.chat_provider, Unset):
            chat_provider = UNSET
        else:
            chat_provider = self.chat_provider

        embedding_provider: None | str | Unset
        if isinstance(self.embedding_provider, Unset):
            embedding_provider = UNSET
        else:
            embedding_provider = self.embedding_provider

        ask_enabled = self.ask_enabled

        enrichment_enabled = self.enrichment_enabled

        log_content = self.log_content

        ask_max_tokens = self.ask_max_tokens

        allow_private_local = self.allow_private_local

        tts_provider: None | str | Unset
        if isinstance(self.tts_provider, Unset):
            tts_provider = UNSET
        else:
            tts_provider = self.tts_provider

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if chat_provider is not UNSET:
            field_dict["chatProvider"] = chat_provider
        if embedding_provider is not UNSET:
            field_dict["embeddingProvider"] = embedding_provider
        if ask_enabled is not UNSET:
            field_dict["askEnabled"] = ask_enabled
        if enrichment_enabled is not UNSET:
            field_dict["enrichmentEnabled"] = enrichment_enabled
        if log_content is not UNSET:
            field_dict["logContent"] = log_content
        if ask_max_tokens is not UNSET:
            field_dict["askMaxTokens"] = ask_max_tokens
        if allow_private_local is not UNSET:
            field_dict["allowPrivateLocal"] = allow_private_local
        if tts_provider is not UNSET:
            field_dict["ttsProvider"] = tts_provider

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_chat_provider(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        chat_provider = _parse_chat_provider(d.pop("chatProvider", UNSET))

        def _parse_embedding_provider(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        embedding_provider = _parse_embedding_provider(
            d.pop("embeddingProvider", UNSET)
        )

        ask_enabled = d.pop("askEnabled", UNSET)

        enrichment_enabled = d.pop("enrichmentEnabled", UNSET)

        log_content = d.pop("logContent", UNSET)

        ask_max_tokens = d.pop("askMaxTokens", UNSET)

        allow_private_local = d.pop("allowPrivateLocal", UNSET)

        def _parse_tts_provider(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tts_provider = _parse_tts_provider(d.pop("ttsProvider", UNSET))

        llm_settings = cls(
            chat_provider=chat_provider,
            embedding_provider=embedding_provider,
            ask_enabled=ask_enabled,
            enrichment_enabled=enrichment_enabled,
            log_content=log_content,
            ask_max_tokens=ask_max_tokens,
            allow_private_local=allow_private_local,
            tts_provider=tts_provider,
        )

        llm_settings.additional_properties = d
        return llm_settings

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
