from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.projected_impact_dependency_paths_item_weakest_edge_label import (
    ProjectedImpactDependencyPathsItemWeakestEdgeLabel,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProjectedImpactDependencyPathsItem")


@_attrs_define
class ProjectedImpactDependencyPathsItem:
    """104 dependency path from an exposed facility to a downstream asset

    Attributes:
        from_ (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        to (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        weakest_edge_label (ProjectedImpactDependencyPathsItemWeakestEdgeLabel): Relation or dependency label (104
            vocabulary)
        hops (list[str] | Unset):
        depth (int | Unset): Edges from the exposed facility
    """

    from_: str
    to: str
    weakest_edge_label: ProjectedImpactDependencyPathsItemWeakestEdgeLabel
    hops: list[str] | Unset = UNSET
    depth: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_

        to = self.to

        weakest_edge_label = self.weakest_edge_label.value

        hops: list[str] | Unset = UNSET
        if not isinstance(self.hops, Unset):
            hops = self.hops

        depth = self.depth

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "weakestEdgeLabel": weakest_edge_label,
            }
        )
        if hops is not UNSET:
            field_dict["hops"] = hops
        if depth is not UNSET:
            field_dict["depth"] = depth

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = d.pop("from")

        to = d.pop("to")

        weakest_edge_label = ProjectedImpactDependencyPathsItemWeakestEdgeLabel(
            d.pop("weakestEdgeLabel")
        )

        hops = cast(list[str], d.pop("hops", UNSET))

        depth = d.pop("depth", UNSET)

        projected_impact_dependency_paths_item = cls(
            from_=from_,
            to=to,
            weakest_edge_label=weakest_edge_label,
            hops=hops,
            depth=depth,
        )

        projected_impact_dependency_paths_item.additional_properties = d
        return projected_impact_dependency_paths_item

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
