from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.llm_spend_window import LlmSpendWindow


T = TypeVar("T", bound="LlmProviderBudgetWindows")


@_attrs_define
class LlmProviderBudgetWindows:
    """
    Attributes:
        hour (LlmSpendWindow): Spend in one window against its cap (162).
        day (LlmSpendWindow): Spend in one window against its cap (162).
        month (LlmSpendWindow): Spend in one window against its cap (162).
    """

    hour: LlmSpendWindow
    day: LlmSpendWindow
    month: LlmSpendWindow
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hour = self.hour.to_dict()

        day = self.day.to_dict()

        month = self.month.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hour": hour,
                "day": day,
                "month": month,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_spend_window import LlmSpendWindow

        d = dict(src_dict)
        hour = LlmSpendWindow.from_dict(d.pop("hour"))

        day = LlmSpendWindow.from_dict(d.pop("day"))

        month = LlmSpendWindow.from_dict(d.pop("month"))

        llm_provider_budget_windows = cls(
            hour=hour,
            day=day,
            month=month,
        )

        llm_provider_budget_windows.additional_properties = d
        return llm_provider_budget_windows

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
