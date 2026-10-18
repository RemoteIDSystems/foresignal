from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_edge import InfraEdge


T = TypeVar("T", bound="InfraNodeEdgesType0")


@_attrs_define
class InfraNodeEdgesType0:
    """Present on getInfraNode

    Attributes:
        in_ (list[InfraEdge] | Unset):
        out (list[InfraEdge] | Unset):
    """

    in_: list[InfraEdge] | Unset = UNSET
    out: list[InfraEdge] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        in_: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.in_, Unset):
            in_ = []
            for in_item_data in self.in_:
                in_item = in_item_data.to_dict()
                in_.append(in_item)

        out: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.out, Unset):
            out = []
            for out_item_data in self.out:
                out_item = out_item_data.to_dict()
                out.append(out_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if in_ is not UNSET:
            field_dict["in"] = in_
        if out is not UNSET:
            field_dict["out"] = out

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_edge import InfraEdge

        d = dict(src_dict)
        _in_ = d.pop("in", UNSET)
        in_: list[InfraEdge] | Unset = UNSET
        if _in_ is not UNSET:
            in_ = []
            for in_item_data in _in_:
                in_item = InfraEdge.from_dict(in_item_data)

                in_.append(in_item)

        _out = d.pop("out", UNSET)
        out: list[InfraEdge] | Unset = UNSET
        if _out is not UNSET:
            out = []
            for out_item_data in _out:
                out_item = InfraEdge.from_dict(out_item_data)

                out.append(out_item)

        infra_node_edges_type_0 = cls(
            in_=in_,
            out=out,
        )

        infra_node_edges_type_0.additional_properties = d
        return infra_node_edges_type_0

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
