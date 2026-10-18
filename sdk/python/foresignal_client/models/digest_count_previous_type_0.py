from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DigestCountPreviousType0")


@_attrs_define
class DigestCountPreviousType0:
    """Same counts for the previous equal-length period; null when below the retention floor

    Attributes:
        new (int | Unset):
        changed (int | Unset):
        removed (int | Unset):
        active (int | Unset): null until 038's rollups (record_live keeps only the latest receipt)
    """

    new: int | Unset = UNSET
    changed: int | Unset = UNSET
    removed: int | Unset = UNSET
    active: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        new = self.new

        changed = self.changed

        removed = self.removed

        active = self.active

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if new is not UNSET:
            field_dict["new"] = new
        if changed is not UNSET:
            field_dict["changed"] = changed
        if removed is not UNSET:
            field_dict["removed"] = removed
        if active is not UNSET:
            field_dict["active"] = active

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        new = d.pop("new", UNSET)

        changed = d.pop("changed", UNSET)

        removed = d.pop("removed", UNSET)

        active = d.pop("active", UNSET)

        digest_count_previous_type_0 = cls(
            new=new,
            changed=changed,
            removed=removed,
            active=active,
        )

        digest_count_previous_type_0.additional_properties = d
        return digest_count_previous_type_0

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
