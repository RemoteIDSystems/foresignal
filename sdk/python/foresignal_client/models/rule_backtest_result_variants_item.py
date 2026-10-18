from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.rule_backtest_result_variants_item_overlap import (
        RuleBacktestResultVariantsItemOverlap,
    )
    from ..models.rule_backtest_result_variants_item_patch import (
        RuleBacktestResultVariantsItemPatch,
    )
    from ..models.rule_backtest_result_variants_item_per_day_item import (
        RuleBacktestResultVariantsItemPerDayItem,
    )


T = TypeVar("T", bound="RuleBacktestResultVariantsItem")


@_attrs_define
class RuleBacktestResultVariantsItem:
    """Result of one variant

    Attributes:
        index (int):
        patch (RuleBacktestResultVariantsItemPatch):
        would_fire (int):
        per_day (list[RuleBacktestResultVariantsItemPerDayItem] | Unset):
        overlap (RuleBacktestResultVariantsItemOverlap | Unset): Overlap with what really happened
    """

    index: int
    patch: RuleBacktestResultVariantsItemPatch
    would_fire: int
    per_day: list[RuleBacktestResultVariantsItemPerDayItem] | Unset = UNSET
    overlap: RuleBacktestResultVariantsItemOverlap | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index = self.index

        patch = self.patch.to_dict()

        would_fire = self.would_fire

        per_day: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.per_day, Unset):
            per_day = []
            for per_day_item_data in self.per_day:
                per_day_item = per_day_item_data.to_dict()
                per_day.append(per_day_item)

        overlap: dict[str, Any] | Unset = UNSET
        if not isinstance(self.overlap, Unset):
            overlap = self.overlap.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "index": index,
                "patch": patch,
                "wouldFire": would_fire,
            }
        )
        if per_day is not UNSET:
            field_dict["perDay"] = per_day
        if overlap is not UNSET:
            field_dict["overlap"] = overlap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.rule_backtest_result_variants_item_overlap import (
            RuleBacktestResultVariantsItemOverlap,
        )
        from ..models.rule_backtest_result_variants_item_patch import (
            RuleBacktestResultVariantsItemPatch,
        )
        from ..models.rule_backtest_result_variants_item_per_day_item import (
            RuleBacktestResultVariantsItemPerDayItem,
        )

        d = dict(src_dict)
        index = d.pop("index")

        patch = RuleBacktestResultVariantsItemPatch.from_dict(d.pop("patch"))

        would_fire = d.pop("wouldFire")

        _per_day = d.pop("perDay", UNSET)
        per_day: list[RuleBacktestResultVariantsItemPerDayItem] | Unset = UNSET
        if _per_day is not UNSET:
            per_day = []
            for per_day_item_data in _per_day:
                per_day_item = RuleBacktestResultVariantsItemPerDayItem.from_dict(
                    per_day_item_data
                )

                per_day.append(per_day_item)

        _overlap = d.pop("overlap", UNSET)
        overlap: RuleBacktestResultVariantsItemOverlap | Unset
        if isinstance(_overlap, Unset):
            overlap = UNSET
        else:
            overlap = RuleBacktestResultVariantsItemOverlap.from_dict(_overlap)

        rule_backtest_result_variants_item = cls(
            index=index,
            patch=patch,
            would_fire=would_fire,
            per_day=per_day,
            overlap=overlap,
        )

        rule_backtest_result_variants_item.additional_properties = d
        return rule_backtest_result_variants_item

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
