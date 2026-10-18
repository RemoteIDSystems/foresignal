from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaLinks")


@_attrs_define
class MediaLinks:
    """Expanded links (documents have no thumbnail links)

    Attributes:
        self_ (str | Unset):
        meta (str | Unset):
        thumbnail256 (str | Unset):
        thumbnail1024 (str | Unset):
    """

    self_: str | Unset = UNSET
    meta: str | Unset = UNSET
    thumbnail256: str | Unset = UNSET
    thumbnail1024: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        self_ = self.self_

        meta = self.meta

        thumbnail256 = self.thumbnail256

        thumbnail1024 = self.thumbnail1024

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if self_ is not UNSET:
            field_dict["self"] = self_
        if meta is not UNSET:
            field_dict["meta"] = meta
        if thumbnail256 is not UNSET:
            field_dict["thumbnail256"] = thumbnail256
        if thumbnail1024 is not UNSET:
            field_dict["thumbnail1024"] = thumbnail1024

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        self_ = d.pop("self", UNSET)

        meta = d.pop("meta", UNSET)

        thumbnail256 = d.pop("thumbnail256", UNSET)

        thumbnail1024 = d.pop("thumbnail1024", UNSET)

        media_links = cls(
            self_=self_,
            meta=meta,
            thumbnail256=thumbnail256,
            thumbnail1024=thumbnail1024,
        )

        media_links.additional_properties = d
        return media_links

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
