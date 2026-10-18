from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoLlm")


@_attrs_define
class SystemInfoLlm:
    """
    Attributes:
        configured (bool | Unset):
        chat_provider (None | str | Unset):
        embedding_provider (None | str | Unset):
        tokens_today (int | Unset):
    """

    configured: bool | Unset = UNSET
    chat_provider: None | str | Unset = UNSET
    embedding_provider: None | str | Unset = UNSET
    tokens_today: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        configured = self.configured

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

        tokens_today = self.tokens_today

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if configured is not UNSET:
            field_dict["configured"] = configured
        if chat_provider is not UNSET:
            field_dict["chatProvider"] = chat_provider
        if embedding_provider is not UNSET:
            field_dict["embeddingProvider"] = embedding_provider
        if tokens_today is not UNSET:
            field_dict["tokensToday"] = tokens_today

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        configured = d.pop("configured", UNSET)

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

        tokens_today = d.pop("tokensToday", UNSET)

        system_info_llm = cls(
            configured=configured,
            chat_provider=chat_provider,
            embedding_provider=embedding_provider,
            tokens_today=tokens_today,
        )

        system_info_llm.additional_properties = d
        return system_info_llm

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
