from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.search_results_mode import SearchResultsMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.search_result import SearchResult


T = TypeVar("T", bound="SearchResults")


@_attrs_define
class SearchResults:
    """
    Attributes:
        mode (SearchResultsMode):
        items (list[SearchResult]):
        degraded (bool | Unset): True when semantic was requested but text was used
        attributions (list[Attribution] | Unset):
        licence_filtered (int | Unset):
        reason (None | str | Unset): Why semantic results were not used, when `degraded` is true (027)
    """

    mode: SearchResultsMode
    items: list[SearchResult]
    degraded: bool | Unset = UNSET
    attributions: list[Attribution] | Unset = UNSET
    licence_filtered: int | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        degraded = self.degraded

        attributions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attributions, Unset):
            attributions = []
            for attributions_item_data in self.attributions:
                attributions_item = attributions_item_data.to_dict()
                attributions.append(attributions_item)

        licence_filtered = self.licence_filtered

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mode": mode,
                "items": items,
            }
        )
        if degraded is not UNSET:
            field_dict["degraded"] = degraded
        if attributions is not UNSET:
            field_dict["attributions"] = attributions
        if licence_filtered is not UNSET:
            field_dict["licenceFiltered"] = licence_filtered
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.search_result import SearchResult

        d = dict(src_dict)
        mode = SearchResultsMode(d.pop("mode"))

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = SearchResult.from_dict(items_item_data)

            items.append(items_item)

        degraded = d.pop("degraded", UNSET)

        _attributions = d.pop("attributions", UNSET)
        attributions: list[Attribution] | Unset = UNSET
        if _attributions is not UNSET:
            attributions = []
            for attributions_item_data in _attributions:
                attributions_item = Attribution.from_dict(attributions_item_data)

                attributions.append(attributions_item)

        licence_filtered = d.pop("licenceFiltered", UNSET)

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        search_results = cls(
            mode=mode,
            items=items,
            degraded=degraded,
            attributions=attributions,
            licence_filtered=licence_filtered,
            reason=reason,
        )

        search_results.additional_properties = d
        return search_results

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
