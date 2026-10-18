from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cpa_result import CpaResult
    from ..models.kinematic_state import KinematicState


T = TypeVar("T", bound="CpaBatchResult")


@_attrs_define
class CpaBatchResult:
    """Top N CPA results by dcpa (114)

    Attributes:
        subject (KinematicState): Kinematic state of a record from head ⋈ live (114)
        as_of (datetime.datetime):
        evaluated (int):
        items (list[CpaResult]):
        truncated (bool | Unset): More than 500 candidates matched the filter
    """

    subject: KinematicState
    as_of: datetime.datetime
    evaluated: int
    items: list[CpaResult]
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subject = self.subject.to_dict()

        as_of = self.as_of.isoformat()

        evaluated = self.evaluated

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subject": subject,
                "asOf": as_of,
                "evaluated": evaluated,
                "items": items,
            }
        )
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cpa_result import CpaResult
        from ..models.kinematic_state import KinematicState

        d = dict(src_dict)
        subject = KinematicState.from_dict(d.pop("subject"))

        as_of = datetime.datetime.fromisoformat(d.pop("asOf"))

        evaluated = d.pop("evaluated")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = CpaResult.from_dict(items_item_data)

            items.append(items_item)

        truncated = d.pop("truncated", UNSET)

        cpa_batch_result = cls(
            subject=subject,
            as_of=as_of,
            evaluated=evaluated,
            items=items,
            truncated=truncated,
        )

        cpa_batch_result.additional_properties = d
        return cpa_batch_result

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
