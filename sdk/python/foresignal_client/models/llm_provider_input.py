from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_provider_input_type import LlmProviderInputType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_provider_input_options import LlmProviderInputOptions


T = TypeVar("T", bound="LlmProviderInput")


@_attrs_define
class LlmProviderInput:
    """
    Attributes:
        id (str):
        type_ (LlmProviderInputType):
        base_url (None | str | Unset): Defaults: anthropic → https://api.anthropic.com, ollama → http://ollama:11434
        chat_model (None | str | Unset):  Example: claude-opus-5-5.
        embedding_model (None | str | Unset):  Example: nomic-embed-text.
        embedding_dimensions (int | None | Unset):
        api_key (str | Unset): Write-only
        options (LlmProviderInputOptions | Unset):
        requests_per_minute (int | Unset):  Default: 30.
        tokens_per_day (int | Unset):  Default: 2000000.
        enabled (bool | Unset):  Default: True.
        knowledge_cutoff (datetime.date | None | Unset): Training-data cutoff of the chat model (`YYYY-MM-DD`)
    """

    id: str
    type_: LlmProviderInputType
    base_url: None | str | Unset = UNSET
    chat_model: None | str | Unset = UNSET
    embedding_model: None | str | Unset = UNSET
    embedding_dimensions: int | None | Unset = UNSET
    api_key: str | Unset = UNSET
    options: LlmProviderInputOptions | Unset = UNSET
    requests_per_minute: int | Unset = 30
    tokens_per_day: int | Unset = 2000000
    enabled: bool | Unset = True
    knowledge_cutoff: datetime.date | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        type_ = self.type_.value

        base_url: None | str | Unset
        if isinstance(self.base_url, Unset):
            base_url = UNSET
        else:
            base_url = self.base_url

        chat_model: None | str | Unset
        if isinstance(self.chat_model, Unset):
            chat_model = UNSET
        else:
            chat_model = self.chat_model

        embedding_model: None | str | Unset
        if isinstance(self.embedding_model, Unset):
            embedding_model = UNSET
        else:
            embedding_model = self.embedding_model

        embedding_dimensions: int | None | Unset
        if isinstance(self.embedding_dimensions, Unset):
            embedding_dimensions = UNSET
        else:
            embedding_dimensions = self.embedding_dimensions

        api_key = self.api_key

        options: dict[str, Any] | Unset = UNSET
        if not isinstance(self.options, Unset):
            options = self.options.to_dict()

        requests_per_minute = self.requests_per_minute

        tokens_per_day = self.tokens_per_day

        enabled = self.enabled

        knowledge_cutoff: None | str | Unset
        if isinstance(self.knowledge_cutoff, Unset):
            knowledge_cutoff = UNSET
        elif isinstance(self.knowledge_cutoff, datetime.date):
            knowledge_cutoff = self.knowledge_cutoff.isoformat()
        else:
            knowledge_cutoff = self.knowledge_cutoff

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
            }
        )
        if base_url is not UNSET:
            field_dict["baseUrl"] = base_url
        if chat_model is not UNSET:
            field_dict["chatModel"] = chat_model
        if embedding_model is not UNSET:
            field_dict["embeddingModel"] = embedding_model
        if embedding_dimensions is not UNSET:
            field_dict["embeddingDimensions"] = embedding_dimensions
        if api_key is not UNSET:
            field_dict["apiKey"] = api_key
        if options is not UNSET:
            field_dict["options"] = options
        if requests_per_minute is not UNSET:
            field_dict["requestsPerMinute"] = requests_per_minute
        if tokens_per_day is not UNSET:
            field_dict["tokensPerDay"] = tokens_per_day
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if knowledge_cutoff is not UNSET:
            field_dict["knowledgeCutoff"] = knowledge_cutoff

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_provider_input_options import LlmProviderInputOptions

        d = dict(src_dict)
        id = d.pop("id")

        type_ = LlmProviderInputType(d.pop("type"))

        def _parse_base_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        base_url = _parse_base_url(d.pop("baseUrl", UNSET))

        def _parse_chat_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        chat_model = _parse_chat_model(d.pop("chatModel", UNSET))

        def _parse_embedding_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        embedding_model = _parse_embedding_model(d.pop("embeddingModel", UNSET))

        def _parse_embedding_dimensions(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        embedding_dimensions = _parse_embedding_dimensions(
            d.pop("embeddingDimensions", UNSET)
        )

        api_key = d.pop("apiKey", UNSET)

        _options = d.pop("options", UNSET)
        options: LlmProviderInputOptions | Unset
        if isinstance(_options, Unset):
            options = UNSET
        else:
            options = LlmProviderInputOptions.from_dict(_options)

        requests_per_minute = d.pop("requestsPerMinute", UNSET)

        tokens_per_day = d.pop("tokensPerDay", UNSET)

        enabled = d.pop("enabled", UNSET)

        def _parse_knowledge_cutoff(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                knowledge_cutoff_type_0 = datetime.date.fromisoformat(data)

                return knowledge_cutoff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        knowledge_cutoff = _parse_knowledge_cutoff(d.pop("knowledgeCutoff", UNSET))

        llm_provider_input = cls(
            id=id,
            type_=type_,
            base_url=base_url,
            chat_model=chat_model,
            embedding_model=embedding_model,
            embedding_dimensions=embedding_dimensions,
            api_key=api_key,
            options=options,
            requests_per_minute=requests_per_minute,
            tokens_per_day=tokens_per_day,
            enabled=enabled,
            knowledge_cutoff=knowledge_cutoff,
        )

        llm_provider_input.additional_properties = d
        return llm_provider_input

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
