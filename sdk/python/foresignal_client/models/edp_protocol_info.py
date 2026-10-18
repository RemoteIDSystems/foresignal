from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_protocol_info_modes_item import EdpProtocolInfoModesItem
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpProtocolInfo")


@_attrs_define
class EdpProtocolInfo:
    """EDP protocol description (024)

    Attributes:
        protocol (str):  Example: foresignal-edp/1.0.
        item_types (list[str]):
        modes (list[EdpProtocolInfoModesItem]):
        supported (list[str] | Unset):  Example: ['foresignal-edp/1.0'].
        schemas (list[str] | Unset):
        documentation (str | Unset):
        max_minor (int | Unset): Highest 1.x minor version accepted
    """

    protocol: str
    item_types: list[str]
    modes: list[EdpProtocolInfoModesItem]
    supported: list[str] | Unset = UNSET
    schemas: list[str] | Unset = UNSET
    documentation: str | Unset = UNSET
    max_minor: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        protocol = self.protocol

        item_types = self.item_types

        modes = []
        for modes_item_data in self.modes:
            modes_item = modes_item_data.value
            modes.append(modes_item)

        supported: list[str] | Unset = UNSET
        if not isinstance(self.supported, Unset):
            supported = self.supported

        schemas: list[str] | Unset = UNSET
        if not isinstance(self.schemas, Unset):
            schemas = self.schemas

        documentation = self.documentation

        max_minor = self.max_minor

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "protocol": protocol,
                "itemTypes": item_types,
                "modes": modes,
            }
        )
        if supported is not UNSET:
            field_dict["supported"] = supported
        if schemas is not UNSET:
            field_dict["schemas"] = schemas
        if documentation is not UNSET:
            field_dict["documentation"] = documentation
        if max_minor is not UNSET:
            field_dict["maxMinor"] = max_minor

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        protocol = d.pop("protocol")

        item_types = cast(list[str], d.pop("itemTypes"))

        modes = []
        _modes = d.pop("modes")
        for modes_item_data in _modes:
            modes_item = EdpProtocolInfoModesItem(modes_item_data)

            modes.append(modes_item)

        supported = cast(list[str], d.pop("supported", UNSET))

        schemas = cast(list[str], d.pop("schemas", UNSET))

        documentation = d.pop("documentation", UNSET)

        max_minor = d.pop("maxMinor", UNSET)

        edp_protocol_info = cls(
            protocol=protocol,
            item_types=item_types,
            modes=modes,
            supported=supported,
            schemas=schemas,
            documentation=documentation,
            max_minor=max_minor,
        )

        edp_protocol_info.additional_properties = d
        return edp_protocol_info

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
