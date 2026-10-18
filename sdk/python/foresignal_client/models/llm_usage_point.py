from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmUsagePoint")


@_attrs_define
class LlmUsagePoint:
    """
    Attributes:
        t (datetime.datetime):
        requests (int):
        input_tokens (int):
        output_tokens (int):
        cached_tokens (int | Unset):
        errors (int | Unset):
        refused (int | Unset):
    """

    t: datetime.datetime
    requests: int
    input_tokens: int
    output_tokens: int
    cached_tokens: int | Unset = UNSET
    errors: int | Unset = UNSET
    refused: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        requests = self.requests

        input_tokens = self.input_tokens

        output_tokens = self.output_tokens

        cached_tokens = self.cached_tokens

        errors = self.errors

        refused = self.refused

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "requests": requests,
                "inputTokens": input_tokens,
                "outputTokens": output_tokens,
            }
        )
        if cached_tokens is not UNSET:
            field_dict["cachedTokens"] = cached_tokens
        if errors is not UNSET:
            field_dict["errors"] = errors
        if refused is not UNSET:
            field_dict["refused"] = refused

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        requests = d.pop("requests")

        input_tokens = d.pop("inputTokens")

        output_tokens = d.pop("outputTokens")

        cached_tokens = d.pop("cachedTokens", UNSET)

        errors = d.pop("errors", UNSET)

        refused = d.pop("refused", UNSET)

        llm_usage_point = cls(
            t=t,
            requests=requests,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            cached_tokens=cached_tokens,
            errors=errors,
            refused=refused,
        )

        llm_usage_point.additional_properties = d
        return llm_usage_point

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
