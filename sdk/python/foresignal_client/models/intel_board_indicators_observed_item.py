from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelBoardIndicatorsObservedItem")


@_attrs_define
class IntelBoardIndicatorsObservedItem:
    """Indicator observed in the last 24 h

    Attributes:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        at (datetime.datetime):
        description (str | Unset):
    """

    indicator_id: str
    set_id: str
    at: datetime.datetime
    description: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        indicator_id = self.indicator_id

        set_id = self.set_id

        at = self.at.isoformat()

        description = self.description

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "indicatorId": indicator_id,
                "setId": set_id,
                "at": at,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        indicator_id = d.pop("indicatorId")

        set_id = d.pop("setId")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        description = d.pop("description", UNSET)

        intel_board_indicators_observed_item = cls(
            indicator_id=indicator_id,
            set_id=set_id,
            at=at,
            description=description,
        )

        intel_board_indicators_observed_item.additional_properties = d
        return intel_board_indicators_observed_item

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
