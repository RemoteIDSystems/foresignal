from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.infra_impact_result_dependents_item_path_item_label import (
    InfraImpactResultDependentsItemPathItemLabel,
)
from ..models.infra_impact_result_dependents_item_path_item_method import (
    InfraImpactResultDependentsItemPathItemMethod,
)
from ..models.infra_impact_result_dependents_item_path_item_relation import (
    InfraImpactResultDependentsItemPathItemRelation,
)

T = TypeVar("T", bound="InfraImpactResultDependentsItemPathItem")


@_attrs_define
class InfraImpactResultDependentsItemPathItem:
    """Edge on the path

    Attributes:
        edge_id (int):
        relation (InfraImpactResultDependentsItemPathItemRelation):
        method (InfraImpactResultDependentsItemPathItemMethod):
        label (InfraImpactResultDependentsItemPathItemLabel):
    """

    edge_id: int
    relation: InfraImpactResultDependentsItemPathItemRelation
    method: InfraImpactResultDependentsItemPathItemMethod
    label: InfraImpactResultDependentsItemPathItemLabel
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        edge_id = self.edge_id

        relation = self.relation.value

        method = self.method.value

        label = self.label.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "edgeId": edge_id,
                "relation": relation,
                "method": method,
                "label": label,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        edge_id = d.pop("edgeId")

        relation = InfraImpactResultDependentsItemPathItemRelation(d.pop("relation"))

        method = InfraImpactResultDependentsItemPathItemMethod(d.pop("method"))

        label = InfraImpactResultDependentsItemPathItemLabel(d.pop("label"))

        infra_impact_result_dependents_item_path_item = cls(
            edge_id=edge_id,
            relation=relation,
            method=method,
            label=label,
        )

        infra_impact_result_dependents_item_path_item.additional_properties = d
        return infra_impact_result_dependents_item_path_item

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
