from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.infra_edge_label import InfraEdgeLabel
from ..models.infra_edge_method import InfraEdgeMethod
from ..models.infra_edge_relation import InfraEdgeRelation
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_edge_evidence import InfraEdgeEvidence
    from ..models.infra_edge_params import InfraEdgeParams


T = TypeVar("T", bound="InfraEdge")


@_attrs_define
class InfraEdge:
    """A typed dependency edge that states how it is known: method, label (proximity never better than possible), rule
    version, parameters and evidence

        Attributes:
            id (int):
            from_node (int):
            to_node (int):
            relation (InfraEdgeRelation):
            method (InfraEdgeMethod):
            label (InfraEdgeLabel):
            score (float):
            rule_id (str):  Example: plant-substation.
            rule_version (str):
            licence (str): Most restrictive input licence (ODbL-1.0 for OSM-derived edges)
            params (InfraEdgeParams | Unset): Rule parameters used
            evidence (InfraEdgeEvidence | Unset): Why the edge exists
            built_at (datetime.datetime | Unset):
    """

    id: int
    from_node: int
    to_node: int
    relation: InfraEdgeRelation
    method: InfraEdgeMethod
    label: InfraEdgeLabel
    score: float
    rule_id: str
    rule_version: str
    licence: str
    params: InfraEdgeParams | Unset = UNSET
    evidence: InfraEdgeEvidence | Unset = UNSET
    built_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        from_node = self.from_node

        to_node = self.to_node

        relation = self.relation.value

        method = self.method.value

        label = self.label.value

        score = self.score

        rule_id = self.rule_id

        rule_version = self.rule_version

        licence = self.licence

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        evidence: dict[str, Any] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = self.evidence.to_dict()

        built_at: str | Unset = UNSET
        if not isinstance(self.built_at, Unset):
            built_at = self.built_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "fromNode": from_node,
                "toNode": to_node,
                "relation": relation,
                "method": method,
                "label": label,
                "score": score,
                "ruleId": rule_id,
                "ruleVersion": rule_version,
                "licence": licence,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if built_at is not UNSET:
            field_dict["builtAt"] = built_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_edge_evidence import InfraEdgeEvidence
        from ..models.infra_edge_params import InfraEdgeParams

        d = dict(src_dict)
        id = d.pop("id")

        from_node = d.pop("fromNode")

        to_node = d.pop("toNode")

        relation = InfraEdgeRelation(d.pop("relation"))

        method = InfraEdgeMethod(d.pop("method"))

        label = InfraEdgeLabel(d.pop("label"))

        score = d.pop("score")

        rule_id = d.pop("ruleId")

        rule_version = d.pop("ruleVersion")

        licence = d.pop("licence")

        _params = d.pop("params", UNSET)
        params: InfraEdgeParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = InfraEdgeParams.from_dict(_params)

        _evidence = d.pop("evidence", UNSET)
        evidence: InfraEdgeEvidence | Unset
        if isinstance(_evidence, Unset):
            evidence = UNSET
        else:
            evidence = InfraEdgeEvidence.from_dict(_evidence)

        _built_at = d.pop("builtAt", UNSET)
        built_at: datetime.datetime | Unset
        if isinstance(_built_at, Unset):
            built_at = UNSET
        else:
            built_at = datetime.datetime.fromisoformat(_built_at)

        infra_edge = cls(
            id=id,
            from_node=from_node,
            to_node=to_node,
            relation=relation,
            method=method,
            label=label,
            score=score,
            rule_id=rule_id,
            rule_version=rule_version,
            licence=licence,
            params=params,
            evidence=evidence,
            built_at=built_at,
        )

        infra_edge.additional_properties = d
        return infra_edge

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
