from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExposureDependentsByLabel")


@_attrs_define
class ExposureDependentsByLabel:
    """Dependent counts by path label

    Attributes:
        confirmed (int | Unset):
        likely (int | Unset):
        possible (int | Unset):
        speculative (int | Unset):
    """

    confirmed: int | Unset = UNSET
    likely: int | Unset = UNSET
    possible: int | Unset = UNSET
    speculative: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        confirmed = self.confirmed

        likely = self.likely

        possible = self.possible

        speculative = self.speculative

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if confirmed is not UNSET:
            field_dict["confirmed"] = confirmed
        if likely is not UNSET:
            field_dict["likely"] = likely
        if possible is not UNSET:
            field_dict["possible"] = possible
        if speculative is not UNSET:
            field_dict["speculative"] = speculative

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        confirmed = d.pop("confirmed", UNSET)

        likely = d.pop("likely", UNSET)

        possible = d.pop("possible", UNSET)

        speculative = d.pop("speculative", UNSET)

        exposure_dependents_by_label = cls(
            confirmed=confirmed,
            likely=likely,
            possible=possible,
            speculative=speculative,
        )

        exposure_dependents_by_label.additional_properties = d
        return exposure_dependents_by_label

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
