from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SourceStorage")


@_attrs_define
class SourceStorage:
    """Storage cost of a source (Core-Data-Model §15), from source_usage

    Attributes:
        observations_per_minute (float | Unset):
        versions_per_minute (float | Unset):
        persist_ratio (float | Unset): versions / observations
        trigger_edge_share (float | Unset): Share of versions persisted as TRIGGER_EDGE over the last hour (warning
            above 0.2)
        bytes_per_day (int | Unset): Version bytes per day (7-day average)
        projected_bytes (int | Unset): bytesPerDay × retention window
        late_last_hour (int | Unset):
        clamped_last_hour (int | Unset):
    """

    observations_per_minute: float | Unset = UNSET
    versions_per_minute: float | Unset = UNSET
    persist_ratio: float | Unset = UNSET
    trigger_edge_share: float | Unset = UNSET
    bytes_per_day: int | Unset = UNSET
    projected_bytes: int | Unset = UNSET
    late_last_hour: int | Unset = UNSET
    clamped_last_hour: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        observations_per_minute = self.observations_per_minute

        versions_per_minute = self.versions_per_minute

        persist_ratio = self.persist_ratio

        trigger_edge_share = self.trigger_edge_share

        bytes_per_day = self.bytes_per_day

        projected_bytes = self.projected_bytes

        late_last_hour = self.late_last_hour

        clamped_last_hour = self.clamped_last_hour

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if observations_per_minute is not UNSET:
            field_dict["observationsPerMinute"] = observations_per_minute
        if versions_per_minute is not UNSET:
            field_dict["versionsPerMinute"] = versions_per_minute
        if persist_ratio is not UNSET:
            field_dict["persistRatio"] = persist_ratio
        if trigger_edge_share is not UNSET:
            field_dict["triggerEdgeShare"] = trigger_edge_share
        if bytes_per_day is not UNSET:
            field_dict["bytesPerDay"] = bytes_per_day
        if projected_bytes is not UNSET:
            field_dict["projectedBytes"] = projected_bytes
        if late_last_hour is not UNSET:
            field_dict["lateLastHour"] = late_last_hour
        if clamped_last_hour is not UNSET:
            field_dict["clampedLastHour"] = clamped_last_hour

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        observations_per_minute = d.pop("observationsPerMinute", UNSET)

        versions_per_minute = d.pop("versionsPerMinute", UNSET)

        persist_ratio = d.pop("persistRatio", UNSET)

        trigger_edge_share = d.pop("triggerEdgeShare", UNSET)

        bytes_per_day = d.pop("bytesPerDay", UNSET)

        projected_bytes = d.pop("projectedBytes", UNSET)

        late_last_hour = d.pop("lateLastHour", UNSET)

        clamped_last_hour = d.pop("clampedLastHour", UNSET)

        source_storage = cls(
            observations_per_minute=observations_per_minute,
            versions_per_minute=versions_per_minute,
            persist_ratio=persist_ratio,
            trigger_edge_share=trigger_edge_share,
            bytes_per_day=bytes_per_day,
            projected_bytes=projected_bytes,
            late_last_hour=late_last_hour,
            clamped_last_hour=clamped_last_hour,
        )

        source_storage.additional_properties = d
        return source_storage

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
