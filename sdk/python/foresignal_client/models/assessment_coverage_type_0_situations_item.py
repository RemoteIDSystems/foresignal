from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.coverage_stats import CoverageStats


T = TypeVar("T", bound="AssessmentCoverageType0SituationsItem")


@_attrs_define
class AssessmentCoverageType0SituationsItem:
    """Coverage of a top situation

    Attributes:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        coverage (CoverageStats): Coverage of a story, topic, situation or a hypothesis's news support (CoverageAnalyzer
            1.0.0); describes reporting, never truth
    """

    situation_id: str
    coverage: CoverageStats
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situation_id = self.situation_id

        coverage = self.coverage.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "situationId": situation_id,
                "coverage": coverage,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coverage_stats import CoverageStats

        d = dict(src_dict)
        situation_id = d.pop("situationId")

        coverage = CoverageStats.from_dict(d.pop("coverage"))

        assessment_coverage_type_0_situations_item = cls(
            situation_id=situation_id,
            coverage=coverage,
        )

        assessment_coverage_type_0_situations_item.additional_properties = d
        return assessment_coverage_type_0_situations_item

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
