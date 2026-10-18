from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.trigger_test_result_window_evaluations_item_state import (
    TriggerTestResultWindowEvaluationsItemState,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="TriggerTestResultWindowEvaluationsItem")


@_attrs_define
class TriggerTestResultWindowEvaluationsItem:
    """Windowed test result at one step instant

    Attributes:
        at (datetime.datetime):
        group_key (str):
        state (TriggerTestResultWindowEvaluationsItemState):
        value (float | None | Unset):
        reason (None | str | Unset):
    """

    at: datetime.datetime
    group_key: str
    state: TriggerTestResultWindowEvaluationsItemState
    value: float | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at = self.at.isoformat()

        group_key = self.group_key

        state = self.state.value

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "groupKey": group_key,
                "state": state,
            }
        )
        if value is not UNSET:
            field_dict["value"] = value
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        group_key = d.pop("groupKey")

        state = TriggerTestResultWindowEvaluationsItemState(d.pop("state"))

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        trigger_test_result_window_evaluations_item = cls(
            at=at,
            group_key=group_key,
            state=state,
            value=value,
            reason=reason,
        )

        trigger_test_result_window_evaluations_item.additional_properties = d
        return trigger_test_result_window_evaluations_item

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
