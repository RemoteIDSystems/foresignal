from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_spend_window_window import LlmSpendWindowWindow

T = TypeVar("T", bound="LlmSpendWindow")


@_attrs_define
class LlmSpendWindow:
    """Spend in one window against its cap (162).

    Attributes:
        window (LlmSpendWindowWindow):
        start (datetime.datetime): When the window began (UTC boundary)
        resets_at (datetime.datetime): When it resets: the next hour, 00:00 UTC, or the 1st
        spent_usd (float): USD spent in the window: settled calls at the provider's prices, seeded from the stored usage
            after a restart
        reserved_usd (float): Estimated USD of the calls in flight, held against the cap until they settle
        cap_usd (float | None): The cap, or null when this window has none
        remaining_usd (float | None): capUsd less spentUsd, never below 0; null without a cap
        exhausted (bool): True when spent plus reserved has reached the cap: calls are refused until resetsAt
    """

    window: LlmSpendWindowWindow
    start: datetime.datetime
    resets_at: datetime.datetime
    spent_usd: float
    reserved_usd: float
    cap_usd: float | None
    remaining_usd: float | None
    exhausted: bool
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        window = self.window.value

        start = self.start.isoformat()

        resets_at = self.resets_at.isoformat()

        spent_usd = self.spent_usd

        reserved_usd = self.reserved_usd

        cap_usd: float | None
        cap_usd = self.cap_usd

        remaining_usd: float | None
        remaining_usd = self.remaining_usd

        exhausted = self.exhausted

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "window": window,
                "start": start,
                "resetsAt": resets_at,
                "spentUsd": spent_usd,
                "reservedUsd": reserved_usd,
                "capUsd": cap_usd,
                "remainingUsd": remaining_usd,
                "exhausted": exhausted,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        window = LlmSpendWindowWindow(d.pop("window"))

        start = datetime.datetime.fromisoformat(d.pop("start"))

        resets_at = datetime.datetime.fromisoformat(d.pop("resetsAt"))

        spent_usd = d.pop("spentUsd")

        reserved_usd = d.pop("reservedUsd")

        def _parse_cap_usd(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        cap_usd = _parse_cap_usd(d.pop("capUsd"))

        def _parse_remaining_usd(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        remaining_usd = _parse_remaining_usd(d.pop("remainingUsd"))

        exhausted = d.pop("exhausted")

        llm_spend_window = cls(
            window=window,
            start=start,
            resets_at=resets_at,
            spent_usd=spent_usd,
            reserved_usd=reserved_usd,
            cap_usd=cap_usd,
            remaining_usd=remaining_usd,
            exhausted=exhausted,
        )

        llm_spend_window.additional_properties = d
        return llm_spend_window

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
