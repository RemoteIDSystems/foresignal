from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_provider_budget_currency import LlmProviderBudgetCurrency

if TYPE_CHECKING:
    from ..models.llm_pricing import LlmPricing
    from ..models.llm_provider_balance import LlmProviderBalance
    from ..models.llm_provider_budget_rate_limits import LlmProviderBudgetRateLimits
    from ..models.llm_provider_budget_windows import LlmProviderBudgetWindows


T = TypeVar("T", bound="LlmProviderBudget")


@_attrs_define
class LlmProviderBudget:
    """A provider's spend against its USD caps, its token and request limits, and the balance it reports, when it reports
    one (162).

        Attributes:
            provider_id (str):
            currency (LlmProviderBudgetCurrency):
            priced (bool): Whether any price is set; without prices spend stays 0
            pricing (LlmPricing): A provider's prices in USD per million tokens (162). Every price is optional; an unset
                price counts those tokens at zero. The spend the USD caps enforce is each call's metered tokens at these prices:
                input at inputUsdPerMillionTokens, output at outputUsdPerMillionTokens, and cached input (Anthropic cache reads,
                OpenAI `prompt_tokens_details.cached_tokens`) at cachedInputUsdPerMillionTokens, or the input price when that is
                unset; embedding calls at embeddingUsdPerMillionTokens. Prices take at most 6 decimal places, from 0 to 100000.
            windows (LlmProviderBudgetWindows):
            rate_limits (LlmProviderBudgetRateLimits):
            provider_balance (LlmProviderBalance | None): The provider-reported balance, or null when the provider reports
                none
    """

    provider_id: str
    currency: LlmProviderBudgetCurrency
    priced: bool
    pricing: LlmPricing
    windows: LlmProviderBudgetWindows
    rate_limits: LlmProviderBudgetRateLimits
    provider_balance: LlmProviderBalance | None
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.llm_provider_balance import LlmProviderBalance

        provider_id = self.provider_id

        currency = self.currency.value

        priced = self.priced

        pricing = self.pricing.to_dict()

        windows = self.windows.to_dict()

        rate_limits = self.rate_limits.to_dict()

        provider_balance: dict[str, Any] | None
        if isinstance(self.provider_balance, LlmProviderBalance):
            provider_balance = self.provider_balance.to_dict()
        else:
            provider_balance = self.provider_balance

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "providerId": provider_id,
                "currency": currency,
                "priced": priced,
                "pricing": pricing,
                "windows": windows,
                "rateLimits": rate_limits,
                "providerBalance": provider_balance,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_pricing import LlmPricing
        from ..models.llm_provider_balance import LlmProviderBalance
        from ..models.llm_provider_budget_rate_limits import LlmProviderBudgetRateLimits
        from ..models.llm_provider_budget_windows import LlmProviderBudgetWindows

        d = dict(src_dict)
        provider_id = d.pop("providerId")

        currency = LlmProviderBudgetCurrency(d.pop("currency"))

        priced = d.pop("priced")

        pricing = LlmPricing.from_dict(d.pop("pricing"))

        windows = LlmProviderBudgetWindows.from_dict(d.pop("windows"))

        rate_limits = LlmProviderBudgetRateLimits.from_dict(d.pop("rateLimits"))

        def _parse_provider_balance(data: object) -> LlmProviderBalance | None:
            if data is None:
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                provider_balance_type_0 = LlmProviderBalance.from_dict(data)

                return provider_balance_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LlmProviderBalance | None, data)

        provider_balance = _parse_provider_balance(d.pop("providerBalance"))

        llm_provider_budget = cls(
            provider_id=provider_id,
            currency=currency,
            priced=priced,
            pricing=pricing,
            windows=windows,
            rate_limits=rate_limits,
            provider_balance=provider_balance,
        )

        llm_provider_budget.additional_properties = d
        return llm_provider_budget

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
