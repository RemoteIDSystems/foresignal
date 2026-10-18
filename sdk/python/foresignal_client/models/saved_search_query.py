from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.saved_search_query_area_type_0 import SavedSearchQueryAreaType0
    from ..models.saved_search_query_filters import SavedSearchQueryFilters
    from ..models.saved_search_query_time_window_type_0 import (
        SavedSearchQueryTimeWindowType0,
    )


T = TypeVar("T", bound="SavedSearchQuery")


@_attrs_define
class SavedSearchQuery:
    """A saved query over object envelopes: 027 hybrid search plus structured filters

    Attributes:
        types (list[str] | Unset): Object types (Client-Contract §2); empty = all
        text (None | str | Unset):
        filters (SavedSearchQueryFilters | Unset): Structured filters (sources, kinds, severity, status…)
        area (None | SavedSearchQueryAreaType0 | Unset):
        time_window (None | SavedSearchQueryTimeWindowType0 | Unset):
        tags (list[str] | Unset): Tag ids the objects must carry
    """

    types: list[str] | Unset = UNSET
    text: None | str | Unset = UNSET
    filters: SavedSearchQueryFilters | Unset = UNSET
    area: None | SavedSearchQueryAreaType0 | Unset = UNSET
    time_window: None | SavedSearchQueryTimeWindowType0 | Unset = UNSET
    tags: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.saved_search_query_area_type_0 import SavedSearchQueryAreaType0
        from ..models.saved_search_query_time_window_type_0 import (
            SavedSearchQueryTimeWindowType0,
        )

        types: list[str] | Unset = UNSET
        if not isinstance(self.types, Unset):
            types = self.types

        text: None | str | Unset
        if isinstance(self.text, Unset):
            text = UNSET
        else:
            text = self.text

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, SavedSearchQueryAreaType0):
            area = self.area.to_dict()
        else:
            area = self.area

        time_window: dict[str, Any] | None | Unset
        if isinstance(self.time_window, Unset):
            time_window = UNSET
        elif isinstance(self.time_window, SavedSearchQueryTimeWindowType0):
            time_window = self.time_window.to_dict()
        else:
            time_window = self.time_window

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if types is not UNSET:
            field_dict["types"] = types
        if text is not UNSET:
            field_dict["text"] = text
        if filters is not UNSET:
            field_dict["filters"] = filters
        if area is not UNSET:
            field_dict["area"] = area
        if time_window is not UNSET:
            field_dict["timeWindow"] = time_window
        if tags is not UNSET:
            field_dict["tags"] = tags

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.saved_search_query_area_type_0 import SavedSearchQueryAreaType0
        from ..models.saved_search_query_filters import SavedSearchQueryFilters
        from ..models.saved_search_query_time_window_type_0 import (
            SavedSearchQueryTimeWindowType0,
        )

        d = dict(src_dict)
        types = cast(list[str], d.pop("types", UNSET))

        def _parse_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        text = _parse_text(d.pop("text", UNSET))

        _filters = d.pop("filters", UNSET)
        filters: SavedSearchQueryFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = SavedSearchQueryFilters.from_dict(_filters)

        def _parse_area(data: object) -> None | SavedSearchQueryAreaType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = SavedSearchQueryAreaType0.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SavedSearchQueryAreaType0 | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        def _parse_time_window(
            data: object,
        ) -> None | SavedSearchQueryTimeWindowType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                time_window_type_0 = SavedSearchQueryTimeWindowType0.from_dict(data)

                return time_window_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SavedSearchQueryTimeWindowType0 | Unset, data)

        time_window = _parse_time_window(d.pop("timeWindow", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        saved_search_query = cls(
            types=types,
            text=text,
            filters=filters,
            area=area,
            time_window=time_window,
            tags=tags,
        )

        saved_search_query.additional_properties = d
        return saved_search_query

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
