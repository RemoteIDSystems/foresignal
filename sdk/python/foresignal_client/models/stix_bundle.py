from __future__ import annotations

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

if TYPE_CHECKING:
    from ..models.stix_bundle_objects_item import StixBundleObjectsItem


T = TypeVar("T", bound="StixBundle")


@_attrs_define
class StixBundle:
    """STIX 2.1 bundle (vulnerability, indicator and identity objects with deterministic UUIDv5 ids)

    Attributes:
        type_ (Literal['bundle']):
        id (str):
        objects (list[StixBundleObjectsItem]):
    """

    type_: Literal["bundle"]
    id: str
    objects: list[StixBundleObjectsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        id = self.id

        objects = []
        for objects_item_data in self.objects:
            objects_item = objects_item_data.to_dict()
            objects.append(objects_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "id": id,
                "objects": objects,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stix_bundle_objects_item import StixBundleObjectsItem

        d = dict(src_dict)
        type_ = cast(Literal["bundle"], d.pop("type"))
        if type_ != "bundle":
            raise ValueError(f"type must match const 'bundle', got '{type_}'")

        id = d.pop("id")

        objects = []
        _objects = d.pop("objects")
        for objects_item_data in _objects:
            objects_item = StixBundleObjectsItem.from_dict(objects_item_data)

            objects.append(objects_item)

        stix_bundle = cls(
            type_=type_,
            id=id,
            objects=objects,
        )

        stix_bundle.additional_properties = d
        return stix_bundle

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
