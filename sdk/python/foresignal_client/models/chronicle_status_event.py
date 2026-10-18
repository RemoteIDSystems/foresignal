from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_status_event_cap_state import ChronicleStatusEventCapState
from ..models.chronicle_status_event_previous_status_type_1 import (
    ChronicleStatusEventPreviousStatusType1,
)
from ..models.chronicle_status_event_previous_status_type_2_type_1 import (
    ChronicleStatusEventPreviousStatusType2Type1,
)
from ..models.chronicle_status_event_previous_status_type_3_type_1 import (
    ChronicleStatusEventPreviousStatusType3Type1,
)
from ..models.chronicle_status_event_status import ChronicleStatusEventStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleStatusEvent")


@_attrs_define
class ChronicleStatusEvent:
    """SSE `chronicle.status` on the `chronicle` topic

    Attributes:
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ChronicleStatusEventStatus):
        at (datetime.datetime):
        previous_status (ChronicleStatusEventPreviousStatusType1 | ChronicleStatusEventPreviousStatusType2Type1 |
            ChronicleStatusEventPreviousStatusType3Type1 | None | Unset):
        cap_state (ChronicleStatusEventCapState | Unset):
        bytes_used (int | Unset):
        reason (None | str | Unset):
    """

    chronicle_id: str
    status: ChronicleStatusEventStatus
    at: datetime.datetime
    previous_status: (
        ChronicleStatusEventPreviousStatusType1
        | ChronicleStatusEventPreviousStatusType2Type1
        | ChronicleStatusEventPreviousStatusType3Type1
        | None
        | Unset
    ) = UNSET
    cap_state: ChronicleStatusEventCapState | Unset = UNSET
    bytes_used: int | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_id = self.chronicle_id

        status = self.status.value

        at = self.at.isoformat()

        previous_status: None | str | Unset
        if isinstance(self.previous_status, Unset):
            previous_status = UNSET
        elif (
            isinstance(self.previous_status, ChronicleStatusEventPreviousStatusType1)
            or isinstance(
                self.previous_status, ChronicleStatusEventPreviousStatusType2Type1
            )
            or isinstance(
                self.previous_status, ChronicleStatusEventPreviousStatusType3Type1
            )
        ):
            previous_status = self.previous_status.value
        else:
            previous_status = self.previous_status

        cap_state: str | Unset = UNSET
        if not isinstance(self.cap_state, Unset):
            cap_state = self.cap_state.value

        bytes_used = self.bytes_used

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chronicleId": chronicle_id,
                "status": status,
                "at": at,
            }
        )
        if previous_status is not UNSET:
            field_dict["previousStatus"] = previous_status
        if cap_state is not UNSET:
            field_dict["capState"] = cap_state
        if bytes_used is not UNSET:
            field_dict["bytesUsed"] = bytes_used
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        status = ChronicleStatusEventStatus(d.pop("status"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_previous_status(
            data: object,
        ) -> (
            ChronicleStatusEventPreviousStatusType1
            | ChronicleStatusEventPreviousStatusType2Type1
            | ChronicleStatusEventPreviousStatusType3Type1
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
                previous_status_type_1 = ChronicleStatusEventPreviousStatusType1(data)

                return previous_status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_2_type_1 = (
                    ChronicleStatusEventPreviousStatusType2Type1(data)
                )

                return previous_status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_3_type_1 = (
                    ChronicleStatusEventPreviousStatusType3Type1(data)
                )

                return previous_status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                ChronicleStatusEventPreviousStatusType1
                | ChronicleStatusEventPreviousStatusType2Type1
                | ChronicleStatusEventPreviousStatusType3Type1
                | None
                | Unset,
                data,
            )

        previous_status = _parse_previous_status(d.pop("previousStatus", UNSET))

        _cap_state = d.pop("capState", UNSET)
        cap_state: ChronicleStatusEventCapState | Unset
        if isinstance(_cap_state, Unset):
            cap_state = UNSET
        else:
            cap_state = ChronicleStatusEventCapState(_cap_state)

        bytes_used = d.pop("bytesUsed", UNSET)

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        chronicle_status_event = cls(
            chronicle_id=chronicle_id,
            status=status,
            at=at,
            previous_status=previous_status,
            cap_state=cap_state,
            bytes_used=bytes_used,
            reason=reason,
        )

        chronicle_status_event.additional_properties = d
        return chronicle_status_event

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
