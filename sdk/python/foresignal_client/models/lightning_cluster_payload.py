from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.lightning_cluster_payload_flashes_5m import (
    LightningClusterPayloadFlashes5M,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="LightningClusterPayload")


@_attrs_define
class LightningClusterPayload:
    """Payload of `lightning-cluster` records (goes-glm DBSCAN)

    Attributes:
        flashes5m (LightningClusterPayloadFlashes5M): Bucket lower bound
        label (str):
        automated (bool):
        not_a_warning (bool):
        radius_km (float | Unset):
        max_energy_fj (float | Unset): Bucketed
    """

    flashes5m: LightningClusterPayloadFlashes5M
    label: str
    automated: bool
    not_a_warning: bool
    radius_km: float | Unset = UNSET
    max_energy_fj: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        flashes5m = self.flashes5m.value

        label = self.label

        automated = self.automated

        not_a_warning = self.not_a_warning

        radius_km = self.radius_km

        max_energy_fj = self.max_energy_fj

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "flashes5m": flashes5m,
                "label": label,
                "automated": automated,
                "notAWarning": not_a_warning,
            }
        )
        if radius_km is not UNSET:
            field_dict["radiusKm"] = radius_km
        if max_energy_fj is not UNSET:
            field_dict["maxEnergyFj"] = max_energy_fj

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        flashes5m = LightningClusterPayloadFlashes5M(d.pop("flashes5m"))

        label = d.pop("label")

        automated = d.pop("automated")

        not_a_warning = d.pop("notAWarning")

        radius_km = d.pop("radiusKm", UNSET)

        max_energy_fj = d.pop("maxEnergyFj", UNSET)

        lightning_cluster_payload = cls(
            flashes5m=flashes5m,
            label=label,
            automated=automated,
            not_a_warning=not_a_warning,
            radius_km=radius_km,
            max_energy_fj=max_energy_fj,
        )

        lightning_cluster_payload.additional_properties = d
        return lightning_cluster_payload

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
