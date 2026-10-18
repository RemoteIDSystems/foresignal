from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope
    from ..models.relation import Relation


T = TypeVar("T", bound="GraphResult")


@_attrs_define
class GraphResult:
    """Nodes (envelopes) and edges for graph views

    Attributes:
        nodes (list[ObjectEnvelope]):
        edges (list[Relation]):
        missing (list[str] | Unset):
        truncated (bool | Unset):
        as_of (datetime.datetime | None | Unset):
    """

    nodes: list[ObjectEnvelope]
    edges: list[Relation]
    missing: list[str] | Unset = UNSET
    truncated: bool | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        nodes = []
        for nodes_item_data in self.nodes:
            nodes_item = nodes_item_data.to_dict()
            nodes.append(nodes_item)

        edges = []
        for edges_item_data in self.edges:
            edges_item = edges_item_data.to_dict()
            edges.append(edges_item)

        missing: list[str] | Unset = UNSET
        if not isinstance(self.missing, Unset):
            missing = self.missing

        truncated = self.truncated

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "nodes": nodes,
                "edges": edges,
            }
        )
        if missing is not UNSET:
            field_dict["missing"] = missing
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if as_of is not UNSET:
            field_dict["asOf"] = as_of

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.relation import Relation

        d = dict(src_dict)
        nodes = []
        _nodes = d.pop("nodes")
        for nodes_item_data in _nodes:
            nodes_item = ObjectEnvelope.from_dict(nodes_item_data)

            nodes.append(nodes_item)

        edges = []
        _edges = d.pop("edges")
        for edges_item_data in _edges:
            edges_item = Relation.from_dict(edges_item_data)

            edges.append(edges_item)

        missing = cast(list[str], d.pop("missing", UNSET))

        truncated = d.pop("truncated", UNSET)

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        graph_result = cls(
            nodes=nodes,
            edges=edges,
            missing=missing,
            truncated=truncated,
            as_of=as_of,
        )

        graph_result.additional_properties = d
        return graph_result

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
