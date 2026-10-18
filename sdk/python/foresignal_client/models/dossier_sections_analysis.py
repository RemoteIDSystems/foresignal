from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_analysis_status import DossierSectionsAnalysisStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_analysis_facets_type_0 import (
        DossierSectionsAnalysisFacetsType0,
    )
    from ..models.dossier_sections_analysis_links_type_0 import (
        DossierSectionsAnalysisLinksType0,
    )
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="DossierSectionsAnalysis")


@_attrs_define
class DossierSectionsAnalysis:
    """Analytic objects that reference this object

    Attributes:
        status (DossierSectionsAnalysisStatus):
        message (None | str | Unset):
        links (DossierSectionsAnalysisLinksType0 | None | Unset):
        facets (DossierSectionsAnalysisFacetsType0 | None | Unset):
        truncated (bool | Unset):
        findings (list[ObjectEnvelope] | Unset):
        hypothesis_sets (list[ObjectEnvelope] | Unset):
        assessments (list[ObjectEnvelope] | Unset):
    """

    status: DossierSectionsAnalysisStatus
    message: None | str | Unset = UNSET
    links: DossierSectionsAnalysisLinksType0 | None | Unset = UNSET
    facets: DossierSectionsAnalysisFacetsType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    findings: list[ObjectEnvelope] | Unset = UNSET
    hypothesis_sets: list[ObjectEnvelope] | Unset = UNSET
    assessments: list[ObjectEnvelope] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_analysis_facets_type_0 import (
            DossierSectionsAnalysisFacetsType0,
        )
        from ..models.dossier_sections_analysis_links_type_0 import (
            DossierSectionsAnalysisLinksType0,
        )

        status = self.status.value

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        links: dict[str, Any] | None | Unset
        if isinstance(self.links, Unset):
            links = UNSET
        elif isinstance(self.links, DossierSectionsAnalysisLinksType0):
            links = self.links.to_dict()
        else:
            links = self.links

        facets: dict[str, Any] | None | Unset
        if isinstance(self.facets, Unset):
            facets = UNSET
        elif isinstance(self.facets, DossierSectionsAnalysisFacetsType0):
            facets = self.facets.to_dict()
        else:
            facets = self.facets

        truncated = self.truncated

        findings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.findings, Unset):
            findings = []
            for findings_item_data in self.findings:
                findings_item = findings_item_data.to_dict()
                findings.append(findings_item)

        hypothesis_sets: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.hypothesis_sets, Unset):
            hypothesis_sets = []
            for hypothesis_sets_item_data in self.hypothesis_sets:
                hypothesis_sets_item = hypothesis_sets_item_data.to_dict()
                hypothesis_sets.append(hypothesis_sets_item)

        assessments: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.assessments, Unset):
            assessments = []
            for assessments_item_data in self.assessments:
                assessments_item = assessments_item_data.to_dict()
                assessments.append(assessments_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if message is not UNSET:
            field_dict["message"] = message
        if links is not UNSET:
            field_dict["links"] = links
        if facets is not UNSET:
            field_dict["facets"] = facets
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if findings is not UNSET:
            field_dict["findings"] = findings
        if hypothesis_sets is not UNSET:
            field_dict["hypothesisSets"] = hypothesis_sets
        if assessments is not UNSET:
            field_dict["assessments"] = assessments

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_analysis_facets_type_0 import (
            DossierSectionsAnalysisFacetsType0,
        )
        from ..models.dossier_sections_analysis_links_type_0 import (
            DossierSectionsAnalysisLinksType0,
        )
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        status = DossierSectionsAnalysisStatus(d.pop("status"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_links(
            data: object,
        ) -> DossierSectionsAnalysisLinksType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                links_type_0 = DossierSectionsAnalysisLinksType0.from_dict(data)

                return links_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsAnalysisLinksType0 | None | Unset, data)

        links = _parse_links(d.pop("links", UNSET))

        def _parse_facets(
            data: object,
        ) -> DossierSectionsAnalysisFacetsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                facets_type_0 = DossierSectionsAnalysisFacetsType0.from_dict(data)

                return facets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsAnalysisFacetsType0 | None | Unset, data)

        facets = _parse_facets(d.pop("facets", UNSET))

        truncated = d.pop("truncated", UNSET)

        _findings = d.pop("findings", UNSET)
        findings: list[ObjectEnvelope] | Unset = UNSET
        if _findings is not UNSET:
            findings = []
            for findings_item_data in _findings:
                findings_item = ObjectEnvelope.from_dict(findings_item_data)

                findings.append(findings_item)

        _hypothesis_sets = d.pop("hypothesisSets", UNSET)
        hypothesis_sets: list[ObjectEnvelope] | Unset = UNSET
        if _hypothesis_sets is not UNSET:
            hypothesis_sets = []
            for hypothesis_sets_item_data in _hypothesis_sets:
                hypothesis_sets_item = ObjectEnvelope.from_dict(
                    hypothesis_sets_item_data
                )

                hypothesis_sets.append(hypothesis_sets_item)

        _assessments = d.pop("assessments", UNSET)
        assessments: list[ObjectEnvelope] | Unset = UNSET
        if _assessments is not UNSET:
            assessments = []
            for assessments_item_data in _assessments:
                assessments_item = ObjectEnvelope.from_dict(assessments_item_data)

                assessments.append(assessments_item)

        dossier_sections_analysis = cls(
            status=status,
            message=message,
            links=links,
            facets=facets,
            truncated=truncated,
            findings=findings,
            hypothesis_sets=hypothesis_sets,
            assessments=assessments,
        )

        dossier_sections_analysis.additional_properties = d
        return dossier_sections_analysis

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
