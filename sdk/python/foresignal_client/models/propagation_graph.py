from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef
    from ..models.propagation_edge import PropagationEdge
    from ..models.propagation_graph_cross_platform_order_item import (
        PropagationGraphCrossPlatformOrderItem,
    )
    from ..models.propagation_graph_descent_share import PropagationGraphDescentShare
    from ..models.propagation_graph_earliest_by_platform_item import (
        PropagationGraphEarliestByPlatformItem,
    )
    from ..models.propagation_graph_first_news_pickup_type_0 import (
        PropagationGraphFirstNewsPickupType0,
    )
    from ..models.propagation_node import PropagationNode
    from ..models.provenance_chain_entry import ProvenanceChainEntry


T = TypeVar("T", bound="PropagationGraph")


@_attrs_define
class PropagationGraph:
    """Per-narrative propagation across reposts, platforms and news (`PropagationTracer` 1.0.0); large graphs summarised to
    ≤ 500 nodes by collapsing evidence groups

        Attributes:
            narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            summarised (bool):
            total_nodes (int):
            total_edges (int):
            nodes (list[PropagationNode]):
            edges (list[PropagationEdge]):
            earliest_by_platform (list[PropagationGraphEarliestByPlatformItem]):
            descent_share (PropagationGraphDescentShare): Posts and articles reachable from the top three evidence-group
                roots
            cross_platform_order (list[PropagationGraphCrossPlatformOrderItem]):
            tracer (DetectorRef):
            computed_at (datetime.datetime):
            first_news_pickup (None | PropagationGraphFirstNewsPickupType0 | Unset):
            median_repost_delay_seconds (float | None | Unset):
            provenance_chain (list[ProvenanceChainEntry] | Unset): 134 chain so circular reporting is recognised
            manifest_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
    """

    narrative_id: str
    summarised: bool
    total_nodes: int
    total_edges: int
    nodes: list[PropagationNode]
    edges: list[PropagationEdge]
    earliest_by_platform: list[PropagationGraphEarliestByPlatformItem]
    descent_share: PropagationGraphDescentShare
    cross_platform_order: list[PropagationGraphCrossPlatformOrderItem]
    tracer: DetectorRef
    computed_at: datetime.datetime
    first_news_pickup: None | PropagationGraphFirstNewsPickupType0 | Unset = UNSET
    median_repost_delay_seconds: float | None | Unset = UNSET
    provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
    manifest_id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.propagation_graph_first_news_pickup_type_0 import (
            PropagationGraphFirstNewsPickupType0,
        )

        narrative_id = self.narrative_id

        summarised = self.summarised

        total_nodes = self.total_nodes

        total_edges = self.total_edges

        nodes = []
        for nodes_item_data in self.nodes:
            nodes_item = nodes_item_data.to_dict()
            nodes.append(nodes_item)

        edges = []
        for edges_item_data in self.edges:
            edges_item = edges_item_data.to_dict()
            edges.append(edges_item)

        earliest_by_platform = []
        for earliest_by_platform_item_data in self.earliest_by_platform:
            earliest_by_platform_item = earliest_by_platform_item_data.to_dict()
            earliest_by_platform.append(earliest_by_platform_item)

        descent_share = self.descent_share.to_dict()

        cross_platform_order = []
        for cross_platform_order_item_data in self.cross_platform_order:
            cross_platform_order_item = cross_platform_order_item_data.to_dict()
            cross_platform_order.append(cross_platform_order_item)

        tracer = self.tracer.to_dict()

        computed_at = self.computed_at.isoformat()

        first_news_pickup: dict[str, Any] | None | Unset
        if isinstance(self.first_news_pickup, Unset):
            first_news_pickup = UNSET
        elif isinstance(self.first_news_pickup, PropagationGraphFirstNewsPickupType0):
            first_news_pickup = self.first_news_pickup.to_dict()
        else:
            first_news_pickup = self.first_news_pickup

        median_repost_delay_seconds: float | None | Unset
        if isinstance(self.median_repost_delay_seconds, Unset):
            median_repost_delay_seconds = UNSET
        else:
            median_repost_delay_seconds = self.median_repost_delay_seconds

        provenance_chain: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.provenance_chain, Unset):
            provenance_chain = []
            for provenance_chain_item_data in self.provenance_chain:
                provenance_chain_item = provenance_chain_item_data.to_dict()
                provenance_chain.append(provenance_chain_item)

        manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narrativeId": narrative_id,
                "summarised": summarised,
                "totalNodes": total_nodes,
                "totalEdges": total_edges,
                "nodes": nodes,
                "edges": edges,
                "earliestByPlatform": earliest_by_platform,
                "descentShare": descent_share,
                "crossPlatformOrder": cross_platform_order,
                "tracer": tracer,
                "computedAt": computed_at,
            }
        )
        if first_news_pickup is not UNSET:
            field_dict["firstNewsPickup"] = first_news_pickup
        if median_repost_delay_seconds is not UNSET:
            field_dict["medianRepostDelaySeconds"] = median_repost_delay_seconds
        if provenance_chain is not UNSET:
            field_dict["provenanceChain"] = provenance_chain
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref import DetectorRef
        from ..models.propagation_edge import PropagationEdge
        from ..models.propagation_graph_cross_platform_order_item import (
            PropagationGraphCrossPlatformOrderItem,
        )
        from ..models.propagation_graph_descent_share import (
            PropagationGraphDescentShare,
        )
        from ..models.propagation_graph_earliest_by_platform_item import (
            PropagationGraphEarliestByPlatformItem,
        )
        from ..models.propagation_graph_first_news_pickup_type_0 import (
            PropagationGraphFirstNewsPickupType0,
        )
        from ..models.propagation_node import PropagationNode
        from ..models.provenance_chain_entry import ProvenanceChainEntry

        d = dict(src_dict)
        narrative_id = d.pop("narrativeId")

        summarised = d.pop("summarised")

        total_nodes = d.pop("totalNodes")

        total_edges = d.pop("totalEdges")

        nodes = []
        _nodes = d.pop("nodes")
        for nodes_item_data in _nodes:
            nodes_item = PropagationNode.from_dict(nodes_item_data)

            nodes.append(nodes_item)

        edges = []
        _edges = d.pop("edges")
        for edges_item_data in _edges:
            edges_item = PropagationEdge.from_dict(edges_item_data)

            edges.append(edges_item)

        earliest_by_platform = []
        _earliest_by_platform = d.pop("earliestByPlatform")
        for earliest_by_platform_item_data in _earliest_by_platform:
            earliest_by_platform_item = (
                PropagationGraphEarliestByPlatformItem.from_dict(
                    earliest_by_platform_item_data
                )
            )

            earliest_by_platform.append(earliest_by_platform_item)

        descent_share = PropagationGraphDescentShare.from_dict(d.pop("descentShare"))

        cross_platform_order = []
        _cross_platform_order = d.pop("crossPlatformOrder")
        for cross_platform_order_item_data in _cross_platform_order:
            cross_platform_order_item = (
                PropagationGraphCrossPlatformOrderItem.from_dict(
                    cross_platform_order_item_data
                )
            )

            cross_platform_order.append(cross_platform_order_item)

        tracer = DetectorRef.from_dict(d.pop("tracer"))

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_first_news_pickup(
            data: object,
        ) -> None | PropagationGraphFirstNewsPickupType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                first_news_pickup_type_0 = (
                    PropagationGraphFirstNewsPickupType0.from_dict(data)
                )

                return first_news_pickup_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PropagationGraphFirstNewsPickupType0 | Unset, data)

        first_news_pickup = _parse_first_news_pickup(d.pop("firstNewsPickup", UNSET))

        def _parse_median_repost_delay_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        median_repost_delay_seconds = _parse_median_repost_delay_seconds(
            d.pop("medianRepostDelaySeconds", UNSET)
        )

        _provenance_chain = d.pop("provenanceChain", UNSET)
        provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
        if _provenance_chain is not UNSET:
            provenance_chain = []
            for provenance_chain_item_data in _provenance_chain:
                provenance_chain_item = ProvenanceChainEntry.from_dict(
                    provenance_chain_item_data
                )

                provenance_chain.append(provenance_chain_item)

        manifest_id = d.pop("manifestId", UNSET)

        propagation_graph = cls(
            narrative_id=narrative_id,
            summarised=summarised,
            total_nodes=total_nodes,
            total_edges=total_edges,
            nodes=nodes,
            edges=edges,
            earliest_by_platform=earliest_by_platform,
            descent_share=descent_share,
            cross_platform_order=cross_platform_order,
            tracer=tracer,
            computed_at=computed_at,
            first_news_pickup=first_news_pickup,
            median_repost_delay_seconds=median_repost_delay_seconds,
            provenance_chain=provenance_chain,
            manifest_id=manifest_id,
        )

        propagation_graph.additional_properties = d
        return propagation_graph

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
