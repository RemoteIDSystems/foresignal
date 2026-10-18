from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edr_instance_list_instances_item import EdrInstanceListInstancesItem
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="EdrInstanceList")


@_attrs_define
class EdrInstanceList:
    """EDR instances of a forecast product (one per run)

    Attributes:
        instances (list[EdrInstanceListInstancesItem]):
        links (list[OgcLink] | Unset):
    """

    instances: list[EdrInstanceListInstancesItem]
    links: list[OgcLink] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instances = []
        for instances_item_data in self.instances:
            instances_item = instances_item_data.to_dict()
            instances.append(instances_item)

        links: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = []
            for links_item_data in self.links:
                links_item = links_item_data.to_dict()
                links.append(links_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instances": instances,
            }
        )
        if links is not UNSET:
            field_dict["links"] = links

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edr_instance_list_instances_item import (
            EdrInstanceListInstancesItem,
        )
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        instances = []
        _instances = d.pop("instances")
        for instances_item_data in _instances:
            instances_item = EdrInstanceListInstancesItem.from_dict(instances_item_data)

            instances.append(instances_item)

        _links = d.pop("links", UNSET)
        links: list[OgcLink] | Unset = UNSET
        if _links is not UNSET:
            links = []
            for links_item_data in _links:
                links_item = OgcLink.from_dict(links_item_data)

                links.append(links_item)

        edr_instance_list = cls(
            instances=instances,
            links=links,
        )

        edr_instance_list.additional_properties = d
        return edr_instance_list

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
