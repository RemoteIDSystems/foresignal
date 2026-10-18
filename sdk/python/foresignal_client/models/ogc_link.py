from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OgcLink")


@_attrs_define
class OgcLink:
    """OGC API link

    Attributes:
        href (str):
        rel (str):
        type_ (str | Unset):
        title (str | Unset):
        templated (bool | Unset):
    """

    href: str
    rel: str
    type_: str | Unset = UNSET
    title: str | Unset = UNSET
    templated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        href = self.href

        rel = self.rel

        type_ = self.type_

        title = self.title

        templated = self.templated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "href": href,
                "rel": rel,
            }
        )
        if type_ is not UNSET:
            field_dict["type"] = type_
        if title is not UNSET:
            field_dict["title"] = title
        if templated is not UNSET:
            field_dict["templated"] = templated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        href = d.pop("href")

        rel = d.pop("rel")

        type_ = d.pop("type", UNSET)

        title = d.pop("title", UNSET)

        templated = d.pop("templated", UNSET)

        ogc_link = cls(
            href=href,
            rel=rel,
            type_=type_,
            title=title,
            templated=templated,
        )

        ogc_link.additional_properties = d
        return ogc_link

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
