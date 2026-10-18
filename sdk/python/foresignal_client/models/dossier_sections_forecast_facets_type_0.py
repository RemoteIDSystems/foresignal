from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_forecast_facets_type_0_by_evidence_class import (
        DossierSectionsForecastFacetsType0ByEvidenceClass,
    )
    from ..models.dossier_sections_forecast_facets_type_0_by_source import (
        DossierSectionsForecastFacetsType0BySource,
    )


T = TypeVar("T", bound="DossierSectionsForecastFacetsType0")


@_attrs_define
class DossierSectionsForecastFacetsType0:
    """Evidence counts by class and by source

    Attributes:
        by_evidence_class (DossierSectionsForecastFacetsType0ByEvidenceClass | Unset): imagery | signals | reports |
            documents | sensor | social | other
        by_source (DossierSectionsForecastFacetsType0BySource | Unset):
    """

    by_evidence_class: DossierSectionsForecastFacetsType0ByEvidenceClass | Unset = UNSET
    by_source: DossierSectionsForecastFacetsType0BySource | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        by_evidence_class: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_evidence_class, Unset):
            by_evidence_class = self.by_evidence_class.to_dict()

        by_source: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_source, Unset):
            by_source = self.by_source.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if by_evidence_class is not UNSET:
            field_dict["byEvidenceClass"] = by_evidence_class
        if by_source is not UNSET:
            field_dict["bySource"] = by_source

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_forecast_facets_type_0_by_evidence_class import (
            DossierSectionsForecastFacetsType0ByEvidenceClass,
        )
        from ..models.dossier_sections_forecast_facets_type_0_by_source import (
            DossierSectionsForecastFacetsType0BySource,
        )

        d = dict(src_dict)
        _by_evidence_class = d.pop("byEvidenceClass", UNSET)
        by_evidence_class: DossierSectionsForecastFacetsType0ByEvidenceClass | Unset
        if isinstance(_by_evidence_class, Unset):
            by_evidence_class = UNSET
        else:
            by_evidence_class = (
                DossierSectionsForecastFacetsType0ByEvidenceClass.from_dict(
                    _by_evidence_class
                )
            )

        _by_source = d.pop("bySource", UNSET)
        by_source: DossierSectionsForecastFacetsType0BySource | Unset
        if isinstance(_by_source, Unset):
            by_source = UNSET
        else:
            by_source = DossierSectionsForecastFacetsType0BySource.from_dict(_by_source)

        dossier_sections_forecast_facets_type_0 = cls(
            by_evidence_class=by_evidence_class,
            by_source=by_source,
        )

        dossier_sections_forecast_facets_type_0.additional_properties = d
        return dossier_sections_forecast_facets_type_0

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
