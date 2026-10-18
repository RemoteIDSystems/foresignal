from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.alert_signal_type import AlertSignalType

T = TypeVar("T", bound="AlertSignal")


@_attrs_define
class AlertSignal:
    """An implicit alert-quality signal (non-exercise alerts only; never stored as feedback) (115)

    Attributes:
        alert_id (int):
        type_ (AlertSignalType): ACK_FAST acknowledged within 15 min; SNOOZED alert or rule snoozed; FLAP resolved via
            inverse_edge within 10 min; UNOPENED notification unread after 24 h
        at (datetime.datetime):
    """

    alert_id: int
    type_: AlertSignalType
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert_id = self.alert_id

        type_ = self.type_.value

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alertId": alert_id,
                "type": type_,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alert_id = d.pop("alertId")

        type_ = AlertSignalType(d.pop("type"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        alert_signal = cls(
            alert_id=alert_id,
            type_=type_,
            at=at,
        )

        alert_signal.additional_properties = d
        return alert_signal

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
