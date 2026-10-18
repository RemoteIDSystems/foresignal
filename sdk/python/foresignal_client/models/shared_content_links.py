from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SharedContentLinks")


@_attrs_define
class SharedContentLinks:
    """Sub-resources available for this target

    Attributes:
        geojson (None | str | Unset):
        tiles (None | str | Unset): Tile URL template (views and tracks)
        download (None | str | Unset): Exports only
    """

    geojson: None | str | Unset = UNSET
    tiles: None | str | Unset = UNSET
    download: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        geojson: None | str | Unset
        if isinstance(self.geojson, Unset):
            geojson = UNSET
        else:
            geojson = self.geojson

        tiles: None | str | Unset
        if isinstance(self.tiles, Unset):
            tiles = UNSET
        else:
            tiles = self.tiles

        download: None | str | Unset
        if isinstance(self.download, Unset):
            download = UNSET
        else:
            download = self.download

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if geojson is not UNSET:
            field_dict["geojson"] = geojson
        if tiles is not UNSET:
            field_dict["tiles"] = tiles
        if download is not UNSET:
            field_dict["download"] = download

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_geojson(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        geojson = _parse_geojson(d.pop("geojson", UNSET))

        def _parse_tiles(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tiles = _parse_tiles(d.pop("tiles", UNSET))

        def _parse_download(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        download = _parse_download(d.pop("download", UNSET))

        shared_content_links = cls(
            geojson=geojson,
            tiles=tiles,
            download=download,
        )

        shared_content_links.additional_properties = d
        return shared_content_links

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
