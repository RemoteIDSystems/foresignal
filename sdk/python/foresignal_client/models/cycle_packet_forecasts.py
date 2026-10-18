from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_packet_item import CyclePacketItem


T = TypeVar("T", bound="CyclePacketForecasts")


@_attrs_define
class CyclePacketForecasts:
    """Ledger changes

    Attributes:
        opened (list[CyclePacketItem] | Unset):
        due (list[CyclePacketItem] | Unset):
        resolved (list[CyclePacketItem] | Unset):
        scorecard_changes (list[CyclePacketItem] | Unset):
    """

    opened: list[CyclePacketItem] | Unset = UNSET
    due: list[CyclePacketItem] | Unset = UNSET
    resolved: list[CyclePacketItem] | Unset = UNSET
    scorecard_changes: list[CyclePacketItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        opened: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.opened, Unset):
            opened = []
            for opened_item_data in self.opened:
                opened_item = opened_item_data.to_dict()
                opened.append(opened_item)

        due: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.due, Unset):
            due = []
            for due_item_data in self.due:
                due_item = due_item_data.to_dict()
                due.append(due_item)

        resolved: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.resolved, Unset):
            resolved = []
            for resolved_item_data in self.resolved:
                resolved_item = resolved_item_data.to_dict()
                resolved.append(resolved_item)

        scorecard_changes: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.scorecard_changes, Unset):
            scorecard_changes = []
            for scorecard_changes_item_data in self.scorecard_changes:
                scorecard_changes_item = scorecard_changes_item_data.to_dict()
                scorecard_changes.append(scorecard_changes_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if opened is not UNSET:
            field_dict["opened"] = opened
        if due is not UNSET:
            field_dict["due"] = due
        if resolved is not UNSET:
            field_dict["resolved"] = resolved
        if scorecard_changes is not UNSET:
            field_dict["scorecardChanges"] = scorecard_changes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_packet_item import CyclePacketItem

        d = dict(src_dict)
        _opened = d.pop("opened", UNSET)
        opened: list[CyclePacketItem] | Unset = UNSET
        if _opened is not UNSET:
            opened = []
            for opened_item_data in _opened:
                opened_item = CyclePacketItem.from_dict(opened_item_data)

                opened.append(opened_item)

        _due = d.pop("due", UNSET)
        due: list[CyclePacketItem] | Unset = UNSET
        if _due is not UNSET:
            due = []
            for due_item_data in _due:
                due_item = CyclePacketItem.from_dict(due_item_data)

                due.append(due_item)

        _resolved = d.pop("resolved", UNSET)
        resolved: list[CyclePacketItem] | Unset = UNSET
        if _resolved is not UNSET:
            resolved = []
            for resolved_item_data in _resolved:
                resolved_item = CyclePacketItem.from_dict(resolved_item_data)

                resolved.append(resolved_item)

        _scorecard_changes = d.pop("scorecardChanges", UNSET)
        scorecard_changes: list[CyclePacketItem] | Unset = UNSET
        if _scorecard_changes is not UNSET:
            scorecard_changes = []
            for scorecard_changes_item_data in _scorecard_changes:
                scorecard_changes_item = CyclePacketItem.from_dict(
                    scorecard_changes_item_data
                )

                scorecard_changes.append(scorecard_changes_item)

        cycle_packet_forecasts = cls(
            opened=opened,
            due=due,
            resolved=resolved,
            scorecard_changes=scorecard_changes,
        )

        cycle_packet_forecasts.additional_properties = d
        return cycle_packet_forecasts

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
