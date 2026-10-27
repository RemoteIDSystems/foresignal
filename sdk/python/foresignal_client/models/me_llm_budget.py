from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_budget_provider import LlmBudgetProvider


T = TypeVar("T", bound="MeLlmBudget")


@_attrs_define
class MeLlmBudget:
    """The calling user's LLM budget and spend across shared and personal providers (R2-011)

    Attributes:
        providers (list[LlmBudgetProvider]):
        total_spent_usd (float): Total spend this UTC month across all providers
        as_of (datetime.datetime | Unset):
    """

    providers: list[LlmBudgetProvider]
    total_spent_usd: float
    as_of: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        providers = []
        for providers_item_data in self.providers:
            providers_item = providers_item_data.to_dict()
            providers.append(providers_item)

        total_spent_usd = self.total_spent_usd

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "providers": providers,
                "totalSpentUsd": total_spent_usd,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_budget_provider import LlmBudgetProvider

        d = dict(src_dict)
        providers = []
        _providers = d.pop("providers")
        for providers_item_data in _providers:
            providers_item = LlmBudgetProvider.from_dict(providers_item_data)

            providers.append(providers_item)

        total_spent_usd = d.pop("totalSpentUsd")

        _as_of = d.pop("asOf", UNSET)
        as_of: datetime.datetime | Unset
        if isinstance(_as_of, Unset):
            as_of = UNSET
        else:
            as_of = datetime.datetime.fromisoformat(_as_of)

        me_llm_budget = cls(
            providers=providers,
            total_spent_usd=total_spent_usd,
            as_of=as_of,
        )

        me_llm_budget.additional_properties = d
        return me_llm_budget

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
