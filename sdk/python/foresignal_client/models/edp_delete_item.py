from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_delete_item_properties import EdpDeleteItemProperties


T = TypeVar("T", bound="EdpDeleteItem")


@_attrs_define
class EdpDeleteItem:
    """Tombstone for {stream}:{id}; observedAt and properties are optional (kept for audit)

    Attributes:
        stream (str):
        type_ (Literal['delete']):
        id (str):
        observed_at (datetime.datetime | Unset):
        properties (EdpDeleteItemProperties | Unset): Item properties, validated against the stream's payloadSchema
    """

    stream: str
    type_: Literal["delete"]
    id: str
    observed_at: datetime.datetime | Unset = UNSET
    properties: EdpDeleteItemProperties | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream = self.stream

        type_ = self.type_

        id = self.id

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "stream": stream,
                "type": type_,
                "id": id,
            }
        )
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if properties is not UNSET:
            field_dict["properties"] = properties

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_delete_item_properties import EdpDeleteItemProperties

        d = dict(src_dict)
        stream = d.pop("stream")

        type_ = cast(Literal["delete"], d.pop("type"))
        if type_ != "delete":
            raise ValueError(f"type must match const 'delete', got '{type_}'")

        id = d.pop("id")

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _properties = d.pop("properties", UNSET)
        properties: EdpDeleteItemProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = EdpDeleteItemProperties.from_dict(_properties)

        edp_delete_item = cls(
            stream=stream,
            type_=type_,
            id=id,
            observed_at=observed_at,
            properties=properties,
        )

        edp_delete_item.additional_properties = d
        return edp_delete_item

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
