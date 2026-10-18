from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.alert_transition_input_feedback_type_1 import (
    AlertTransitionInputFeedbackType1,
)
from ..models.alert_transition_input_feedback_type_2_type_1 import (
    AlertTransitionInputFeedbackType2Type1,
)
from ..models.alert_transition_input_feedback_type_3_type_1 import (
    AlertTransitionInputFeedbackType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="AlertTransitionInput")


@_attrs_define
class AlertTransitionInput:
    """Optional note and feedback recorded when acknowledging or resolving an alert

    Attributes:
        note (None | str | Unset):
        feedback (AlertTransitionInputFeedbackType1 | AlertTransitionInputFeedbackType2Type1 |
            AlertTransitionInputFeedbackType3Type1 | None | Unset):
    """

    note: None | str | Unset = UNSET
    feedback: (
        AlertTransitionInputFeedbackType1
        | AlertTransitionInputFeedbackType2Type1
        | AlertTransitionInputFeedbackType3Type1
        | None
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        feedback: None | str | Unset
        if isinstance(self.feedback, Unset):
            feedback = UNSET
        elif (
            isinstance(self.feedback, AlertTransitionInputFeedbackType1)
            or isinstance(self.feedback, AlertTransitionInputFeedbackType2Type1)
            or isinstance(self.feedback, AlertTransitionInputFeedbackType3Type1)
        ):
            feedback = self.feedback.value
        else:
            feedback = self.feedback

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if note is not UNSET:
            field_dict["note"] = note
        if feedback is not UNSET:
            field_dict["feedback"] = feedback

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        def _parse_feedback(
            data: object,
        ) -> (
            AlertTransitionInputFeedbackType1
            | AlertTransitionInputFeedbackType2Type1
            | AlertTransitionInputFeedbackType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_type_1 = AlertTransitionInputFeedbackType1(data)

                return feedback_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_type_2_type_1 = AlertTransitionInputFeedbackType2Type1(data)

                return feedback_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                feedback_type_3_type_1 = AlertTransitionInputFeedbackType3Type1(data)

                return feedback_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AlertTransitionInputFeedbackType1
                | AlertTransitionInputFeedbackType2Type1
                | AlertTransitionInputFeedbackType3Type1
                | None
                | Unset,
                data,
            )

        feedback = _parse_feedback(d.pop("feedback", UNSET))

        alert_transition_input = cls(
            note=note,
            feedback=feedback,
        )

        alert_transition_input.additional_properties = d
        return alert_transition_input

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
