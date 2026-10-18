from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FireClusterPayload")


@_attrs_define
class FireClusterPayload:
    """Payload of `fire-cluster` records (goes-fdc 8-connected components)

    Attributes:
        pixels (int):
        label (str):
        frp_total_mw (float | Unset):
        frp_max_mw (float | Unset):
        first_seen_at (datetime.datetime | Unset):
    """

    pixels: int
    label: str
    frp_total_mw: float | Unset = UNSET
    frp_max_mw: float | Unset = UNSET
    first_seen_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pixels = self.pixels

        label = self.label

        frp_total_mw = self.frp_total_mw

        frp_max_mw = self.frp_max_mw

        first_seen_at: str | Unset = UNSET
        if not isinstance(self.first_seen_at, Unset):
            first_seen_at = self.first_seen_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "pixels": pixels,
                "label": label,
            }
        )
        if frp_total_mw is not UNSET:
            field_dict["frpTotalMw"] = frp_total_mw
        if frp_max_mw is not UNSET:
            field_dict["frpMaxMw"] = frp_max_mw
        if first_seen_at is not UNSET:
            field_dict["firstSeenAt"] = first_seen_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        pixels = d.pop("pixels")

        label = d.pop("label")

        frp_total_mw = d.pop("frpTotalMw", UNSET)

        frp_max_mw = d.pop("frpMaxMw", UNSET)

        _first_seen_at = d.pop("firstSeenAt", UNSET)
        first_seen_at: datetime.datetime | Unset
        if isinstance(_first_seen_at, Unset):
            first_seen_at = UNSET
        else:
            first_seen_at = datetime.datetime.fromisoformat(_first_seen_at)

        fire_cluster_payload = cls(
            pixels=pixels,
            label=label,
            frp_total_mw=frp_total_mw,
            frp_max_mw=frp_max_mw,
            first_seen_at=first_seen_at,
        )

        fire_cluster_payload.additional_properties = d
        return fire_cluster_payload

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
