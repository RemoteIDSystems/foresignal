from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleBacktestResultActual")


@_attrs_define
class RuleBacktestResultActual:
    """Alerts the rule really fired in range

    Attributes:
        alerts (int | Unset):
        comparable (bool | Unset): Definition hash matches the alert's rule version or the rule is unchanged
    """

    alerts: int | Unset = UNSET
    comparable: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alerts = self.alerts

        comparable = self.comparable

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if comparable is not UNSET:
            field_dict["comparable"] = comparable

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alerts = d.pop("alerts", UNSET)

        comparable = d.pop("comparable", UNSET)

        rule_backtest_result_actual = cls(
            alerts=alerts,
            comparable=comparable,
        )

        rule_backtest_result_actual.additional_properties = d
        return rule_backtest_result_actual

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
