from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.action_result_action import ActionResultAction

T = TypeVar("T", bound="ActionResult")


@_attrs_define
class ActionResult:
    """Result of executing a notification action capability (033)

    Attributes:
        action (ActionResultAction):
        alert_id (int):
        result (str): e.g. acknowledged, snoozed, muted, already-acknowledged, already-resolved Example: acknowledged.
    """

    action: ActionResultAction
    alert_id: int
    result: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        action = self.action.value

        alert_id = self.alert_id

        result = self.result

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "action": action,
                "alertId": alert_id,
                "result": result,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        action = ActionResultAction(d.pop("action"))

        alert_id = d.pop("alertId")

        result = d.pop("result")

        action_result = cls(
            action=action,
            alert_id=alert_id,
            result=result,
        )

        action_result.additional_properties = d
        return action_result

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
