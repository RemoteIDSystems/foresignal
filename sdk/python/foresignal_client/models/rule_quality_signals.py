from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleQualitySignals")


@_attrs_define
class RuleQualitySignals:
    """Implicit signal counts (exercise alerts excluded)

    Attributes:
        ack_fast (int | Unset):
        snoozed (int | Unset):
        flap (int | Unset):
        unopened (int | Unset):
    """

    ack_fast: int | Unset = UNSET
    snoozed: int | Unset = UNSET
    flap: int | Unset = UNSET
    unopened: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ack_fast = self.ack_fast

        snoozed = self.snoozed

        flap = self.flap

        unopened = self.unopened

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if ack_fast is not UNSET:
            field_dict["ACK_FAST"] = ack_fast
        if snoozed is not UNSET:
            field_dict["SNOOZED"] = snoozed
        if flap is not UNSET:
            field_dict["FLAP"] = flap
        if unopened is not UNSET:
            field_dict["UNOPENED"] = unopened

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ack_fast = d.pop("ACK_FAST", UNSET)

        snoozed = d.pop("SNOOZED", UNSET)

        flap = d.pop("FLAP", UNSET)

        unopened = d.pop("UNOPENED", UNSET)

        rule_quality_signals = cls(
            ack_fast=ack_fast,
            snoozed=snoozed,
            flap=flap,
            unopened=unopened,
        )

        rule_quality_signals.additional_properties = d
        return rule_quality_signals

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
