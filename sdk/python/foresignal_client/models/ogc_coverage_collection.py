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

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_coverage_collection_data_queries import (
        OgcCoverageCollectionDataQueries,
    )
    from ..models.ogc_coverage_collection_extent import OgcCoverageCollectionExtent
    from ..models.ogc_coverage_collection_parameter_names import (
        OgcCoverageCollectionParameterNames,
    )
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcCoverageCollection")


@_attrs_define
class OgcCoverageCollection:
    """A raster product exposed as an OGC API collection with map tiles (20-057) and EDR queries (19-086r6)

    Attributes:
        id (str): Product id
        item_type (Literal['coverage']):
        links (list[OgcLink]):
        title (str | Unset):
        description (None | str | Unset):
        extent (OgcCoverageCollectionExtent | Unset): Spatial and temporal extent
        data_queries (OgcCoverageCollectionDataQueries | Unset): EDR queries
        parameter_names (OgcCoverageCollectionParameterNames | Unset): EDR parameters with units and nodata
        output_formats (list[str] | Unset):  Example: ['CoverageJSON'].
        crs (list[str] | Unset):
        attribution (None | str | Unset):
    """

    id: str
    item_type: Literal["coverage"]
    links: list[OgcLink]
    title: str | Unset = UNSET
    description: None | str | Unset = UNSET
    extent: OgcCoverageCollectionExtent | Unset = UNSET
    data_queries: OgcCoverageCollectionDataQueries | Unset = UNSET
    parameter_names: OgcCoverageCollectionParameterNames | Unset = UNSET
    output_formats: list[str] | Unset = UNSET
    crs: list[str] | Unset = UNSET
    attribution: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        item_type = self.item_type

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

        extent: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extent, Unset):
            extent = self.extent.to_dict()

        data_queries: dict[str, Any] | Unset = UNSET
        if not isinstance(self.data_queries, Unset):
            data_queries = self.data_queries.to_dict()

        parameter_names: dict[str, Any] | Unset = UNSET
        if not isinstance(self.parameter_names, Unset):
            parameter_names = self.parameter_names.to_dict()

        output_formats: list[str] | Unset = UNSET
        if not isinstance(self.output_formats, Unset):
            output_formats = self.output_formats

        crs: list[str] | Unset = UNSET
        if not isinstance(self.crs, Unset):
            crs = self.crs

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

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
        if extent is not UNSET:
            field_dict["extent"] = extent
        if data_queries is not UNSET:
            field_dict["data_queries"] = data_queries
        if parameter_names is not UNSET:
            field_dict["parameter_names"] = parameter_names
        if output_formats is not UNSET:
            field_dict["output_formats"] = output_formats
        if crs is not UNSET:
            field_dict["crs"] = crs
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_coverage_collection_data_queries import (
            OgcCoverageCollectionDataQueries,
        )
        from ..models.ogc_coverage_collection_extent import OgcCoverageCollectionExtent
        from ..models.ogc_coverage_collection_parameter_names import (
            OgcCoverageCollectionParameterNames,
        )
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        id = d.pop("id")

        item_type = cast(Literal["coverage"], d.pop("itemType"))
        if item_type != "coverage":
            raise ValueError(f"itemType must match const 'coverage', got '{item_type}'")

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

        _extent = d.pop("extent", UNSET)
        extent: OgcCoverageCollectionExtent | Unset
        if isinstance(_extent, Unset):
            extent = UNSET
        else:
            extent = OgcCoverageCollectionExtent.from_dict(_extent)

        _data_queries = d.pop("data_queries", UNSET)
        data_queries: OgcCoverageCollectionDataQueries | Unset
        if isinstance(_data_queries, Unset):
            data_queries = UNSET
        else:
            data_queries = OgcCoverageCollectionDataQueries.from_dict(_data_queries)

        _parameter_names = d.pop("parameter_names", UNSET)
        parameter_names: OgcCoverageCollectionParameterNames | Unset
        if isinstance(_parameter_names, Unset):
            parameter_names = UNSET
        else:
            parameter_names = OgcCoverageCollectionParameterNames.from_dict(
                _parameter_names
            )

        output_formats = cast(list[str], d.pop("output_formats", UNSET))

        crs = cast(list[str], d.pop("crs", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        ogc_coverage_collection = cls(
            id=id,
            item_type=item_type,
            links=links,
            title=title,
            description=description,
            extent=extent,
            data_queries=data_queries,
            parameter_names=parameter_names,
            output_formats=output_formats,
            crs=crs,
            attribution=attribution,
        )

        ogc_coverage_collection.additional_properties = d
        return ogc_coverage_collection

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
