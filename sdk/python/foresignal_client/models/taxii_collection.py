from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TaxiiCollection")


@_attrs_define
class TaxiiCollection:
    """TAXII 2.1 collection resource

    Attributes:
        id (str):
        title (str):
        can_read (bool):
        can_write (bool):
        description (None | str | Unset):
        media_types (list[str] | Unset):
    """

    id: str
    title: str
    can_read: bool
    can_write: bool
    description: None | str | Unset = UNSET
    media_types: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        title = self.title

        can_read = self.can_read

        can_write = self.can_write

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        media_types: list[str] | Unset = UNSET
        if not isinstance(self.media_types, Unset):
            media_types = self.media_types

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "can_read": can_read,
                "can_write": can_write,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if media_types is not UNSET:
            field_dict["media_types"] = media_types

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        can_read = d.pop("can_read")

        can_write = d.pop("can_write")

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        media_types = cast(list[str], d.pop("media_types", UNSET))

        taxii_collection = cls(
            id=id,
            title=title,
            can_read=can_read,
            can_write=can_write,
            description=description,
            media_types=media_types,
        )

        taxii_collection.additional_properties = d
        return taxii_collection

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
