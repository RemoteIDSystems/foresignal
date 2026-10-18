from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TuningSuggestionExpectedEffectType0")


@_attrs_define
class TuningSuggestionExpectedEffectType0:
    """From a backtest of the patched variant when simulated

    Attributes:
        would_fire (int | Unset):
        removed_negatives (int | Unset):
        kept_positives (int | Unset):
        backtest_id (None | str | Unset):
    """

    would_fire: int | Unset = UNSET
    removed_negatives: int | Unset = UNSET
    kept_positives: int | Unset = UNSET
    backtest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        would_fire = self.would_fire

        removed_negatives = self.removed_negatives

        kept_positives = self.kept_positives

        backtest_id: None | str | Unset
        if isinstance(self.backtest_id, Unset):
            backtest_id = UNSET
        else:
            backtest_id = self.backtest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if would_fire is not UNSET:
            field_dict["wouldFire"] = would_fire
        if removed_negatives is not UNSET:
            field_dict["removedNegatives"] = removed_negatives
        if kept_positives is not UNSET:
            field_dict["keptPositives"] = kept_positives
        if backtest_id is not UNSET:
            field_dict["backtestId"] = backtest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        would_fire = d.pop("wouldFire", UNSET)

        removed_negatives = d.pop("removedNegatives", UNSET)

        kept_positives = d.pop("keptPositives", UNSET)

        def _parse_backtest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        backtest_id = _parse_backtest_id(d.pop("backtestId", UNSET))

        tuning_suggestion_expected_effect_type_0 = cls(
            would_fire=would_fire,
            removed_negatives=removed_negatives,
            kept_positives=kept_positives,
            backtest_id=backtest_id,
        )

        tuning_suggestion_expected_effect_type_0.additional_properties = d
        return tuning_suggestion_expected_effect_type_0

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
