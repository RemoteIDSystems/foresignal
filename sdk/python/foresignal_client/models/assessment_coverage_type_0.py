from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.assessment_coverage_type_0_situations_item import (
        AssessmentCoverageType0SituationsItem,
    )


T = TypeVar("T", bound="AssessmentCoverageType0")


@_attrs_define
class AssessmentCoverageType0:
    """Deterministic Coverage section

    Attributes:
        situations (list[AssessmentCoverageType0SituationsItem] | Unset):
        blindspot_story_ids (list[str] | Unset):
        divergence_finding_ids (list[str] | Unset):
    """

    situations: list[AssessmentCoverageType0SituationsItem] | Unset = UNSET
    blindspot_story_ids: list[str] | Unset = UNSET
    divergence_finding_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.situations, Unset):
            situations = []
            for situations_item_data in self.situations:
                situations_item = situations_item_data.to_dict()
                situations.append(situations_item)

        blindspot_story_ids: list[str] | Unset = UNSET
        if not isinstance(self.blindspot_story_ids, Unset):
            blindspot_story_ids = self.blindspot_story_ids

        divergence_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.divergence_finding_ids, Unset):
            divergence_finding_ids = self.divergence_finding_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if situations is not UNSET:
            field_dict["situations"] = situations
        if blindspot_story_ids is not UNSET:
            field_dict["blindspotStoryIds"] = blindspot_story_ids
        if divergence_finding_ids is not UNSET:
            field_dict["divergenceFindingIds"] = divergence_finding_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.assessment_coverage_type_0_situations_item import (
            AssessmentCoverageType0SituationsItem,
        )

        d = dict(src_dict)
        _situations = d.pop("situations", UNSET)
        situations: list[AssessmentCoverageType0SituationsItem] | Unset = UNSET
        if _situations is not UNSET:
            situations = []
            for situations_item_data in _situations:
                situations_item = AssessmentCoverageType0SituationsItem.from_dict(
                    situations_item_data
                )

                situations.append(situations_item)

        blindspot_story_ids = cast(list[str], d.pop("blindspotStoryIds", UNSET))

        divergence_finding_ids = cast(list[str], d.pop("divergenceFindingIds", UNSET))

        assessment_coverage_type_0 = cls(
            situations=situations,
            blindspot_story_ids=blindspot_story_ids,
            divergence_finding_ids=divergence_finding_ids,
        )

        assessment_coverage_type_0.additional_properties = d
        return assessment_coverage_type_0

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
