from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleBacktestResultWindowType0")


@_attrs_define
class RuleBacktestResultWindowType0:
    """Windowed rules

    Attributes:
        steps (int | Unset):
        unknown_steps (int | Unset):
    """

    steps: int | Unset = UNSET
    unknown_steps: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        steps = self.steps

        unknown_steps = self.unknown_steps

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if steps is not UNSET:
            field_dict["steps"] = steps
        if unknown_steps is not UNSET:
            field_dict["unknownSteps"] = unknown_steps

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        steps = d.pop("steps", UNSET)

        unknown_steps = d.pop("unknownSteps", UNSET)

        rule_backtest_result_window_type_0 = cls(
            steps=steps,
            unknown_steps=unknown_steps,
        )

        rule_backtest_result_window_type_0.additional_properties = d
        return rule_backtest_result_window_type_0

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
