from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.ogc_collection import OgcCollection
    from ..models.ogc_coverage_collection import OgcCoverageCollection
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcCollections")


@_attrs_define
class OgcCollections:
    """Collections list (112)

    Attributes:
        links (list[OgcLink]):
        collections (list[OgcCollection | OgcCoverageCollection]):
    """

    links: list[OgcLink]
    collections: list[OgcCollection | OgcCoverageCollection]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ogc_collection import OgcCollection

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        collections = []
        for collections_item_data in self.collections:
            collections_item: dict[str, Any]
            if isinstance(collections_item_data, OgcCollection):
                collections_item = collections_item_data.to_dict()
            else:
                collections_item = collections_item_data.to_dict()

            collections.append(collections_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "links": links,
                "collections": collections,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_collection import OgcCollection
        from ..models.ogc_coverage_collection import OgcCoverageCollection
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        collections = []
        _collections = d.pop("collections")
        for collections_item_data in _collections:

            def _parse_collections_item(
                data: object,
            ) -> OgcCollection | OgcCoverageCollection:
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    collections_item_type_0 = OgcCollection.from_dict(data)

                    return collections_item_type_0
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                if not isinstance(data, dict):
                    raise TypeError()
                collections_item_type_1 = OgcCoverageCollection.from_dict(data)

                return collections_item_type_1

            collections_item = _parse_collections_item(collections_item_data)

            collections.append(collections_item)

        ogc_collections = cls(
            links=links,
            collections=collections,
        )

        ogc_collections.additional_properties = d
        return ogc_collections

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
