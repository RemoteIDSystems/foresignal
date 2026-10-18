from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PackSpecRasterType0")


@_attrs_define
class PackSpecRasterType0:
    """Raster (PNG) product frame

    Attributes:
        product_id (str):
        frame (str | Unset): `latest` or a frame id Default: 'latest'.
    """

    product_id: str
    frame: str | Unset = "latest"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        frame = self.frame

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
            }
        )
        if frame is not UNSET:
            field_dict["frame"] = frame

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        product_id = d.pop("productId")

        frame = d.pop("frame", UNSET)

        pack_spec_raster_type_0 = cls(
            product_id=product_id,
            frame=frame,
        )

        pack_spec_raster_type_0.additional_properties = d
        return pack_spec_raster_type_0

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
