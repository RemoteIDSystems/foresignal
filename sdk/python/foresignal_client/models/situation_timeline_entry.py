from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_timeline_entry_entry_type import SituationTimelineEntryEntryType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_member import SituationMember
    from ..models.situation_timeline_entry_after_type_0 import (
        SituationTimelineEntryAfterType0,
    )
    from ..models.situation_timeline_entry_before_type_0 import (
        SituationTimelineEntryBeforeType0,
    )


T = TypeVar("T", bound="SituationTimelineEntry")


@_attrs_define
class SituationTimelineEntry:
    """One timeline item: a member observation or a situation log entry, in time order

    Attributes:
        at (datetime.datetime):
        entry_type (SituationTimelineEntryEntryType):
        member (None | SituationMember | Unset):
        action (None | str | Unset): Log action (log entries); null for a member entry. The list grows as the server
            adds actions; a client shows an action it does not know as its text.
        actor (None | str | Unset): Token id, `linker:<version>` or `analyst:<profile>`
        before (None | SituationTimelineEntryBeforeType0 | Unset):
        after (None | SituationTimelineEntryAfterType0 | Unset):
    """

    at: datetime.datetime
    entry_type: SituationTimelineEntryEntryType
    member: None | SituationMember | Unset = UNSET
    action: None | str | Unset = UNSET
    actor: None | str | Unset = UNSET
    before: None | SituationTimelineEntryBeforeType0 | Unset = UNSET
    after: None | SituationTimelineEntryAfterType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.situation_member import SituationMember
        from ..models.situation_timeline_entry_after_type_0 import (
            SituationTimelineEntryAfterType0,
        )
        from ..models.situation_timeline_entry_before_type_0 import (
            SituationTimelineEntryBeforeType0,
        )

        at = self.at.isoformat()

        entry_type = self.entry_type.value

        member: dict[str, Any] | None | Unset
        if isinstance(self.member, Unset):
            member = UNSET
        elif isinstance(self.member, SituationMember):
            member = self.member.to_dict()
        else:
            member = self.member

        action: None | str | Unset
        if isinstance(self.action, Unset):
            action = UNSET
        else:
            action = self.action

        actor: None | str | Unset
        if isinstance(self.actor, Unset):
            actor = UNSET
        else:
            actor = self.actor

        before: dict[str, Any] | None | Unset
        if isinstance(self.before, Unset):
            before = UNSET
        elif isinstance(self.before, SituationTimelineEntryBeforeType0):
            before = self.before.to_dict()
        else:
            before = self.before

        after: dict[str, Any] | None | Unset
        if isinstance(self.after, Unset):
            after = UNSET
        elif isinstance(self.after, SituationTimelineEntryAfterType0):
            after = self.after.to_dict()
        else:
            after = self.after

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "entryType": entry_type,
            }
        )
        if member is not UNSET:
            field_dict["member"] = member
        if action is not UNSET:
            field_dict["action"] = action
        if actor is not UNSET:
            field_dict["actor"] = actor
        if before is not UNSET:
            field_dict["before"] = before
        if after is not UNSET:
            field_dict["after"] = after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_member import SituationMember
        from ..models.situation_timeline_entry_after_type_0 import (
            SituationTimelineEntryAfterType0,
        )
        from ..models.situation_timeline_entry_before_type_0 import (
            SituationTimelineEntryBeforeType0,
        )

        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        entry_type = SituationTimelineEntryEntryType(d.pop("entryType"))

        def _parse_member(data: object) -> None | SituationMember | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                member_type_0 = SituationMember.from_dict(data)

                return member_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationMember | Unset, data)

        member = _parse_member(d.pop("member", UNSET))

        def _parse_action(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        action = _parse_action(d.pop("action", UNSET))

        def _parse_actor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        actor = _parse_actor(d.pop("actor", UNSET))

        def _parse_before(
            data: object,
        ) -> None | SituationTimelineEntryBeforeType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                before_type_0 = SituationTimelineEntryBeforeType0.from_dict(data)

                return before_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationTimelineEntryBeforeType0 | Unset, data)

        before = _parse_before(d.pop("before", UNSET))

        def _parse_after(
            data: object,
        ) -> None | SituationTimelineEntryAfterType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                after_type_0 = SituationTimelineEntryAfterType0.from_dict(data)

                return after_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationTimelineEntryAfterType0 | Unset, data)

        after = _parse_after(d.pop("after", UNSET))

        situation_timeline_entry = cls(
            at=at,
            entry_type=entry_type,
            member=member,
            action=action,
            actor=actor,
            before=before,
            after=after,
        )

        situation_timeline_entry.additional_properties = d
        return situation_timeline_entry

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
