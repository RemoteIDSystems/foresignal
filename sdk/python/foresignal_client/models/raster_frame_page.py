from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.raster_frame import RasterFrame
    from ..models.raster_frame_page_prefetch import RasterFramePagePrefetch


T = TypeVar("T", bound="RasterFramePage")


@_attrs_define
class RasterFramePage:
    """Cursor page of frames

    Attributes:
        items (list[RasterFrame]):
        server_time (datetime.datetime):
        next_cursor (None | str | Unset):
        prefetch (RasterFramePagePrefetch | Unset): Prefetch hint for animation
        earliest (datetime.datetime | None | Unset): Oldest retained frame
    """

    items: list[RasterFrame]
    server_time: datetime.datetime
    next_cursor: None | str | Unset = UNSET
    prefetch: RasterFramePagePrefetch | Unset = UNSET
    earliest: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        server_time = self.server_time.isoformat()

        next_cursor: None | str | Unset
        if isinstance(self.next_cursor, Unset):
            next_cursor = UNSET
        else:
            next_cursor = self.next_cursor

        prefetch: dict[str, Any] | Unset = UNSET
        if not isinstance(self.prefetch, Unset):
            prefetch = self.prefetch.to_dict()

        earliest: None | str | Unset
        if isinstance(self.earliest, Unset):
            earliest = UNSET
        elif isinstance(self.earliest, datetime.datetime):
            earliest = self.earliest.isoformat()
        else:
            earliest = self.earliest

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
                "serverTime": server_time,
            }
        )
        if next_cursor is not UNSET:
            field_dict["nextCursor"] = next_cursor
        if prefetch is not UNSET:
            field_dict["prefetch"] = prefetch
        if earliest is not UNSET:
            field_dict["earliest"] = earliest

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.raster_frame import RasterFrame
        from ..models.raster_frame_page_prefetch import RasterFramePagePrefetch

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = RasterFrame.from_dict(items_item_data)

            items.append(items_item)

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        def _parse_next_cursor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor", UNSET))

        _prefetch = d.pop("prefetch", UNSET)
        prefetch: RasterFramePagePrefetch | Unset
        if isinstance(_prefetch, Unset):
            prefetch = UNSET
        else:
            prefetch = RasterFramePagePrefetch.from_dict(_prefetch)

        def _parse_earliest(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                earliest_type_0 = datetime.datetime.fromisoformat(data)

                return earliest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        earliest = _parse_earliest(d.pop("earliest", UNSET))

        raster_frame_page = cls(
            items=items,
            server_time=server_time,
            next_cursor=next_cursor,
            prefetch=prefetch,
            earliest=earliest,
        )

        raster_frame_page.additional_properties = d
        return raster_frame_page

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
