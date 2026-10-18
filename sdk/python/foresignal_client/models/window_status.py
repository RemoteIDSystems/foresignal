from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.window_status_states import WindowStatusStates


T = TypeVar("T", bound="WindowStatus")


@_attrs_define
class WindowStatus:
    """Summary of a windowed rule's state across its groups

    Attributes:
        states (WindowStatusStates): Group counts per state
        groups (int):
        groups_truncated (bool | Unset):
        learning_until (datetime.datetime | None | Unset): Anomaly rules: when every slot is expected to have minWeeks
            samples
        last_evaluated_at (datetime.datetime | None | Unset):
        low_confidence (bool | Unset): hour-of-day anomaly rules started before minWeeks
    """

    states: WindowStatusStates
    groups: int
    groups_truncated: bool | Unset = UNSET
    learning_until: datetime.datetime | None | Unset = UNSET
    last_evaluated_at: datetime.datetime | None | Unset = UNSET
    low_confidence: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        states = self.states.to_dict()

        groups = self.groups

        groups_truncated = self.groups_truncated

        learning_until: None | str | Unset
        if isinstance(self.learning_until, Unset):
            learning_until = UNSET
        elif isinstance(self.learning_until, datetime.datetime):
            learning_until = self.learning_until.isoformat()
        else:
            learning_until = self.learning_until

        last_evaluated_at: None | str | Unset
        if isinstance(self.last_evaluated_at, Unset):
            last_evaluated_at = UNSET
        elif isinstance(self.last_evaluated_at, datetime.datetime):
            last_evaluated_at = self.last_evaluated_at.isoformat()
        else:
            last_evaluated_at = self.last_evaluated_at

        low_confidence = self.low_confidence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "states": states,
                "groups": groups,
            }
        )
        if groups_truncated is not UNSET:
            field_dict["groupsTruncated"] = groups_truncated
        if learning_until is not UNSET:
            field_dict["learningUntil"] = learning_until
        if last_evaluated_at is not UNSET:
            field_dict["lastEvaluatedAt"] = last_evaluated_at
        if low_confidence is not UNSET:
            field_dict["lowConfidence"] = low_confidence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.window_status_states import WindowStatusStates

        d = dict(src_dict)
        states = WindowStatusStates.from_dict(d.pop("states"))

        groups = d.pop("groups")

        groups_truncated = d.pop("groupsTruncated", UNSET)

        def _parse_learning_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                learning_until_type_0 = datetime.datetime.fromisoformat(data)

                return learning_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        learning_until = _parse_learning_until(d.pop("learningUntil", UNSET))

        def _parse_last_evaluated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_evaluated_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_evaluated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_evaluated_at = _parse_last_evaluated_at(d.pop("lastEvaluatedAt", UNSET))

        low_confidence = d.pop("lowConfidence", UNSET)

        window_status = cls(
            states=states,
            groups=groups,
            groups_truncated=groups_truncated,
            learning_until=learning_until,
            last_evaluated_at=last_evaluated_at,
            low_confidence=low_confidence,
        )

        window_status.additional_properties = d
        return window_status

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
