from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.infra_impact_result_dependents_item_path_label import (
    InfraImpactResultDependentsItemPathLabel,
)

if TYPE_CHECKING:
    from ..models.infra_impact_result_dependents_item_path_item import (
        InfraImpactResultDependentsItemPathItem,
    )
    from ..models.infra_node import InfraNode


T = TypeVar("T", bound="InfraImpactResultDependentsItem")


@_attrs_define
class InfraImpactResultDependentsItem:
    """A dependent facility and its path

    Attributes:
        node (InfraNode): A facility node: a view of a reference or OSM record, never edited by hand
        depth (int):
        path_label (InfraImpactResultDependentsItemPathLabel): Weakest edge label
        path_score (float): Product of edge scores
        path (list[InfraImpactResultDependentsItemPathItem]):
    """

    node: InfraNode
    depth: int
    path_label: InfraImpactResultDependentsItemPathLabel
    path_score: float
    path: list[InfraImpactResultDependentsItemPathItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        node = self.node.to_dict()

        depth = self.depth

        path_label = self.path_label.value

        path_score = self.path_score

        path = []
        for path_item_data in self.path:
            path_item = path_item_data.to_dict()
            path.append(path_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "node": node,
                "depth": depth,
                "pathLabel": path_label,
                "pathScore": path_score,
                "path": path,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_impact_result_dependents_item_path_item import (
            InfraImpactResultDependentsItemPathItem,
        )
        from ..models.infra_node import InfraNode

        d = dict(src_dict)
        node = InfraNode.from_dict(d.pop("node"))

        depth = d.pop("depth")

        path_label = InfraImpactResultDependentsItemPathLabel(d.pop("pathLabel"))

        path_score = d.pop("pathScore")

        path = []
        _path = d.pop("path")
        for path_item_data in _path:
            path_item = InfraImpactResultDependentsItemPathItem.from_dict(
                path_item_data
            )

            path.append(path_item)

        infra_impact_result_dependents_item = cls(
            node=node,
            depth=depth,
            path_label=path_label,
            path_score=path_score,
            path=path,
        )

        infra_impact_result_dependents_item.additional_properties = d
        return infra_impact_result_dependents_item

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
