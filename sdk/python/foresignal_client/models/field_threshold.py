from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FieldThreshold")


@_attrs_define
class FieldThreshold:
    """SAMPLED threshold: persist when a numeric payload field moved by at least `delta` since the persisted version

    Attributes:
        field (str): Top-level payload key (never significant) Example: alt_baro.
        delta (float): Minimum change
        angular (bool | Unset): Compare with 360° wrap (headings, courses) Default: False.
    """

    field: str
    delta: float
    angular: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        field = self.field

        delta = self.delta

        angular = self.angular

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "field": field,
                "delta": delta,
            }
        )
        if angular is not UNSET:
            field_dict["angular"] = angular

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        field = d.pop("field")

        delta = d.pop("delta")

        angular = d.pop("angular", UNSET)

        field_threshold = cls(
            field=field,
            delta=delta,
            angular=angular,
        )

        field_threshold.additional_properties = d
        return field_threshold

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
