from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.recipe_backtest_result_counts_by_type import (
        RecipeBacktestResultCountsByType,
    )
    from ..models.recipe_backtest_result_events_item import (
        RecipeBacktestResultEventsItem,
    )
    from ..models.recipe_backtest_result_overlap import RecipeBacktestResultOverlap


T = TypeVar("T", bound="RecipeBacktestResult")


@_attrs_define
class RecipeBacktestResult:
    """Events a recipe would have formed over the range (dry run with simulated dedup and lifecycle)

    Attributes:
        recipe_id (str):
        events (list[RecipeBacktestResultEventsItem]):
        recipe_version (int | Unset):
        counts_by_type (RecipeBacktestResultCountsByType | Unset):
        overlap (RecipeBacktestResultOverlap | Unset): Overlap with real events in range (matched by shared members)
    """

    recipe_id: str
    events: list[RecipeBacktestResultEventsItem]
    recipe_version: int | Unset = UNSET
    counts_by_type: RecipeBacktestResultCountsByType | Unset = UNSET
    overlap: RecipeBacktestResultOverlap | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        recipe_id = self.recipe_id

        events = []
        for events_item_data in self.events:
            events_item = events_item_data.to_dict()
            events.append(events_item)

        recipe_version = self.recipe_version

        counts_by_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.counts_by_type, Unset):
            counts_by_type = self.counts_by_type.to_dict()

        overlap: dict[str, Any] | Unset = UNSET
        if not isinstance(self.overlap, Unset):
            overlap = self.overlap.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "recipeId": recipe_id,
                "events": events,
            }
        )
        if recipe_version is not UNSET:
            field_dict["recipeVersion"] = recipe_version
        if counts_by_type is not UNSET:
            field_dict["countsByType"] = counts_by_type
        if overlap is not UNSET:
            field_dict["overlap"] = overlap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.recipe_backtest_result_counts_by_type import (
            RecipeBacktestResultCountsByType,
        )
        from ..models.recipe_backtest_result_events_item import (
            RecipeBacktestResultEventsItem,
        )
        from ..models.recipe_backtest_result_overlap import RecipeBacktestResultOverlap

        d = dict(src_dict)
        recipe_id = d.pop("recipeId")

        events = []
        _events = d.pop("events")
        for events_item_data in _events:
            events_item = RecipeBacktestResultEventsItem.from_dict(events_item_data)

            events.append(events_item)

        recipe_version = d.pop("recipeVersion", UNSET)

        _counts_by_type = d.pop("countsByType", UNSET)
        counts_by_type: RecipeBacktestResultCountsByType | Unset
        if isinstance(_counts_by_type, Unset):
            counts_by_type = UNSET
        else:
            counts_by_type = RecipeBacktestResultCountsByType.from_dict(_counts_by_type)

        _overlap = d.pop("overlap", UNSET)
        overlap: RecipeBacktestResultOverlap | Unset
        if isinstance(_overlap, Unset):
            overlap = UNSET
        else:
            overlap = RecipeBacktestResultOverlap.from_dict(_overlap)

        recipe_backtest_result = cls(
            recipe_id=recipe_id,
            events=events,
            recipe_version=recipe_version,
            counts_by_type=counts_by_type,
            overlap=overlap,
        )

        recipe_backtest_result.additional_properties = d
        return recipe_backtest_result

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
