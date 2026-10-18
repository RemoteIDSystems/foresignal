from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_packet_item import CyclePacketItem


T = TypeVar("T", bound="CyclePacketSituations")


@_attrs_define
class CyclePacketSituations:
    """Situation changes (118 snapshot diffs)

    Attributes:
        new (list[CyclePacketItem] | Unset):
        activated (list[CyclePacketItem] | Unset):
        escalated (list[CyclePacketItem] | Unset):
        dormant (list[CyclePacketItem] | Unset):
        closed (list[CyclePacketItem] | Unset):
    """

    new: list[CyclePacketItem] | Unset = UNSET
    activated: list[CyclePacketItem] | Unset = UNSET
    escalated: list[CyclePacketItem] | Unset = UNSET
    dormant: list[CyclePacketItem] | Unset = UNSET
    closed: list[CyclePacketItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        new: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.new, Unset):
            new = []
            for new_item_data in self.new:
                new_item = new_item_data.to_dict()
                new.append(new_item)

        activated: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.activated, Unset):
            activated = []
            for activated_item_data in self.activated:
                activated_item = activated_item_data.to_dict()
                activated.append(activated_item)

        escalated: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.escalated, Unset):
            escalated = []
            for escalated_item_data in self.escalated:
                escalated_item = escalated_item_data.to_dict()
                escalated.append(escalated_item)

        dormant: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.dormant, Unset):
            dormant = []
            for dormant_item_data in self.dormant:
                dormant_item = dormant_item_data.to_dict()
                dormant.append(dormant_item)

        closed: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.closed, Unset):
            closed = []
            for closed_item_data in self.closed:
                closed_item = closed_item_data.to_dict()
                closed.append(closed_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if new is not UNSET:
            field_dict["new"] = new
        if activated is not UNSET:
            field_dict["activated"] = activated
        if escalated is not UNSET:
            field_dict["escalated"] = escalated
        if dormant is not UNSET:
            field_dict["dormant"] = dormant
        if closed is not UNSET:
            field_dict["closed"] = closed

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_packet_item import CyclePacketItem

        d = dict(src_dict)
        _new = d.pop("new", UNSET)
        new: list[CyclePacketItem] | Unset = UNSET
        if _new is not UNSET:
            new = []
            for new_item_data in _new:
                new_item = CyclePacketItem.from_dict(new_item_data)

                new.append(new_item)

        _activated = d.pop("activated", UNSET)
        activated: list[CyclePacketItem] | Unset = UNSET
        if _activated is not UNSET:
            activated = []
            for activated_item_data in _activated:
                activated_item = CyclePacketItem.from_dict(activated_item_data)

                activated.append(activated_item)

        _escalated = d.pop("escalated", UNSET)
        escalated: list[CyclePacketItem] | Unset = UNSET
        if _escalated is not UNSET:
            escalated = []
            for escalated_item_data in _escalated:
                escalated_item = CyclePacketItem.from_dict(escalated_item_data)

                escalated.append(escalated_item)

        _dormant = d.pop("dormant", UNSET)
        dormant: list[CyclePacketItem] | Unset = UNSET
        if _dormant is not UNSET:
            dormant = []
            for dormant_item_data in _dormant:
                dormant_item = CyclePacketItem.from_dict(dormant_item_data)

                dormant.append(dormant_item)

        _closed = d.pop("closed", UNSET)
        closed: list[CyclePacketItem] | Unset = UNSET
        if _closed is not UNSET:
            closed = []
            for closed_item_data in _closed:
                closed_item = CyclePacketItem.from_dict(closed_item_data)

                closed.append(closed_item)

        cycle_packet_situations = cls(
            new=new,
            activated=activated,
            escalated=escalated,
            dormant=dormant,
            closed=closed,
        )

        cycle_packet_situations.additional_properties = d
        return cycle_packet_situations

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
