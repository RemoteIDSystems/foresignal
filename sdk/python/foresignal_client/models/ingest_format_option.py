from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ingest_format_option_type import IngestFormatOptionType
from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestFormatOption")


@_attrs_define
class IngestFormatOption:
    """One option a format accepts under `formats.{id}` (024)

    Attributes:
        name (str):
        type_ (IngestFormatOptionType):
        required (bool | Unset):
        default (Any | Unset):
        description (str | Unset):
    """

    name: str
    type_: IngestFormatOptionType
    required: bool | Unset = UNSET
    default: Any | Unset = UNSET
    description: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        type_ = self.type_.value

        required = self.required

        default = self.default

        description = self.description

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "type": type_,
            }
        )
        if required is not UNSET:
            field_dict["required"] = required
        if default is not UNSET:
            field_dict["default"] = default
        if description is not UNSET:
            field_dict["description"] = description

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        type_ = IngestFormatOptionType(d.pop("type"))

        required = d.pop("required", UNSET)

        default = d.pop("default", UNSET)

        description = d.pop("description", UNSET)

        ingest_format_option = cls(
            name=name,
            type_=type_,
            required=required,
            default=default,
            description=description,
        )

        ingest_format_option.additional_properties = d
        return ingest_format_option

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
