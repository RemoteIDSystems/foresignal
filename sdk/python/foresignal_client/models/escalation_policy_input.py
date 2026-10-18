from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.escalation_policy_input_repeat_type_0 import (
        EscalationPolicyInputRepeatType0,
    )
    from ..models.escalation_policy_input_stop_on import EscalationPolicyInputStopOn
    from ..models.escalation_step import EscalationStep


T = TypeVar("T", bound="EscalationPolicyInput")


@_attrs_define
class EscalationPolicyInput:
    """Create or replace an escalation policy (033)

    Attributes:
        name (str):
        steps (list[EscalationStep]):
        repeat (EscalationPolicyInputRepeatType0 | None | Unset): Optional repeat of the last step
        stop_on (EscalationPolicyInputStopOn | Unset): Stop conditions; acknowledgement always stops
    """

    name: str
    steps: list[EscalationStep]
    repeat: EscalationPolicyInputRepeatType0 | None | Unset = UNSET
    stop_on: EscalationPolicyInputStopOn | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.escalation_policy_input_repeat_type_0 import (
            EscalationPolicyInputRepeatType0,
        )

        name = self.name

        steps = []
        for steps_item_data in self.steps:
            steps_item = steps_item_data.to_dict()
            steps.append(steps_item)

        repeat: dict[str, Any] | None | Unset
        if isinstance(self.repeat, Unset):
            repeat = UNSET
        elif isinstance(self.repeat, EscalationPolicyInputRepeatType0):
            repeat = self.repeat.to_dict()
        else:
            repeat = self.repeat

        stop_on: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stop_on, Unset):
            stop_on = self.stop_on.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "steps": steps,
            }
        )
        if repeat is not UNSET:
            field_dict["repeat"] = repeat
        if stop_on is not UNSET:
            field_dict["stopOn"] = stop_on

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.escalation_policy_input_repeat_type_0 import (
            EscalationPolicyInputRepeatType0,
        )
        from ..models.escalation_policy_input_stop_on import EscalationPolicyInputStopOn
        from ..models.escalation_step import EscalationStep

        d = dict(src_dict)
        name = d.pop("name")

        steps = []
        _steps = d.pop("steps")
        for steps_item_data in _steps:
            steps_item = EscalationStep.from_dict(steps_item_data)

            steps.append(steps_item)

        def _parse_repeat(
            data: object,
        ) -> EscalationPolicyInputRepeatType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                repeat_type_0 = EscalationPolicyInputRepeatType0.from_dict(data)

                return repeat_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EscalationPolicyInputRepeatType0 | None | Unset, data)

        repeat = _parse_repeat(d.pop("repeat", UNSET))

        _stop_on = d.pop("stopOn", UNSET)
        stop_on: EscalationPolicyInputStopOn | Unset
        if isinstance(_stop_on, Unset):
            stop_on = UNSET
        else:
            stop_on = EscalationPolicyInputStopOn.from_dict(_stop_on)

        escalation_policy_input = cls(
            name=name,
            steps=steps,
            repeat=repeat,
            stop_on=stop_on,
        )

        escalation_policy_input.additional_properties = d
        return escalation_policy_input

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
