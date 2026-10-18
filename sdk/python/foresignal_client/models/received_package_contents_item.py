from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ReceivedPackageContentsItem")


@_attrs_define
class ReceivedPackageContentsItem:
    """Stored content file

    Attributes:
        path (str):
        sha256 (str):
        size (int | Unset):
        media_id (None | str | Unset):
    """

    path: str
    sha256: str
    size: int | Unset = UNSET
    media_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        path = self.path

        sha256 = self.sha256

        size = self.size

        media_id: None | str | Unset
        if isinstance(self.media_id, Unset):
            media_id = UNSET
        else:
            media_id = self.media_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "path": path,
                "sha256": sha256,
            }
        )
        if size is not UNSET:
            field_dict["size"] = size
        if media_id is not UNSET:
            field_dict["mediaId"] = media_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = d.pop("path")

        sha256 = d.pop("sha256")

        size = d.pop("size", UNSET)

        def _parse_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        media_id = _parse_media_id(d.pop("mediaId", UNSET))

        received_package_contents_item = cls(
            path=path,
            sha256=sha256,
            size=size,
            media_id=media_id,
        )

        received_package_contents_item.additional_properties = d
        return received_package_contents_item

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
