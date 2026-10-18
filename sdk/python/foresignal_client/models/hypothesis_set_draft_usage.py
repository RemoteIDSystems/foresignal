from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisSetDraftUsage")


@_attrs_define
class HypothesisSetDraftUsage:
    """Token usage

    Attributes:
        input_tokens (int | Unset):
        output_tokens (int | Unset):
        cached_tokens (int | Unset):
    """

    input_tokens: int | Unset = UNSET
    output_tokens: int | Unset = UNSET
    cached_tokens: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        input_tokens = self.input_tokens

        output_tokens = self.output_tokens

        cached_tokens = self.cached_tokens

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if input_tokens is not UNSET:
            field_dict["inputTokens"] = input_tokens
        if output_tokens is not UNSET:
            field_dict["outputTokens"] = output_tokens
        if cached_tokens is not UNSET:
            field_dict["cachedTokens"] = cached_tokens

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        input_tokens = d.pop("inputTokens", UNSET)

        output_tokens = d.pop("outputTokens", UNSET)

        cached_tokens = d.pop("cachedTokens", UNSET)

        hypothesis_set_draft_usage = cls(
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            cached_tokens=cached_tokens,
        )

        hypothesis_set_draft_usage.additional_properties = d
        return hypothesis_set_draft_usage

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
