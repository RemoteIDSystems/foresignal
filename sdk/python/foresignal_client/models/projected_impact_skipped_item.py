from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProjectedImpactSkippedItem")


@_attrs_define
class ProjectedImpactSkippedItem:
    """Impact pair skipped (n < 20 or no rate)

    Attributes:
        impact_family (str):
        reason (str):
        region_class (None | str | Unset):
        n (int | None | Unset):
    """

    impact_family: str
    reason: str
    region_class: None | str | Unset = UNSET
    n: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        impact_family = self.impact_family

        reason = self.reason

        region_class: None | str | Unset
        if isinstance(self.region_class, Unset):
            region_class = UNSET
        else:
            region_class = self.region_class

        n: int | None | Unset
        if isinstance(self.n, Unset):
            n = UNSET
        else:
            n = self.n

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "impactFamily": impact_family,
                "reason": reason,
            }
        )
        if region_class is not UNSET:
            field_dict["regionClass"] = region_class
        if n is not UNSET:
            field_dict["n"] = n

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        impact_family = d.pop("impactFamily")

        reason = d.pop("reason")

        def _parse_region_class(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_class = _parse_region_class(d.pop("regionClass", UNSET))

        def _parse_n(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        n = _parse_n(d.pop("n", UNSET))

        projected_impact_skipped_item = cls(
            impact_family=impact_family,
            reason=reason,
            region_class=region_class,
            n=n,
        )

        projected_impact_skipped_item.additional_properties = d
        return projected_impact_skipped_item

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
