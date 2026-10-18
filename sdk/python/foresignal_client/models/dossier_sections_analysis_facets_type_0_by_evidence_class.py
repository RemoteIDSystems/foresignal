from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="DossierSectionsAnalysisFacetsType0ByEvidenceClass")


@_attrs_define
class DossierSectionsAnalysisFacetsType0ByEvidenceClass:
    """imagery | signals | reports | documents | sensor | social | other"""

    additional_properties: dict[str, int] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        dossier_sections_analysis_facets_type_0_by_evidence_class = cls()

        dossier_sections_analysis_facets_type_0_by_evidence_class.additional_properties = d
        return dossier_sections_analysis_facets_type_0_by_evidence_class

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> int:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: int) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
