from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DigestCountEntities")


@_attrs_define
class DigestCountEntities:
    """Distinct entities per group

    Attributes:
        new (int | Unset):
        changed (int | Unset):
        removed (int | Unset):
        edges (int | Unset):
    """

    new: int | Unset = UNSET
    changed: int | Unset = UNSET
    removed: int | Unset = UNSET
    edges: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        new = self.new

        changed = self.changed

        removed = self.removed

        edges = self.edges

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if new is not UNSET:
            field_dict["new"] = new
        if changed is not UNSET:
            field_dict["changed"] = changed
        if removed is not UNSET:
            field_dict["removed"] = removed
        if edges is not UNSET:
            field_dict["edges"] = edges

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        new = d.pop("new", UNSET)

        changed = d.pop("changed", UNSET)

        removed = d.pop("removed", UNSET)

        edges = d.pop("edges", UNSET)

        digest_count_entities = cls(
            new=new,
            changed=changed,
            removed=removed,
            edges=edges,
        )

        digest_count_entities.additional_properties = d
        return digest_count_entities

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
