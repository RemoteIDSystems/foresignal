from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleBacktestResultVariantsItemOverlap")


@_attrs_define
class RuleBacktestResultVariantsItemOverlap:
    """Overlap with what really happened

    Attributes:
        both (int | Unset):
        only_backtest (int | Unset):
        only_actual (int | Unset):
    """

    both: int | Unset = UNSET
    only_backtest: int | Unset = UNSET
    only_actual: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        both = self.both

        only_backtest = self.only_backtest

        only_actual = self.only_actual

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if both is not UNSET:
            field_dict["both"] = both
        if only_backtest is not UNSET:
            field_dict["onlyBacktest"] = only_backtest
        if only_actual is not UNSET:
            field_dict["onlyActual"] = only_actual

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        both = d.pop("both", UNSET)

        only_backtest = d.pop("onlyBacktest", UNSET)

        only_actual = d.pop("onlyActual", UNSET)

        rule_backtest_result_variants_item_overlap = cls(
            both=both,
            only_backtest=only_backtest,
            only_actual=only_actual,
        )

        rule_backtest_result_variants_item_overlap.additional_properties = d
        return rule_backtest_result_variants_item_overlap

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
