from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CyclePacketCoverage")


@_attrs_define
class CyclePacketCoverage:
    """Coverage caveats

    Attributes:
        failing_sources (list[str] | Unset):
        degraded_sources (list[str] | Unset):
        low_reporting_density_regions (list[str] | Unset):
    """

    failing_sources: list[str] | Unset = UNSET
    degraded_sources: list[str] | Unset = UNSET
    low_reporting_density_regions: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        failing_sources: list[str] | Unset = UNSET
        if not isinstance(self.failing_sources, Unset):
            failing_sources = self.failing_sources

        degraded_sources: list[str] | Unset = UNSET
        if not isinstance(self.degraded_sources, Unset):
            degraded_sources = self.degraded_sources

        low_reporting_density_regions: list[str] | Unset = UNSET
        if not isinstance(self.low_reporting_density_regions, Unset):
            low_reporting_density_regions = self.low_reporting_density_regions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if failing_sources is not UNSET:
            field_dict["failingSources"] = failing_sources
        if degraded_sources is not UNSET:
            field_dict["degradedSources"] = degraded_sources
        if low_reporting_density_regions is not UNSET:
            field_dict["lowReportingDensityRegions"] = low_reporting_density_regions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        failing_sources = cast(list[str], d.pop("failingSources", UNSET))

        degraded_sources = cast(list[str], d.pop("degradedSources", UNSET))

        low_reporting_density_regions = cast(
            list[str], d.pop("lowReportingDensityRegions", UNSET)
        )

        cycle_packet_coverage = cls(
            failing_sources=failing_sources,
            degraded_sources=degraded_sources,
            low_reporting_density_regions=low_reporting_density_regions,
        )

        cycle_packet_coverage.additional_properties = d
        return cycle_packet_coverage

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
