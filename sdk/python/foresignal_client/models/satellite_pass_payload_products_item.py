from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SatellitePassPayloadProductsItem")


@_attrs_define
class SatellitePassPayloadProductsItem:
    """Decoded product

    Attributes:
        composite (str):
        product_id (str | Unset): The polar overlay product `satdump-{station}-{composite}` (054); present when the
            composite has a GeoTIFF overlay
        frame_key (None | str | Unset): The overlay frame's content-addressed key (054)
        thumb_media_id (None | str | Unset):
        tiles_template (None | str | Unset): Content-addressed tile template (054 polar overlays)
        instrument (str | Unset):
        media_id (None | str | Unset):
    """

    composite: str
    product_id: str | Unset = UNSET
    frame_key: None | str | Unset = UNSET
    thumb_media_id: None | str | Unset = UNSET
    tiles_template: None | str | Unset = UNSET
    instrument: str | Unset = UNSET
    media_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        composite = self.composite

        product_id = self.product_id

        frame_key: None | str | Unset
        if isinstance(self.frame_key, Unset):
            frame_key = UNSET
        else:
            frame_key = self.frame_key

        thumb_media_id: None | str | Unset
        if isinstance(self.thumb_media_id, Unset):
            thumb_media_id = UNSET
        else:
            thumb_media_id = self.thumb_media_id

        tiles_template: None | str | Unset
        if isinstance(self.tiles_template, Unset):
            tiles_template = UNSET
        else:
            tiles_template = self.tiles_template

        instrument = self.instrument

        media_id: None | str | Unset
        if isinstance(self.media_id, Unset):
            media_id = UNSET
        else:
            media_id = self.media_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "composite": composite,
            }
        )
        if product_id is not UNSET:
            field_dict["productId"] = product_id
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if thumb_media_id is not UNSET:
            field_dict["thumbMediaId"] = thumb_media_id
        if tiles_template is not UNSET:
            field_dict["tilesTemplate"] = tiles_template
        if instrument is not UNSET:
            field_dict["instrument"] = instrument
        if media_id is not UNSET:
            field_dict["mediaId"] = media_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        composite = d.pop("composite")

        product_id = d.pop("productId", UNSET)

        def _parse_frame_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_key = _parse_frame_key(d.pop("frameKey", UNSET))

        def _parse_thumb_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        thumb_media_id = _parse_thumb_media_id(d.pop("thumbMediaId", UNSET))

        def _parse_tiles_template(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tiles_template = _parse_tiles_template(d.pop("tilesTemplate", UNSET))

        instrument = d.pop("instrument", UNSET)

        def _parse_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        media_id = _parse_media_id(d.pop("mediaId", UNSET))

        satellite_pass_payload_products_item = cls(
            composite=composite,
            product_id=product_id,
            frame_key=frame_key,
            thumb_media_id=thumb_media_id,
            tiles_template=tiles_template,
            instrument=instrument,
            media_id=media_id,
        )

        satellite_pass_payload_products_item.additional_properties = d
        return satellite_pass_payload_products_item

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
