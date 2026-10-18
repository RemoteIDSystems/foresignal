from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edr_instance_list_instances_item_extent import (
        EdrInstanceListInstancesItemExtent,
    )
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="EdrInstanceListInstancesItem")


@_attrs_define
class EdrInstanceListInstancesItem:
    """One run

    Attributes:
        id (str): Run time, ISO extended
        links (list[OgcLink]):
        title (str | Unset):
        extent (EdrInstanceListInstancesItemExtent | Unset):
    """

    id: str
    links: list[OgcLink]
    title: str | Unset = UNSET
    extent: EdrInstanceListInstancesItemExtent | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        title = self.title

        extent: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extent, Unset):
            extent = self.extent.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "links": links,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if extent is not UNSET:
            field_dict["extent"] = extent

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edr_instance_list_instances_item_extent import (
            EdrInstanceListInstancesItemExtent,
        )
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        id = d.pop("id")

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        title = d.pop("title", UNSET)

        _extent = d.pop("extent", UNSET)
        extent: EdrInstanceListInstancesItemExtent | Unset
        if isinstance(_extent, Unset):
            extent = UNSET
        else:
            extent = EdrInstanceListInstancesItemExtent.from_dict(_extent)

        edr_instance_list_instances_item = cls(
            id=id,
            links=links,
            title=title,
            extent=extent,
        )

        edr_instance_list_instances_item.additional_properties = d
        return edr_instance_list_instances_item

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
