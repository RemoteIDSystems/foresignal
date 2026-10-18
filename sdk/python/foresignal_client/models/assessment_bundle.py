from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_manifest import AnalysisManifest
    from ..models.assessment import Assessment
    from ..models.assessment_bundle_objects_item import AssessmentBundleObjectsItem


T = TypeVar("T", bound="AssessmentBundle")


@_attrs_define
class AssessmentBundle:
    """An assessment with its cited objects, provenance and manifests

    Attributes:
        assessment (Assessment): ICD 203-structured assessment that builds on the previous one of its cadence
        generated_at (datetime.datetime):
        objects (list[AssessmentBundleObjectsItem]):
        manifests (list[AnalysisManifest] | Unset):
    """

    assessment: Assessment
    generated_at: datetime.datetime
    objects: list[AssessmentBundleObjectsItem]
    manifests: list[AnalysisManifest] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        assessment = self.assessment.to_dict()

        generated_at = self.generated_at.isoformat()

        objects = []
        for objects_item_data in self.objects:
            objects_item = objects_item_data.to_dict()
            objects.append(objects_item)

        manifests: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.manifests, Unset):
            manifests = []
            for manifests_item_data in self.manifests:
                manifests_item = manifests_item_data.to_dict()
                manifests.append(manifests_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "assessment": assessment,
                "generatedAt": generated_at,
                "objects": objects,
            }
        )
        if manifests is not UNSET:
            field_dict["manifests"] = manifests

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_manifest import AnalysisManifest
        from ..models.assessment import Assessment
        from ..models.assessment_bundle_objects_item import AssessmentBundleObjectsItem

        d = dict(src_dict)
        assessment = Assessment.from_dict(d.pop("assessment"))

        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        objects = []
        _objects = d.pop("objects")
        for objects_item_data in _objects:
            objects_item = AssessmentBundleObjectsItem.from_dict(objects_item_data)

            objects.append(objects_item)

        _manifests = d.pop("manifests", UNSET)
        manifests: list[AnalysisManifest] | Unset = UNSET
        if _manifests is not UNSET:
            manifests = []
            for manifests_item_data in _manifests:
                manifests_item = AnalysisManifest.from_dict(manifests_item_data)

                manifests.append(manifests_item)

        assessment_bundle = cls(
            assessment=assessment,
            generated_at=generated_at,
            objects=objects,
            manifests=manifests,
        )

        assessment_bundle.additional_properties = d
        return assessment_bundle

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
