from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_event_log_entry_after_type_0 import IntelEventLogEntryAfterType0
    from ..models.intel_event_log_entry_before_type_0 import (
        IntelEventLogEntryBeforeType0,
    )


T = TypeVar("T", bound="IntelEventLogEntry")


@_attrs_define
class IntelEventLogEntry:
    """Audit entry of an intelligence event

    Attributes:
        at (datetime.datetime):
        actor (str): Token id or `recipe:<id>`
        action (str): What happened. The list grows as the server adds actions; a client shows an action it does not
            know as its text.
        before (IntelEventLogEntryBeforeType0 | None | Unset):
        after (IntelEventLogEntryAfterType0 | None | Unset):
    """

    at: datetime.datetime
    actor: str
    action: str
    before: IntelEventLogEntryBeforeType0 | None | Unset = UNSET
    after: IntelEventLogEntryAfterType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.intel_event_log_entry_after_type_0 import (
            IntelEventLogEntryAfterType0,
        )
        from ..models.intel_event_log_entry_before_type_0 import (
            IntelEventLogEntryBeforeType0,
        )

        at = self.at.isoformat()

        actor = self.actor

        action = self.action

        before: dict[str, Any] | None | Unset
        if isinstance(self.before, Unset):
            before = UNSET
        elif isinstance(self.before, IntelEventLogEntryBeforeType0):
            before = self.before.to_dict()
        else:
            before = self.before

        after: dict[str, Any] | None | Unset
        if isinstance(self.after, Unset):
            after = UNSET
        elif isinstance(self.after, IntelEventLogEntryAfterType0):
            after = self.after.to_dict()
        else:
            after = self.after

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "actor": actor,
                "action": action,
            }
        )
        if before is not UNSET:
            field_dict["before"] = before
        if after is not UNSET:
            field_dict["after"] = after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_event_log_entry_after_type_0 import (
            IntelEventLogEntryAfterType0,
        )
        from ..models.intel_event_log_entry_before_type_0 import (
            IntelEventLogEntryBeforeType0,
        )

        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        actor = d.pop("actor")

        action = d.pop("action")

        def _parse_before(data: object) -> IntelEventLogEntryBeforeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                before_type_0 = IntelEventLogEntryBeforeType0.from_dict(data)

                return before_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IntelEventLogEntryBeforeType0 | None | Unset, data)

        before = _parse_before(d.pop("before", UNSET))

        def _parse_after(data: object) -> IntelEventLogEntryAfterType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                after_type_0 = IntelEventLogEntryAfterType0.from_dict(data)

                return after_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IntelEventLogEntryAfterType0 | None | Unset, data)

        after = _parse_after(d.pop("after", UNSET))

        intel_event_log_entry = cls(
            at=at,
            actor=actor,
            action=action,
            before=before,
            after=after,
        )

        intel_event_log_entry.additional_properties = d
        return intel_event_log_entry

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
