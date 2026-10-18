from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ogc_collection_item_type import OgcCollectionItemType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_collection_extent import OgcCollectionExtent
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcCollection")


@_attrs_define
class OgcCollection:
    """A feature collection: one per kind for current state (id = kind) and one per kind for history (id = {kind}-history)
    (112)

        Attributes:
            id (str):
            item_type (OgcCollectionItemType):
            links (list[OgcLink]):
            title (str | Unset):
            description (None | str | Unset):
            crs (list[str] | Unset):
            attribution (None | str | Unset): 109 aggregate for the collection's sources
            extent (OgcCollectionExtent | Unset): Spatial and temporal extent
    """

    id: str
    item_type: OgcCollectionItemType
    links: list[OgcLink]
    title: str | Unset = UNSET
    description: None | str | Unset = UNSET
    crs: list[str] | Unset = UNSET
    attribution: None | str | Unset = UNSET
    extent: OgcCollectionExtent | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        item_type = self.item_type.value

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        title = self.title

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        crs: list[str] | Unset = UNSET
        if not isinstance(self.crs, Unset):
            crs = self.crs

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        extent: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extent, Unset):
            extent = self.extent.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "itemType": item_type,
                "links": links,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if description is not UNSET:
            field_dict["description"] = description
        if crs is not UNSET:
            field_dict["crs"] = crs
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if extent is not UNSET:
            field_dict["extent"] = extent

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_collection_extent import OgcCollectionExtent
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        id = d.pop("id")

        item_type = OgcCollectionItemType(d.pop("itemType"))

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        title = d.pop("title", UNSET)

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        crs = cast(list[str], d.pop("crs", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        _extent = d.pop("extent", UNSET)
        extent: OgcCollectionExtent | Unset
        if isinstance(_extent, Unset):
            extent = UNSET
        else:
            extent = OgcCollectionExtent.from_dict(_extent)

        ogc_collection = cls(
            id=id,
            item_type=item_type,
            links=links,
            title=title,
            description=description,
            crs=crs,
            attribution=attribution,
            extent=extent,
        )

        ogc_collection.additional_properties = d
        return ogc_collection

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
