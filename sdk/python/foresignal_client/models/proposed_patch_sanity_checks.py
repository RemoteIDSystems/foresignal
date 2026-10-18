from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProposedPatchSanityChecks")


@_attrs_define
class ProposedPatchSanityChecks:
    """javac-free checks on the patched file

    Attributes:
        braces_balanced (bool | Unset):
        class_name_unchanged (bool | Unset):
    """

    braces_balanced: bool | Unset = UNSET
    class_name_unchanged: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        braces_balanced = self.braces_balanced

        class_name_unchanged = self.class_name_unchanged

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if braces_balanced is not UNSET:
            field_dict["bracesBalanced"] = braces_balanced
        if class_name_unchanged is not UNSET:
            field_dict["classNameUnchanged"] = class_name_unchanged

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        braces_balanced = d.pop("bracesBalanced", UNSET)

        class_name_unchanged = d.pop("classNameUnchanged", UNSET)

        proposed_patch_sanity_checks = cls(
            braces_balanced=braces_balanced,
            class_name_unchanged=class_name_unchanged,
        )

        proposed_patch_sanity_checks.additional_properties = d
        return proposed_patch_sanity_checks

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
