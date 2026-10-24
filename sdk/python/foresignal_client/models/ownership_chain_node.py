from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ownership_chain_node_weakest_edge_label import (
    OwnershipChainNodeWeakestEdgeLabel,
)

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope
    from ..models.ownership_chain_node_paths_item import OwnershipChainNodePathsItem


T = TypeVar("T", bound="OwnershipChainNode")


@_attrs_define
class OwnershipChainNode:
    """An entity reached through ownership / affiliation edges, with the chain from each seed that reached it

    Attributes:
        node (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        reached_from (list[str]): The seeds whose chain reaches this entity
        weakest_edge_label (OwnershipChainNodeWeakestEdgeLabel): Weakest edge across the reaching chains (104
            vocabulary)
        paths (list[OwnershipChainNodePathsItem]):
    """

    node: ObjectEnvelope
    reached_from: list[str]
    weakest_edge_label: OwnershipChainNodeWeakestEdgeLabel
    paths: list[OwnershipChainNodePathsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        node = self.node.to_dict()

        reached_from = self.reached_from

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
                "reachedFrom": reached_from,
                "weakestEdgeLabel": weakest_edge_label,
                "paths": paths,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.ownership_chain_node_paths_item import OwnershipChainNodePathsItem

        d = dict(src_dict)
        node = ObjectEnvelope.from_dict(d.pop("node"))

        reached_from = cast(list[str], d.pop("reachedFrom"))

        weakest_edge_label = OwnershipChainNodeWeakestEdgeLabel(
            d.pop("weakestEdgeLabel")
        )

        paths = []
        _paths = d.pop("paths")
        for paths_item_data in _paths:
            paths_item = OwnershipChainNodePathsItem.from_dict(paths_item_data)

            paths.append(paths_item)

        ownership_chain_node = cls(
            node=node,
            reached_from=reached_from,
            weakest_edge_label=weakest_edge_label,
            paths=paths,
        )

        ownership_chain_node.additional_properties = d
        return ownership_chain_node

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
