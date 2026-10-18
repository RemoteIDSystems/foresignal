from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intelligence_event import IntelligenceEvent


T = TypeVar("T", bound="RecipeTestResult")


@_attrs_define
class RecipeTestResult:
    """Would-be events from an in-memory dry run (larger runs are 103 backtests)

    Attributes:
        recipe_id (str):
        from_seq (int):
        to_seq (int):
        events (list[IntelligenceEvent]):
        recipe_version (int | Unset):
        truncated (bool | Unset):
    """

    recipe_id: str
    from_seq: int
    to_seq: int
    events: list[IntelligenceEvent]
    recipe_version: int | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        recipe_id = self.recipe_id

        from_seq = self.from_seq

        to_seq = self.to_seq

        events = []
        for events_item_data in self.events:
            events_item = events_item_data.to_dict()
            events.append(events_item)

        recipe_version = self.recipe_version

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "recipeId": recipe_id,
                "fromSeq": from_seq,
                "toSeq": to_seq,
                "events": events,
            }
        )
        if recipe_version is not UNSET:
            field_dict["recipeVersion"] = recipe_version
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intelligence_event import IntelligenceEvent

        d = dict(src_dict)
        recipe_id = d.pop("recipeId")

        from_seq = d.pop("fromSeq")

        to_seq = d.pop("toSeq")

        events = []
        _events = d.pop("events")
        for events_item_data in _events:
            events_item = IntelligenceEvent.from_dict(events_item_data)

            events.append(events_item)

        recipe_version = d.pop("recipeVersion", UNSET)

        truncated = d.pop("truncated", UNSET)

        recipe_test_result = cls(
            recipe_id=recipe_id,
            from_seq=from_seq,
            to_seq=to_seq,
            events=events,
            recipe_version=recipe_version,
            truncated=truncated,
        )

        recipe_test_result.additional_properties = d
        return recipe_test_result

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
