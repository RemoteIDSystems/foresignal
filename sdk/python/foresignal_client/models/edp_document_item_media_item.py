from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpDocumentItemMediaItem")


@_attrs_define
class EdpDocumentItemMediaItem:
    """
    Attributes:
        url (str):
        type_ (str | Unset):
        thumb_url (None | str | Unset):
    """

    url: str
    type_: str | Unset = UNSET
    thumb_url: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        url = self.url

        type_ = self.type_

        thumb_url: None | str | Unset
        if isinstance(self.thumb_url, Unset):
            thumb_url = UNSET
        else:
            thumb_url = self.thumb_url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "url": url,
            }
        )
        if type_ is not UNSET:
            field_dict["type"] = type_
        if thumb_url is not UNSET:
            field_dict["thumbUrl"] = thumb_url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        url = d.pop("url")

        type_ = d.pop("type", UNSET)

        def _parse_thumb_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        thumb_url = _parse_thumb_url(d.pop("thumbUrl", UNSET))

        edp_document_item_media_item = cls(
            url=url,
            type_=type_,
            thumb_url=thumb_url,
        )

        edp_document_item_media_item.additional_properties = d
        return edp_document_item_media_item

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
