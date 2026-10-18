from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.map_attachment_config_attach_map import MapAttachmentConfigAttachMap
from ..types import UNSET, Unset

T = TypeVar("T", bound="MapAttachmentConfig")


@_attrs_define
class MapAttachmentConfig:
    """Map-attachment keys accepted in every channel's `config` (055)

    Attributes:
        attach_map (MapAttachmentConfigAttachMap | Unset): Attach a static map to geolocated alerts (cloud chat channels
            are opt-in) Default: MapAttachmentConfigAttachMap.NONE.
        map_products (list[str] | Unset): Products drawn (default ["mrms-reflectivity"] or the product that matched the
            rule)
        map_radius_km (float | Unset):  Default: 50.0.
        max_attachment_bytes (int | Unset): Per-type default: discord 8 MB, telegram 10 MB, ntfy 15 MB, pushover
            5,242,880, email 10 MB
    """

    attach_map: MapAttachmentConfigAttachMap | Unset = MapAttachmentConfigAttachMap.NONE
    map_products: list[str] | Unset = UNSET
    map_radius_km: float | Unset = 50.0
    max_attachment_bytes: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        attach_map: str | Unset = UNSET
        if not isinstance(self.attach_map, Unset):
            attach_map = self.attach_map.value

        map_products: list[str] | Unset = UNSET
        if not isinstance(self.map_products, Unset):
            map_products = self.map_products

        map_radius_km = self.map_radius_km

        max_attachment_bytes = self.max_attachment_bytes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if attach_map is not UNSET:
            field_dict["attachMap"] = attach_map
        if map_products is not UNSET:
            field_dict["mapProducts"] = map_products
        if map_radius_km is not UNSET:
            field_dict["mapRadiusKm"] = map_radius_km
        if max_attachment_bytes is not UNSET:
            field_dict["maxAttachmentBytes"] = max_attachment_bytes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _attach_map = d.pop("attachMap", UNSET)
        attach_map: MapAttachmentConfigAttachMap | Unset
        if isinstance(_attach_map, Unset):
            attach_map = UNSET
        else:
            attach_map = MapAttachmentConfigAttachMap(_attach_map)

        map_products = cast(list[str], d.pop("mapProducts", UNSET))

        map_radius_km = d.pop("mapRadiusKm", UNSET)

        max_attachment_bytes = d.pop("maxAttachmentBytes", UNSET)

        map_attachment_config = cls(
            attach_map=attach_map,
            map_products=map_products,
            map_radius_km=map_radius_km,
            max_attachment_bytes=max_attachment_bytes,
        )

        map_attachment_config.additional_properties = d
        return map_attachment_config

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
