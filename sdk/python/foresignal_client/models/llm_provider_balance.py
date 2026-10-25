from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_provider_balance_amounts_item import LlmProviderBalanceAmountsItem


T = TypeVar("T", bound="LlmProviderBalance")


@_attrs_define
class LlmProviderBalance:
    """A balance the provider itself reports for the account behind the key (162). Information only: it is shown beside the
    caps and never enforced; the caps count Foresignal's own metered spend. Reported by DeepSeek (GET /user/balance,
    `total_balance` per currency) and OpenRouter (GET /api/v1/credits, total_credits less total_usage, which only a
    management key may read; otherwise GET /api/v1/key, the key's `limit_remaining`). Other providers expose no balance
    to the inference key, so they have none. Fetched at most once a minute.

        Attributes:
            provider (str): Who reported it Example: DeepSeek.
            kind (str): `account`: the account's balance or credits; `key-limit`: what is left of this key's own spending
                limit
            amounts (list[LlmProviderBalanceAmountsItem]): One amount per currency; empty when it could not be fetched
            fetched_at (datetime.datetime):
            error (None | str | Unset): Why it could not be fetched, or null
    """

    provider: str
    kind: str
    amounts: list[LlmProviderBalanceAmountsItem]
    fetched_at: datetime.datetime
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider = self.provider

        kind = self.kind

        amounts = []
        for amounts_item_data in self.amounts:
            amounts_item = amounts_item_data.to_dict()
            amounts.append(amounts_item)

        fetched_at = self.fetched_at.isoformat()

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "provider": provider,
                "kind": kind,
                "amounts": amounts,
                "fetchedAt": fetched_at,
            }
        )
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_provider_balance_amounts_item import (
            LlmProviderBalanceAmountsItem,
        )

        d = dict(src_dict)
        provider = d.pop("provider")

        kind = d.pop("kind")

        amounts = []
        _amounts = d.pop("amounts")
        for amounts_item_data in _amounts:
            amounts_item = LlmProviderBalanceAmountsItem.from_dict(amounts_item_data)

            amounts.append(amounts_item)

        fetched_at = datetime.datetime.fromisoformat(d.pop("fetchedAt"))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        llm_provider_balance = cls(
            provider=provider,
            kind=kind,
            amounts=amounts,
            fetched_at=fetched_at,
            error=error,
        )

        llm_provider_balance.additional_properties = d
        return llm_provider_balance

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
