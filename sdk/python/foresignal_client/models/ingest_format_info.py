from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ingest_format_info_methods_item import IngestFormatInfoMethodsItem
from ..models.ingest_format_info_verifiers_item import IngestFormatInfoVerifiersItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ingest_format_option import IngestFormatOption


T = TypeVar("T", bound="IngestFormatInfo")


@_attrs_define
class IngestFormatInfo:
    """A registered ingest format (024)

    Attributes:
        id (str):  Example: alertmanager.
        media_types (list[str]):
        description (str | Unset):
        methods (list[IngestFormatInfoMethodsItem] | Unset):
        options (list[IngestFormatOption] | Unset):
        verifiers (list[IngestFormatInfoVerifiersItem] | Unset):
        reserved (bool | Unset): Name reserved for a later prompt (media, rasters: 054; mapping-preview: 059)
    """

    id: str
    media_types: list[str]
    description: str | Unset = UNSET
    methods: list[IngestFormatInfoMethodsItem] | Unset = UNSET
    options: list[IngestFormatOption] | Unset = UNSET
    verifiers: list[IngestFormatInfoVerifiersItem] | Unset = UNSET
    reserved: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        media_types = self.media_types

        description = self.description

        methods: list[str] | Unset = UNSET
        if not isinstance(self.methods, Unset):
            methods = []
            for methods_item_data in self.methods:
                methods_item = methods_item_data.value
                methods.append(methods_item)

        options: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.options, Unset):
            options = []
            for options_item_data in self.options:
                options_item = options_item_data.to_dict()
                options.append(options_item)

        verifiers: list[str] | Unset = UNSET
        if not isinstance(self.verifiers, Unset):
            verifiers = []
            for verifiers_item_data in self.verifiers:
                verifiers_item = verifiers_item_data.value
                verifiers.append(verifiers_item)

        reserved = self.reserved

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "mediaTypes": media_types,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if methods is not UNSET:
            field_dict["methods"] = methods
        if options is not UNSET:
            field_dict["options"] = options
        if verifiers is not UNSET:
            field_dict["verifiers"] = verifiers
        if reserved is not UNSET:
            field_dict["reserved"] = reserved

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ingest_format_option import IngestFormatOption

        d = dict(src_dict)
        id = d.pop("id")

        media_types = cast(list[str], d.pop("mediaTypes"))

        description = d.pop("description", UNSET)

        _methods = d.pop("methods", UNSET)
        methods: list[IngestFormatInfoMethodsItem] | Unset = UNSET
        if _methods is not UNSET:
            methods = []
            for methods_item_data in _methods:
                methods_item = IngestFormatInfoMethodsItem(methods_item_data)

                methods.append(methods_item)

        _options = d.pop("options", UNSET)
        options: list[IngestFormatOption] | Unset = UNSET
        if _options is not UNSET:
            options = []
            for options_item_data in _options:
                options_item = IngestFormatOption.from_dict(options_item_data)

                options.append(options_item)

        _verifiers = d.pop("verifiers", UNSET)
        verifiers: list[IngestFormatInfoVerifiersItem] | Unset = UNSET
        if _verifiers is not UNSET:
            verifiers = []
            for verifiers_item_data in _verifiers:
                verifiers_item = IngestFormatInfoVerifiersItem(verifiers_item_data)

                verifiers.append(verifiers_item)

        reserved = d.pop("reserved", UNSET)

        ingest_format_info = cls(
            id=id,
            media_types=media_types,
            description=description,
            methods=methods,
            options=options,
            verifiers=verifiers,
            reserved=reserved,
        )

        ingest_format_info.additional_properties = d
        return ingest_format_info

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
