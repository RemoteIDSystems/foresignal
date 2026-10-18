from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.product_category import ProductCategory
from ..models.product_coverage import ProductCoverage
from ..models.product_dtype_type_1 import ProductDtypeType1
from ..models.product_dtype_type_2_type_1 import ProductDtypeType2Type1
from ..models.product_dtype_type_3_type_1 import ProductDtypeType3Type1
from ..models.product_kind import ProductKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.external_catalogue_entry import ExternalCatalogueEntry
    from ..models.product_codes import ProductCodes
    from ..models.product_legend import ProductLegend
    from ..models.product_links import ProductLinks
    from ..models.product_render import ProductRender
    from ..models.product_spatial import ProductSpatial
    from ..models.product_status import ProductStatus
    from ..models.product_temporal import ProductTemporal


T = TypeVar("T", bound="Product")


@_attrs_define
class Product:
    """A raster, field or external product in the catalogue (Weather-Imagery-Design §6.2); `compact=true` lists carry id,
    title, kind, category, source, coverage, units, temporal and status only

        Attributes:
            id (str):  Example: mrms-reflectivity.
            title (str):  Example: MRMS composite reflectivity.
            kind (ProductKind): raster: rendered tiles; field: model vector/scalar field (also tiles); external: client-
                direct catalogue entry
            category (ProductCategory):
            source (str): Owning source id Example: mrms.
            dynamic (bool | Unset): Registered at runtime (SatDump products)
            units (None | str | Unset):  Example: dBZ.
            dtype (None | ProductDtypeType1 | ProductDtypeType2Type1 | ProductDtypeType3Type1 | Unset): Stored sample type
            coverage (ProductCoverage | Unset):
            coverage_meaning (str | Unset): One-line meaning of the coverage Example: Contiguous US radar mosaic; outside it
                values are no-coverage, not clear weather.
            codes (ProductCodes | Unset): Categorical and special raw codes → meaning, e.g. {"0": "noCoverage", "1":
                "noEcho"}
            temporal (ProductTemporal | Unset): Temporal facts of a product: frame type, cadence, gap tolerance, available
                range, retention and runs
            spatial (ProductSpatial | Unset): Spatial facts of a product for map clients
            render (ProductRender | Unset): Rendering defaults
            legend (ProductLegend | Unset): Legend block and links
            attribution (Attribution | Unset): Structured attribution clients must render for a product or derived output
                (Weather-Imagery-Design §6.10)
            status (ProductStatus | Unset): Current product health
            external (ExternalCatalogueEntry | None | Unset):
            links (ProductLinks | Unset): Fully expanded links (clients never build URLs)
    """

    id: str
    title: str
    kind: ProductKind
    category: ProductCategory
    source: str
    dynamic: bool | Unset = UNSET
    units: None | str | Unset = UNSET
    dtype: (
        None
        | ProductDtypeType1
        | ProductDtypeType2Type1
        | ProductDtypeType3Type1
        | Unset
    ) = UNSET
    coverage: ProductCoverage | Unset = UNSET
    coverage_meaning: str | Unset = UNSET
    codes: ProductCodes | Unset = UNSET
    temporal: ProductTemporal | Unset = UNSET
    spatial: ProductSpatial | Unset = UNSET
    render: ProductRender | Unset = UNSET
    legend: ProductLegend | Unset = UNSET
    attribution: Attribution | Unset = UNSET
    status: ProductStatus | Unset = UNSET
    external: ExternalCatalogueEntry | None | Unset = UNSET
    links: ProductLinks | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.external_catalogue_entry import ExternalCatalogueEntry

        id = self.id

        title = self.title

        kind = self.kind.value

        category = self.category.value

        source = self.source

        dynamic = self.dynamic

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        dtype: None | str | Unset
        if isinstance(self.dtype, Unset):
            dtype = UNSET
        elif (
            isinstance(self.dtype, ProductDtypeType1)
            or isinstance(self.dtype, ProductDtypeType2Type1)
            or isinstance(self.dtype, ProductDtypeType3Type1)
        ):
            dtype = self.dtype.value
        else:
            dtype = self.dtype

        coverage: str | Unset = UNSET
        if not isinstance(self.coverage, Unset):
            coverage = self.coverage.value

        coverage_meaning = self.coverage_meaning

        codes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.codes, Unset):
            codes = self.codes.to_dict()

        temporal: dict[str, Any] | Unset = UNSET
        if not isinstance(self.temporal, Unset):
            temporal = self.temporal.to_dict()

        spatial: dict[str, Any] | Unset = UNSET
        if not isinstance(self.spatial, Unset):
            spatial = self.spatial.to_dict()

        render: dict[str, Any] | Unset = UNSET
        if not isinstance(self.render, Unset):
            render = self.render.to_dict()

        legend: dict[str, Any] | Unset = UNSET
        if not isinstance(self.legend, Unset):
            legend = self.legend.to_dict()

        attribution: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = self.attribution.to_dict()

        status: dict[str, Any] | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.to_dict()

        external: dict[str, Any] | None | Unset
        if isinstance(self.external, Unset):
            external = UNSET
        elif isinstance(self.external, ExternalCatalogueEntry):
            external = self.external.to_dict()
        else:
            external = self.external

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "kind": kind,
                "category": category,
                "source": source,
            }
        )
        if dynamic is not UNSET:
            field_dict["dynamic"] = dynamic
        if units is not UNSET:
            field_dict["units"] = units
        if dtype is not UNSET:
            field_dict["dtype"] = dtype
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if coverage_meaning is not UNSET:
            field_dict["coverageMeaning"] = coverage_meaning
        if codes is not UNSET:
            field_dict["codes"] = codes
        if temporal is not UNSET:
            field_dict["temporal"] = temporal
        if spatial is not UNSET:
            field_dict["spatial"] = spatial
        if render is not UNSET:
            field_dict["render"] = render
        if legend is not UNSET:
            field_dict["legend"] = legend
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if status is not UNSET:
            field_dict["status"] = status
        if external is not UNSET:
            field_dict["external"] = external
        if links is not UNSET:
            field_dict["links"] = links

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.external_catalogue_entry import ExternalCatalogueEntry
        from ..models.product_codes import ProductCodes
        from ..models.product_legend import ProductLegend
        from ..models.product_links import ProductLinks
        from ..models.product_render import ProductRender
        from ..models.product_spatial import ProductSpatial
        from ..models.product_status import ProductStatus
        from ..models.product_temporal import ProductTemporal

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        kind = ProductKind(d.pop("kind"))

        category = ProductCategory(d.pop("category"))

        source = d.pop("source")

        dynamic = d.pop("dynamic", UNSET)

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        def _parse_dtype(
            data: object,
        ) -> (
            None
            | ProductDtypeType1
            | ProductDtypeType2Type1
            | ProductDtypeType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                dtype_type_1 = ProductDtypeType1(data)

                return dtype_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                dtype_type_2_type_1 = ProductDtypeType2Type1(data)

                return dtype_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                dtype_type_3_type_1 = ProductDtypeType3Type1(data)

                return dtype_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | ProductDtypeType1
                | ProductDtypeType2Type1
                | ProductDtypeType3Type1
                | Unset,
                data,
            )

        dtype = _parse_dtype(d.pop("dtype", UNSET))

        _coverage = d.pop("coverage", UNSET)
        coverage: ProductCoverage | Unset
        if isinstance(_coverage, Unset):
            coverage = UNSET
        else:
            coverage = ProductCoverage(_coverage)

        coverage_meaning = d.pop("coverageMeaning", UNSET)

        _codes = d.pop("codes", UNSET)
        codes: ProductCodes | Unset
        if isinstance(_codes, Unset):
            codes = UNSET
        else:
            codes = ProductCodes.from_dict(_codes)

        _temporal = d.pop("temporal", UNSET)
        temporal: ProductTemporal | Unset
        if isinstance(_temporal, Unset):
            temporal = UNSET
        else:
            temporal = ProductTemporal.from_dict(_temporal)

        _spatial = d.pop("spatial", UNSET)
        spatial: ProductSpatial | Unset
        if isinstance(_spatial, Unset):
            spatial = UNSET
        else:
            spatial = ProductSpatial.from_dict(_spatial)

        _render = d.pop("render", UNSET)
        render: ProductRender | Unset
        if isinstance(_render, Unset):
            render = UNSET
        else:
            render = ProductRender.from_dict(_render)

        _legend = d.pop("legend", UNSET)
        legend: ProductLegend | Unset
        if isinstance(_legend, Unset):
            legend = UNSET
        else:
            legend = ProductLegend.from_dict(_legend)

        _attribution = d.pop("attribution", UNSET)
        attribution: Attribution | Unset
        if isinstance(_attribution, Unset):
            attribution = UNSET
        else:
            attribution = Attribution.from_dict(_attribution)

        _status = d.pop("status", UNSET)
        status: ProductStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = ProductStatus.from_dict(_status)

        def _parse_external(data: object) -> ExternalCatalogueEntry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                external_type_0 = ExternalCatalogueEntry.from_dict(data)

                return external_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ExternalCatalogueEntry | None | Unset, data)

        external = _parse_external(d.pop("external", UNSET))

        _links = d.pop("links", UNSET)
        links: ProductLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = ProductLinks.from_dict(_links)

        product = cls(
            id=id,
            title=title,
            kind=kind,
            category=category,
            source=source,
            dynamic=dynamic,
            units=units,
            dtype=dtype,
            coverage=coverage,
            coverage_meaning=coverage_meaning,
            codes=codes,
            temporal=temporal,
            spatial=spatial,
            render=render,
            legend=legend,
            attribution=attribution,
            status=status,
            external=external,
            links=links,
        )

        product.additional_properties = d
        return product

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
