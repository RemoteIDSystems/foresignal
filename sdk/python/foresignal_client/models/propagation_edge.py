from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.propagation_edge_type import PropagationEdgeType
from ..types import UNSET, Unset

T = TypeVar("T", bound="PropagationEdge")


@_attrs_define
class PropagationEdge:
    """A directed propagation edge with its time delta

    Attributes:
        from_ (str):
        to (str):
        type_ (PropagationEdgeType):
        delta_seconds (float):
        count (int | Unset): Edges collapsed into this one
    """

    from_: str
    to: str
    type_: PropagationEdgeType
    delta_seconds: float
    count: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_

        to = self.to

        type_ = self.type_.value

        delta_seconds = self.delta_seconds

        count = self.count

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "type": type_,
                "deltaSeconds": delta_seconds,
            }
        )
        if count is not UNSET:
            field_dict["count"] = count

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = d.pop("from")

        to = d.pop("to")

        type_ = PropagationEdgeType(d.pop("type"))

        delta_seconds = d.pop("deltaSeconds")

        count = d.pop("count", UNSET)

        propagation_edge = cls(
            from_=from_,
            to=to,
            type_=type_,
            delta_seconds=delta_seconds,
            count=count,
        )

        propagation_edge.additional_properties = d
        return propagation_edge

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
