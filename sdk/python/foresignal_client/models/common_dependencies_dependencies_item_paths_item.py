from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.common_dependencies_dependencies_item_paths_item_weakest_edge_label import (
    CommonDependenciesDependenciesItemPathsItemWeakestEdgeLabel,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="CommonDependenciesDependenciesItemPathsItem")


@_attrs_define
class CommonDependenciesDependenciesItemPathsItem:
    """Path from an input object to the dependency

    Attributes:
        from_ (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        hops (list[str]):
        weakest_edge_label (CommonDependenciesDependenciesItemPathsItemWeakestEdgeLabel): Relation or dependency label
            (104 vocabulary)
        relation_types (list[str] | Unset):
    """

    from_: str
    hops: list[str]
    weakest_edge_label: CommonDependenciesDependenciesItemPathsItemWeakestEdgeLabel
    relation_types: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_

        hops = self.hops

        weakest_edge_label = self.weakest_edge_label.value

        relation_types: list[str] | Unset = UNSET
        if not isinstance(self.relation_types, Unset):
            relation_types = self.relation_types

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "hops": hops,
                "weakestEdgeLabel": weakest_edge_label,
            }
        )
        if relation_types is not UNSET:
            field_dict["relationTypes"] = relation_types

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = d.pop("from")

        hops = cast(list[str], d.pop("hops"))

        weakest_edge_label = (
            CommonDependenciesDependenciesItemPathsItemWeakestEdgeLabel(
                d.pop("weakestEdgeLabel")
            )
        )

        relation_types = cast(list[str], d.pop("relationTypes", UNSET))

        common_dependencies_dependencies_item_paths_item = cls(
            from_=from_,
            hops=hops,
            weakest_edge_label=weakest_edge_label,
            relation_types=relation_types,
        )

        common_dependencies_dependencies_item_paths_item.additional_properties = d
        return common_dependencies_dependencies_item_paths_item

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
