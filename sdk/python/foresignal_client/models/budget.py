from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Budget")


@_attrs_define
class Budget:
    """
    Attributes:
        per_minute (int):
        per_day (int):
        monthly_cap_usd (float | None | Unset): Money cap per UTC calendar month for a paid source (135). Set with
            `updateSource`; the budget object is replaced whole, so leaving it out (or null) removes the cap. Once the
            month's spend (`SourceHealth.spentUsdThisMonth`) reaches it, the budget governor refuses every request of the
            source until the next month begins (a push or upload is refused 429 BUDGET_EXCEEDED) and the source's health is
            `degraded` with the reason. X's own platform cap (`social.x.monthlyCapUsd`, default 25) applies as well.
    """

    per_minute: int
    per_day: int
    monthly_cap_usd: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        per_minute = self.per_minute

        per_day = self.per_day

        monthly_cap_usd: float | None | Unset
        if isinstance(self.monthly_cap_usd, Unset):
            monthly_cap_usd = UNSET
        else:
            monthly_cap_usd = self.monthly_cap_usd

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "perMinute": per_minute,
                "perDay": per_day,
            }
        )
        if monthly_cap_usd is not UNSET:
            field_dict["monthlyCapUsd"] = monthly_cap_usd

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        per_minute = d.pop("perMinute")

        per_day = d.pop("perDay")

        def _parse_monthly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        monthly_cap_usd = _parse_monthly_cap_usd(d.pop("monthlyCapUsd", UNSET))

        budget = cls(
            per_minute=per_minute,
            per_day=per_day,
            monthly_cap_usd=monthly_cap_usd,
        )

        budget.additional_properties = d
        return budget

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
