from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSpend")


@_attrs_define
class SocialSpend:
    """Money spend of a paid platform this calendar month (005 budget in money as well as calls)

    Attributes:
        currency (Literal['USD']):
        spent_this_month (float): US dollars
        monthly_cap_usd (float | None | Unset): Calls are refused with BUDGET_EXCEEDED when spent + estimate would
            exceed it
        projected_this_month (float | None | Unset): Projected month-end spend at the current rate
        standing_counts_estimate_monthly (float | None | Unset): X: monthly cost of hourly counts for the current
            standing topics, shown before they are enabled
        prices_as_of (datetime.date | None | Unset): Date the seeded or edited prices were taken from the pricing page
        refused_this_month (int | Unset):
    """

    currency: Literal["USD"]
    spent_this_month: float
    monthly_cap_usd: float | None | Unset = UNSET
    projected_this_month: float | None | Unset = UNSET
    standing_counts_estimate_monthly: float | None | Unset = UNSET
    prices_as_of: datetime.date | None | Unset = UNSET
    refused_this_month: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        currency = self.currency

        spent_this_month = self.spent_this_month

        monthly_cap_usd: float | None | Unset
        if isinstance(self.monthly_cap_usd, Unset):
            monthly_cap_usd = UNSET
        else:
            monthly_cap_usd = self.monthly_cap_usd

        projected_this_month: float | None | Unset
        if isinstance(self.projected_this_month, Unset):
            projected_this_month = UNSET
        else:
            projected_this_month = self.projected_this_month

        standing_counts_estimate_monthly: float | None | Unset
        if isinstance(self.standing_counts_estimate_monthly, Unset):
            standing_counts_estimate_monthly = UNSET
        else:
            standing_counts_estimate_monthly = self.standing_counts_estimate_monthly

        prices_as_of: None | str | Unset
        if isinstance(self.prices_as_of, Unset):
            prices_as_of = UNSET
        elif isinstance(self.prices_as_of, datetime.date):
            prices_as_of = self.prices_as_of.isoformat()
        else:
            prices_as_of = self.prices_as_of

        refused_this_month = self.refused_this_month

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "currency": currency,
                "spentThisMonth": spent_this_month,
            }
        )
        if monthly_cap_usd is not UNSET:
            field_dict["monthlyCapUsd"] = monthly_cap_usd
        if projected_this_month is not UNSET:
            field_dict["projectedThisMonth"] = projected_this_month
        if standing_counts_estimate_monthly is not UNSET:
            field_dict["standingCountsEstimateMonthly"] = (
                standing_counts_estimate_monthly
            )
        if prices_as_of is not UNSET:
            field_dict["pricesAsOf"] = prices_as_of
        if refused_this_month is not UNSET:
            field_dict["refusedThisMonth"] = refused_this_month

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        currency = cast(Literal["USD"], d.pop("currency"))
        if currency != "USD":
            raise ValueError(f"currency must match const 'USD', got '{currency}'")

        spent_this_month = d.pop("spentThisMonth")

        def _parse_monthly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        monthly_cap_usd = _parse_monthly_cap_usd(d.pop("monthlyCapUsd", UNSET))

        def _parse_projected_this_month(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        projected_this_month = _parse_projected_this_month(
            d.pop("projectedThisMonth", UNSET)
        )

        def _parse_standing_counts_estimate_monthly(
            data: object,
        ) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        standing_counts_estimate_monthly = _parse_standing_counts_estimate_monthly(
            d.pop("standingCountsEstimateMonthly", UNSET)
        )

        def _parse_prices_as_of(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                prices_as_of_type_0 = datetime.date.fromisoformat(data)

                return prices_as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        prices_as_of = _parse_prices_as_of(d.pop("pricesAsOf", UNSET))

        refused_this_month = d.pop("refusedThisMonth", UNSET)

        social_spend = cls(
            currency=currency,
            spent_this_month=spent_this_month,
            monthly_cap_usd=monthly_cap_usd,
            projected_this_month=projected_this_month,
            standing_counts_estimate_monthly=standing_counts_estimate_monthly,
            prices_as_of=prices_as_of,
            refused_this_month=refused_this_month,
        )

        social_spend.additional_properties = d
        return social_spend

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
