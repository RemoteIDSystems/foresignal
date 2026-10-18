from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.trigger_rule_input import TriggerRuleInput


T = TypeVar("T", bound="TuningLogEntry")


@_attrs_define
class TuningLogEntry:
    """An applied rule change from a tuning suggestion, undoable while the rule is unchanged (115)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        rule_id (str):
        before (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds, conditions, geofence or
            near is required. Unknown sources → 400; unknown kinds warn.
        after (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds, conditions, geofence or near
            is required. Unknown sources → 400; unknown kinds warn.
        applied_by (str):
        applied_at (datetime.datetime):
        suggestion_id (None | str | Unset):
        note (None | str | Unset):
        undone_by (None | str | Unset):
        undone_at (datetime.datetime | None | Unset):
    """

    id: str
    rule_id: str
    before: TriggerRuleInput
    after: TriggerRuleInput
    applied_by: str
    applied_at: datetime.datetime
    suggestion_id: None | str | Unset = UNSET
    note: None | str | Unset = UNSET
    undone_by: None | str | Unset = UNSET
    undone_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        rule_id = self.rule_id

        before = self.before.to_dict()

        after = self.after.to_dict()

        applied_by = self.applied_by

        applied_at = self.applied_at.isoformat()

        suggestion_id: None | str | Unset
        if isinstance(self.suggestion_id, Unset):
            suggestion_id = UNSET
        else:
            suggestion_id = self.suggestion_id

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        undone_by: None | str | Unset
        if isinstance(self.undone_by, Unset):
            undone_by = UNSET
        else:
            undone_by = self.undone_by

        undone_at: None | str | Unset
        if isinstance(self.undone_at, Unset):
            undone_at = UNSET
        elif isinstance(self.undone_at, datetime.datetime):
            undone_at = self.undone_at.isoformat()
        else:
            undone_at = self.undone_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ruleId": rule_id,
                "before": before,
                "after": after,
                "appliedBy": applied_by,
                "appliedAt": applied_at,
            }
        )
        if suggestion_id is not UNSET:
            field_dict["suggestionId"] = suggestion_id
        if note is not UNSET:
            field_dict["note"] = note
        if undone_by is not UNSET:
            field_dict["undoneBy"] = undone_by
        if undone_at is not UNSET:
            field_dict["undoneAt"] = undone_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.trigger_rule_input import TriggerRuleInput

        d = dict(src_dict)
        id = d.pop("id")

        rule_id = d.pop("ruleId")

        before = TriggerRuleInput.from_dict(d.pop("before"))

        after = TriggerRuleInput.from_dict(d.pop("after"))

        applied_by = d.pop("appliedBy")

        applied_at = datetime.datetime.fromisoformat(d.pop("appliedAt"))

        def _parse_suggestion_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        suggestion_id = _parse_suggestion_id(d.pop("suggestionId", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        def _parse_undone_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        undone_by = _parse_undone_by(d.pop("undoneBy", UNSET))

        def _parse_undone_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                undone_at_type_0 = datetime.datetime.fromisoformat(data)

                return undone_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        undone_at = _parse_undone_at(d.pop("undoneAt", UNSET))

        tuning_log_entry = cls(
            id=id,
            rule_id=rule_id,
            before=before,
            after=after,
            applied_by=applied_by,
            applied_at=applied_at,
            suggestion_id=suggestion_id,
            note=note,
            undone_by=undone_by,
            undone_at=undone_at,
        )

        tuning_log_entry.additional_properties = d
        return tuning_log_entry

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
