from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_impact_result_summary_by_label import (
        InfraImpactResultSummaryByLabel,
    )
    from ..models.infra_impact_result_summary_by_type import (
        InfraImpactResultSummaryByType,
    )


T = TypeVar("T", bound="InfraImpactResultSummary")


@_attrs_define
class InfraImpactResultSummary:
    """Counts by facility type and label

    Attributes:
        by_type (InfraImpactResultSummaryByType | Unset):
        by_label (InfraImpactResultSummaryByLabel | Unset):
    """

    by_type: InfraImpactResultSummaryByType | Unset = UNSET
    by_label: InfraImpactResultSummaryByLabel | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        by_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_type, Unset):
            by_type = self.by_type.to_dict()

        by_label: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_label, Unset):
            by_label = self.by_label.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if by_type is not UNSET:
            field_dict["byType"] = by_type
        if by_label is not UNSET:
            field_dict["byLabel"] = by_label

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_impact_result_summary_by_label import (
            InfraImpactResultSummaryByLabel,
        )
        from ..models.infra_impact_result_summary_by_type import (
            InfraImpactResultSummaryByType,
        )

        d = dict(src_dict)
        _by_type = d.pop("byType", UNSET)
        by_type: InfraImpactResultSummaryByType | Unset
        if isinstance(_by_type, Unset):
            by_type = UNSET
        else:
            by_type = InfraImpactResultSummaryByType.from_dict(_by_type)

        _by_label = d.pop("byLabel", UNSET)
        by_label: InfraImpactResultSummaryByLabel | Unset
        if isinstance(_by_label, Unset):
            by_label = UNSET
        else:
            by_label = InfraImpactResultSummaryByLabel.from_dict(_by_label)

        infra_impact_result_summary = cls(
            by_type=by_type,
            by_label=by_label,
        )

        infra_impact_result_summary.additional_properties = d
        return infra_impact_result_summary

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
