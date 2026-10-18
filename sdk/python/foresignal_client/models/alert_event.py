from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.alert_event_type import AlertEventType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alert import Alert


T = TypeVar("T", bound="AlertEvent")


@_attrs_define
class AlertEvent:
    """Alert lifecycle event (AlertBus; SSE `alert`, `alert.updated`, `alert.resolved`); clients upsert by `alert.id`

    Attributes:
        type_ (AlertEventType): Lifecycle transition; SSE maps fired → `alert`, resolved → `alert.resolved`, all others
            → `alert.updated`
        alert (Alert):
        suppressed (bool | Unset): Suppressed by a rule or alert snooze (not routed to channels; hidden on streams
            unless includeSuppressed) Default: False.
        exercise (bool | Unset): Exercise alert Default: False.
    """

    type_: AlertEventType
    alert: Alert
    suppressed: bool | Unset = False
    exercise: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        alert = self.alert.to_dict()

        suppressed = self.suppressed

        exercise = self.exercise

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "alert": alert,
            }
        )
        if suppressed is not UNSET:
            field_dict["suppressed"] = suppressed
        if exercise is not UNSET:
            field_dict["exercise"] = exercise

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alert import Alert

        d = dict(src_dict)
        type_ = AlertEventType(d.pop("type"))

        alert = Alert.from_dict(d.pop("alert"))

        suppressed = d.pop("suppressed", UNSET)

        exercise = d.pop("exercise", UNSET)

        alert_event = cls(
            type_=type_,
            alert=alert,
            suppressed=suppressed,
            exercise=exercise,
        )

        alert_event.additional_properties = d
        return alert_event

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
