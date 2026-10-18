from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="CollabAttachmentImageMetaType0")


@_attrs_define
class CollabAttachmentImageMetaType0:
    """Image metadata

    Attributes:
        width (int):
        height (int):
        exif_stripped (bool):
    """

    width: int
    height: int
    exif_stripped: bool
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        width = self.width

        height = self.height

        exif_stripped = self.exif_stripped

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "width": width,
                "height": height,
                "exifStripped": exif_stripped,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        width = d.pop("width")

        height = d.pop("height")

        exif_stripped = d.pop("exifStripped")

        collab_attachment_image_meta_type_0 = cls(
            width=width,
            height=height,
            exif_stripped=exif_stripped,
        )

        collab_attachment_image_meta_type_0.additional_properties = d
        return collab_attachment_image_meta_type_0

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
