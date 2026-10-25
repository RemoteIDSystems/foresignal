from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="NarrativeLogEntry")


@_attrs_define
class NarrativeLogEntry:
    """A narrative history entry (merges and splits follow 118 rules)

    Attributes:
        at (datetime.datetime):
        action (str): What happened. The list grows as the server adds actions; a client shows an action it does not
            know as its text.
        by (str): `system` or principal id
        related_narrative_ids (list[str] | Unset):
        from_ (None | str | Unset):
        to (None | str | Unset):
        reason (None | str | Unset):
    """

    at: datetime.datetime
    action: str
    by: str
    related_narrative_ids: list[str] | Unset = UNSET
    from_: None | str | Unset = UNSET
    to: None | str | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at = self.at.isoformat()

        action = self.action

        by = self.by

        related_narrative_ids: list[str] | Unset = UNSET
        if not isinstance(self.related_narrative_ids, Unset):
            related_narrative_ids = self.related_narrative_ids

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        else:
            to = self.to

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "action": action,
                "by": by,
            }
        )
        if related_narrative_ids is not UNSET:
            field_dict["relatedNarrativeIds"] = related_narrative_ids
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        action = d.pop("action")

        by = d.pop("by")

        related_narrative_ids = cast(list[str], d.pop("relatedNarrativeIds", UNSET))

        def _parse_from_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        narrative_log_entry = cls(
            at=at,
            action=action,
            by=by,
            related_narrative_ids=related_narrative_ids,
            from_=from_,
            to=to,
            reason=reason,
        )

        narrative_log_entry.additional_properties = d
        return narrative_log_entry

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
