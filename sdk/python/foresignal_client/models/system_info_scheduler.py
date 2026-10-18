from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoScheduler")


@_attrs_define
class SystemInfoScheduler:
    """
    Attributes:
        running (bool | Unset):
        scheduled_sources (int | Unset):
        active_fetches (int | Unset):
        active_streams (int | Unset):
        ingest_queue_depth (int | Unset):
    """

    running: bool | Unset = UNSET
    scheduled_sources: int | Unset = UNSET
    active_fetches: int | Unset = UNSET
    active_streams: int | Unset = UNSET
    ingest_queue_depth: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        running = self.running

        scheduled_sources = self.scheduled_sources

        active_fetches = self.active_fetches

        active_streams = self.active_streams

        ingest_queue_depth = self.ingest_queue_depth

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if running is not UNSET:
            field_dict["running"] = running
        if scheduled_sources is not UNSET:
            field_dict["scheduledSources"] = scheduled_sources
        if active_fetches is not UNSET:
            field_dict["activeFetches"] = active_fetches
        if active_streams is not UNSET:
            field_dict["activeStreams"] = active_streams
        if ingest_queue_depth is not UNSET:
            field_dict["ingestQueueDepth"] = ingest_queue_depth

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        running = d.pop("running", UNSET)

        scheduled_sources = d.pop("scheduledSources", UNSET)

        active_fetches = d.pop("activeFetches", UNSET)

        active_streams = d.pop("activeStreams", UNSET)

        ingest_queue_depth = d.pop("ingestQueueDepth", UNSET)

        system_info_scheduler = cls(
            running=running,
            scheduled_sources=scheduled_sources,
            active_fetches=active_fetches,
            active_streams=active_streams,
            ingest_queue_depth=ingest_queue_depth,
        )

        system_info_scheduler.additional_properties = d
        return system_info_scheduler

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
