from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_catalog_entry import SeriesCatalogEntry
    from ..models.series_point import SeriesPoint


T = TypeVar("T", bound="SeriesPointPage")


@_attrs_define
class SeriesPointPage:
    """Points of one series in `periodStart` order, with its catalogue entry (093)

    Attributes:
        series (SeriesCatalogEntry): A /v1/series catalogue item: 093's SeriesInfo plus the series metadata event
            studies need (also carried in the series-latest payload) (117)
        items (list[SeriesPoint]):
        next_cursor (None | str | Unset):
        truncated (bool | Unset): More points exist in [from, to) than `limit`
    """

    series: SeriesCatalogEntry
    items: list[SeriesPoint]
    next_cursor: None | str | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        series = self.series.to_dict()

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        next_cursor: None | str | Unset
        if isinstance(self.next_cursor, Unset):
            next_cursor = UNSET
        else:
            next_cursor = self.next_cursor

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "series": series,
                "items": items,
            }
        )
        if next_cursor is not UNSET:
            field_dict["nextCursor"] = next_cursor
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_catalog_entry import SeriesCatalogEntry
        from ..models.series_point import SeriesPoint

        d = dict(src_dict)
        series = SeriesCatalogEntry.from_dict(d.pop("series"))

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = SeriesPoint.from_dict(items_item_data)

            items.append(items_item)

        def _parse_next_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor", UNSET))

        truncated = d.pop("truncated", UNSET)

        series_point_page = cls(
            series=series,
            items=items,
            next_cursor=next_cursor,
            truncated=truncated,
        )

        series_point_page.additional_properties = d
        return series_point_page

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
