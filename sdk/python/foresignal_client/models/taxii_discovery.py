from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TaxiiDiscovery")


@_attrs_define
class TaxiiDiscovery:
    """TAXII 2.1 discovery resource (member names per TAXII)

    Attributes:
        title (str):
        description (None | str | Unset):
        contact (None | str | Unset):
        default (None | str | Unset):
        api_roots (list[str] | Unset):
    """

    title: str
    description: None | str | Unset = UNSET
    contact: None | str | Unset = UNSET
    default: None | str | Unset = UNSET
    api_roots: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        contact: None | str | Unset
        if isinstance(self.contact, Unset):
            contact = UNSET
        else:
            contact = self.contact

        default: None | str | Unset
        if isinstance(self.default, Unset):
            default = UNSET
        else:
            default = self.default

        api_roots: list[str] | Unset = UNSET
        if not isinstance(self.api_roots, Unset):
            api_roots = self.api_roots

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "title": title,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if contact is not UNSET:
            field_dict["contact"] = contact
        if default is not UNSET:
            field_dict["default"] = default
        if api_roots is not UNSET:
            field_dict["api_roots"] = api_roots

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        title = d.pop("title")

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_contact(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        contact = _parse_contact(d.pop("contact", UNSET))

        def _parse_default(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        default = _parse_default(d.pop("default", UNSET))

        api_roots = cast(list[str], d.pop("api_roots", UNSET))

        taxii_discovery = cls(
            title=title,
            description=description,
            contact=contact,
            default=default,
            api_roots=api_roots,
        )

        taxii_discovery.additional_properties = d
        return taxii_discovery

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
