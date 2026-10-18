from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.key_assumption_event_previous_status import (
    KeyAssumptionEventPreviousStatus,
)
from ..models.key_assumption_event_status import KeyAssumptionEventStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="KeyAssumptionEvent")


@_attrs_define
class KeyAssumptionEvent:
    """SSE payload for `assumption.broken` (topic `hypotheses`)

    Attributes:
        assumption_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (KeyAssumptionEventStatus):
        at (datetime.datetime):
        text (str | Unset):
        linchpin (bool | Unset):
        previous_status (KeyAssumptionEventPreviousStatus | Unset):
        private (bool | Unset):
    """

    assumption_id: str
    set_id: str
    status: KeyAssumptionEventStatus
    at: datetime.datetime
    text: str | Unset = UNSET
    linchpin: bool | Unset = UNSET
    previous_status: KeyAssumptionEventPreviousStatus | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        assumption_id = self.assumption_id

        set_id = self.set_id

        status = self.status.value

        at = self.at.isoformat()

        text = self.text

        linchpin = self.linchpin

        previous_status: str | Unset = UNSET
        if not isinstance(self.previous_status, Unset):
            previous_status = self.previous_status.value

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "assumptionId": assumption_id,
                "setId": set_id,
                "status": status,
                "at": at,
            }
        )
        if text is not UNSET:
            field_dict["text"] = text
        if linchpin is not UNSET:
            field_dict["linchpin"] = linchpin
        if previous_status is not UNSET:
            field_dict["previousStatus"] = previous_status
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        assumption_id = d.pop("assumptionId")

        set_id = d.pop("setId")

        status = KeyAssumptionEventStatus(d.pop("status"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        text = d.pop("text", UNSET)

        linchpin = d.pop("linchpin", UNSET)

        _previous_status = d.pop("previousStatus", UNSET)
        previous_status: KeyAssumptionEventPreviousStatus | Unset
        if isinstance(_previous_status, Unset):
            previous_status = UNSET
        else:
            previous_status = KeyAssumptionEventPreviousStatus(_previous_status)

        private = d.pop("private", UNSET)

        key_assumption_event = cls(
            assumption_id=assumption_id,
            set_id=set_id,
            status=status,
            at=at,
            text=text,
            linchpin=linchpin,
            previous_status=previous_status,
            private=private,
        )

        key_assumption_event.additional_properties = d
        return key_assumption_event

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
