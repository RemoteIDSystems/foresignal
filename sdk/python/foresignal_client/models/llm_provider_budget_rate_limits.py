from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="LlmProviderBudgetRateLimits")


@_attrs_define
class LlmProviderBudgetRateLimits:
    """
    Attributes:
        requests_per_minute (int):
        tokens_per_day (int):
        tokens_today (int):
    """

    requests_per_minute: int
    tokens_per_day: int
    tokens_today: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        requests_per_minute = self.requests_per_minute

        tokens_per_day = self.tokens_per_day

        tokens_today = self.tokens_today

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "requestsPerMinute": requests_per_minute,
                "tokensPerDay": tokens_per_day,
                "tokensToday": tokens_today,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        requests_per_minute = d.pop("requestsPerMinute")

        tokens_per_day = d.pop("tokensPerDay")

        tokens_today = d.pop("tokensToday")

        llm_provider_budget_rate_limits = cls(
            requests_per_minute=requests_per_minute,
            tokens_per_day=tokens_per_day,
            tokens_today=tokens_today,
        )

        llm_provider_budget_rate_limits.additional_properties = d
        return llm_provider_budget_rate_limits

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
