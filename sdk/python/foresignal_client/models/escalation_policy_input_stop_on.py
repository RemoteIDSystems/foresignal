from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EscalationPolicyInputStopOn")


@_attrs_define
class EscalationPolicyInputStopOn:
    """Stop conditions; acknowledgement always stops

    Attributes:
        resolve (bool | Unset):  Default: True.
        snooze (bool | Unset):  Default: True.
    """

    resolve: bool | Unset = True
    snooze: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        resolve = self.resolve

        snooze = self.snooze

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if resolve is not UNSET:
            field_dict["resolve"] = resolve
        if snooze is not UNSET:
            field_dict["snooze"] = snooze

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        resolve = d.pop("resolve", UNSET)

        snooze = d.pop("snooze", UNSET)

        escalation_policy_input_stop_on = cls(
            resolve=resolve,
            snooze=snooze,
        )

        escalation_policy_input_stop_on.additional_properties = d
        return escalation_policy_input_stop_on

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
