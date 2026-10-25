from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmPricing")


@_attrs_define
class LlmPricing:
    """A provider's prices in USD per million tokens (162). Every price is optional; an unset price counts those tokens at
    zero. The spend the USD caps enforce is each call's metered tokens at these prices: input at
    inputUsdPerMillionTokens, output at outputUsdPerMillionTokens, and cached input (Anthropic cache reads, OpenAI
    `prompt_tokens_details.cached_tokens`) at cachedInputUsdPerMillionTokens, or the input price when that is unset;
    embedding calls at embeddingUsdPerMillionTokens. Prices take at most 6 decimal places, from 0 to 100000.

        Attributes:
            input_usd_per_million_tokens (float | None | Unset): USD per million input tokens Example: 3.
            output_usd_per_million_tokens (float | None | Unset): USD per million output tokens (thinking included) Example:
                15.
            cached_input_usd_per_million_tokens (float | None | Unset): USD per million cached input tokens; the input price
                when unset Example: 0.3.
            embedding_usd_per_million_tokens (float | None | Unset): USD per million embedding input tokens; refused for
                anthropic, which has no embeddings endpoint Example: 0.02.
    """

    input_usd_per_million_tokens: float | None | Unset = UNSET
    output_usd_per_million_tokens: float | None | Unset = UNSET
    cached_input_usd_per_million_tokens: float | None | Unset = UNSET
    embedding_usd_per_million_tokens: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        input_usd_per_million_tokens: float | None | Unset
        if isinstance(self.input_usd_per_million_tokens, Unset):
            input_usd_per_million_tokens = UNSET
        else:
            input_usd_per_million_tokens = self.input_usd_per_million_tokens

        output_usd_per_million_tokens: float | None | Unset
        if isinstance(self.output_usd_per_million_tokens, Unset):
            output_usd_per_million_tokens = UNSET
        else:
            output_usd_per_million_tokens = self.output_usd_per_million_tokens

        cached_input_usd_per_million_tokens: float | None | Unset
        if isinstance(self.cached_input_usd_per_million_tokens, Unset):
            cached_input_usd_per_million_tokens = UNSET
        else:
            cached_input_usd_per_million_tokens = (
                self.cached_input_usd_per_million_tokens
            )

        embedding_usd_per_million_tokens: float | None | Unset
        if isinstance(self.embedding_usd_per_million_tokens, Unset):
            embedding_usd_per_million_tokens = UNSET
        else:
            embedding_usd_per_million_tokens = self.embedding_usd_per_million_tokens

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if input_usd_per_million_tokens is not UNSET:
            field_dict["inputUsdPerMillionTokens"] = input_usd_per_million_tokens
        if output_usd_per_million_tokens is not UNSET:
            field_dict["outputUsdPerMillionTokens"] = output_usd_per_million_tokens
        if cached_input_usd_per_million_tokens is not UNSET:
            field_dict["cachedInputUsdPerMillionTokens"] = (
                cached_input_usd_per_million_tokens
            )
        if embedding_usd_per_million_tokens is not UNSET:
            field_dict["embeddingUsdPerMillionTokens"] = (
                embedding_usd_per_million_tokens
            )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_input_usd_per_million_tokens(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        input_usd_per_million_tokens = _parse_input_usd_per_million_tokens(
            d.pop("inputUsdPerMillionTokens", UNSET)
        )

        def _parse_output_usd_per_million_tokens(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        output_usd_per_million_tokens = _parse_output_usd_per_million_tokens(
            d.pop("outputUsdPerMillionTokens", UNSET)
        )

        def _parse_cached_input_usd_per_million_tokens(
            data: object,
        ) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        cached_input_usd_per_million_tokens = (
            _parse_cached_input_usd_per_million_tokens(
                d.pop("cachedInputUsdPerMillionTokens", UNSET)
            )
        )

        def _parse_embedding_usd_per_million_tokens(
            data: object,
        ) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        embedding_usd_per_million_tokens = _parse_embedding_usd_per_million_tokens(
            d.pop("embeddingUsdPerMillionTokens", UNSET)
        )

        llm_pricing = cls(
            input_usd_per_million_tokens=input_usd_per_million_tokens,
            output_usd_per_million_tokens=output_usd_per_million_tokens,
            cached_input_usd_per_million_tokens=cached_input_usd_per_million_tokens,
            embedding_usd_per_million_tokens=embedding_usd_per_million_tokens,
        )

        llm_pricing.additional_properties = d
        return llm_pricing

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
