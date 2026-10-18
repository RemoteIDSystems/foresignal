from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.common_dependencies_dependencies_item_weakest_edge_label import (
    CommonDependenciesDependenciesItemWeakestEdgeLabel,
)

if TYPE_CHECKING:
    from ..models.common_dependencies_dependencies_item_paths_item import (
        CommonDependenciesDependenciesItemPathsItem,
    )
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="CommonDependenciesDependenciesItem")


@_attrs_define
class CommonDependenciesDependenciesItem:
    """One shared dependency

    Attributes:
        node (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        shared_by (list[str]):
        weakest_edge_label (CommonDependenciesDependenciesItemWeakestEdgeLabel): Relation or dependency label (104
            vocabulary)
        paths (list[CommonDependenciesDependenciesItemPathsItem]):
    """

    node: ObjectEnvelope
    shared_by: list[str]
    weakest_edge_label: CommonDependenciesDependenciesItemWeakestEdgeLabel
    paths: list[CommonDependenciesDependenciesItemPathsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        node = self.node.to_dict()

        shared_by = self.shared_by

        weakest_edge_label = self.weakest_edge_label.value

        paths = []
        for paths_item_data in self.paths:
            paths_item = paths_item_data.to_dict()
            paths.append(paths_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "node": node,
                "sharedBy": shared_by,
                "weakestEdgeLabel": weakest_edge_label,
                "paths": paths,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.common_dependencies_dependencies_item_paths_item import (
            CommonDependenciesDependenciesItemPathsItem,
        )
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        node = ObjectEnvelope.from_dict(d.pop("node"))

        shared_by = cast(list[str], d.pop("sharedBy"))

        weakest_edge_label = CommonDependenciesDependenciesItemWeakestEdgeLabel(
            d.pop("weakestEdgeLabel")
        )

        paths = []
        _paths = d.pop("paths")
        for paths_item_data in _paths:
            paths_item = CommonDependenciesDependenciesItemPathsItem.from_dict(
                paths_item_data
            )

            paths.append(paths_item)

        common_dependencies_dependencies_item = cls(
            node=node,
            shared_by=shared_by,
            weakest_edge_label=weakest_edge_label,
            paths=paths,
        )

        common_dependencies_dependencies_item.additional_properties = d
        return common_dependencies_dependencies_item

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
