from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmBudgetProvider")


@_attrs_define
class LlmBudgetProvider:
    """The caller's spend and budget on one provider (per-user-per-provider metering, 174)

    Attributes:
        provider_id (str): The provider id (an `LlmProvider.id`)
        name (str):
        shared (bool): True for an admin-managed shared provider, false for the user's personal provider
        spent_usd (float): Spent this UTC month
        cap_usd (float | None | Unset): The caller's monthly USD cap, if set
        remaining_usd (float | None | Unset): cap minus spend; null when uncapped
    """

    provider_id: str
    name: str
    shared: bool
    spent_usd: float
    cap_usd: float | None | Unset = UNSET
    remaining_usd: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider_id = self.provider_id

        name = self.name

        shared = self.shared

        spent_usd = self.spent_usd

        cap_usd: float | None | Unset
        if isinstance(self.cap_usd, Unset):
            cap_usd = UNSET
        else:
            cap_usd = self.cap_usd

        remaining_usd: float | None | Unset
        if isinstance(self.remaining_usd, Unset):
            remaining_usd = UNSET
        else:
            remaining_usd = self.remaining_usd

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "providerId": provider_id,
                "name": name,
                "shared": shared,
                "spentUsd": spent_usd,
            }
        )
        if cap_usd is not UNSET:
            field_dict["capUsd"] = cap_usd
        if remaining_usd is not UNSET:
            field_dict["remainingUsd"] = remaining_usd

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        provider_id = d.pop("providerId")

        name = d.pop("name")

        shared = d.pop("shared")

        spent_usd = d.pop("spentUsd")

        def _parse_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        cap_usd = _parse_cap_usd(d.pop("capUsd", UNSET))

        def _parse_remaining_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        remaining_usd = _parse_remaining_usd(d.pop("remainingUsd", UNSET))

        llm_budget_provider = cls(
            provider_id=provider_id,
            name=name,
            shared=shared,
            spent_usd=spent_usd,
            cap_usd=cap_usd,
            remaining_usd=remaining_usd,
        )

        llm_budget_provider.additional_properties = d
        return llm_budget_provider

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
