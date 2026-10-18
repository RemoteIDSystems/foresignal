from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_imagery_items_item_kind import (
    DossierSectionsImageryItemsItemKind,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="DossierSectionsImageryItemsItem")


@_attrs_define
class DossierSectionsImageryItemsItem:
    """Raster frame, SatDump product or STAC scene

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (DossierSectionsImageryItemsItemKind):
        title (str | Unset):
        time (datetime.datetime | None | Unset):
        source_id (None | str | Unset):
        frame_url (None | str | Unset):
        thumbnail_url (None | str | Unset):
        bbox (list[float] | None | Unset):
    """

    ref: str
    kind: DossierSectionsImageryItemsItemKind
    title: str | Unset = UNSET
    time: datetime.datetime | None | Unset = UNSET
    source_id: None | str | Unset = UNSET
    frame_url: None | str | Unset = UNSET
    thumbnail_url: None | str | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        kind = self.kind.value

        title = self.title

        time: None | str | Unset
        if isinstance(self.time, Unset):
            time = UNSET
        elif isinstance(self.time, datetime.datetime):
            time = self.time.isoformat()
        else:
            time = self.time

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        frame_url: None | str | Unset
        if isinstance(self.frame_url, Unset):
            frame_url = UNSET
        else:
            frame_url = self.frame_url

        thumbnail_url: None | str | Unset
        if isinstance(self.thumbnail_url, Unset):
            thumbnail_url = UNSET
        else:
            thumbnail_url = self.thumbnail_url

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "kind": kind,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if time is not UNSET:
            field_dict["time"] = time
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if frame_url is not UNSET:
            field_dict["frameUrl"] = frame_url
        if thumbnail_url is not UNSET:
            field_dict["thumbnailUrl"] = thumbnail_url
        if bbox is not UNSET:
            field_dict["bbox"] = bbox

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        kind = DossierSectionsImageryItemsItemKind(d.pop("kind"))

        title = d.pop("title", UNSET)

        def _parse_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                time_type_0 = datetime.datetime.fromisoformat(data)

                return time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        time = _parse_time(d.pop("time", UNSET))

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        def _parse_frame_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_url = _parse_frame_url(d.pop("frameUrl", UNSET))

        def _parse_thumbnail_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        thumbnail_url = _parse_thumbnail_url(d.pop("thumbnailUrl", UNSET))

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        dossier_sections_imagery_items_item = cls(
            ref=ref,
            kind=kind,
            title=title,
            time=time,
            source_id=source_id,
            frame_url=frame_url,
            thumbnail_url=thumbnail_url,
            bbox=bbox,
        )

        dossier_sections_imagery_items_item.additional_properties = d
        return dossier_sections_imagery_items_item

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
