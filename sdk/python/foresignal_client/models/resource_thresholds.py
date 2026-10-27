from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.resource_threshold import ResourceThreshold
    from ..models.resource_thresholds_collector_backlog_type_0 import (
        ResourceThresholdsCollectorBacklogType0,
    )
    from ..models.resource_thresholds_source_storm_type_0 import (
        ResourceThresholdsSourceStormType0,
    )


T = TypeVar("T", bound="ResourceThresholds")


@_attrs_define
class ResourceThresholds:
    """Admin thresholds that raise /v1/system warnings and fire channel alerts when breached (B1/177). A PUT replaces the
    whole value: a missing member takes its default (disk 15 % warn / 5 % critical free, memory off, a storm at 5
    failing sources, a backlog of 10,000 seq for 300 s) and an explicit null turns it off.

        Attributes:
            disk (ResourceThreshold | Unset): Warn/critical thresholds for one resource (free space or memory). A level is
                reached when the free share drops below its percent or the free amount below its bytes; a null level is off.
                Critical must be below warn.
            memory (ResourceThreshold | Unset): Warn/critical thresholds for one resource (free space or memory). A level is
                reached when the free share drops below its percent or the free amount below its bytes; a null level is off.
                Critical must be below warn.
            alert_channels (list[str] | Unset): Channel ids notified on a breach, personal channels included, whatever their
                event filters. Empty uses every channel subscribed to `system` plus the personal channels of every active user
                with `audit.read`. An unknown id is a 400 that lists it.
            source_storm (None | ResourceThresholdsSourceStormType0 | Unset): Raises `system.source_storm` when at least
                this many sources are failing at once (R2-014)
            collector_backlog (None | ResourceThresholdsCollectorBacklogType0 | Unset): Raises `system.collector_backlog`
                when the trigger lag or the fetches waiting for a pull permit stay above their bound for `sustainedSeconds`
                (R2-014)
    """

    disk: ResourceThreshold | Unset = UNSET
    memory: ResourceThreshold | Unset = UNSET
    alert_channels: list[str] | Unset = UNSET
    source_storm: None | ResourceThresholdsSourceStormType0 | Unset = UNSET
    collector_backlog: None | ResourceThresholdsCollectorBacklogType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.resource_thresholds_collector_backlog_type_0 import (
            ResourceThresholdsCollectorBacklogType0,
        )
        from ..models.resource_thresholds_source_storm_type_0 import (
            ResourceThresholdsSourceStormType0,
        )

        disk: dict[str, Any] | Unset = UNSET
        if not isinstance(self.disk, Unset):
            disk = self.disk.to_dict()

        memory: dict[str, Any] | Unset = UNSET
        if not isinstance(self.memory, Unset):
            memory = self.memory.to_dict()

        alert_channels: list[str] | Unset = UNSET
        if not isinstance(self.alert_channels, Unset):
            alert_channels = self.alert_channels

        source_storm: dict[str, Any] | None | Unset
        if isinstance(self.source_storm, Unset):
            source_storm = UNSET
        elif isinstance(self.source_storm, ResourceThresholdsSourceStormType0):
            source_storm = self.source_storm.to_dict()
        else:
            source_storm = self.source_storm

        collector_backlog: dict[str, Any] | None | Unset
        if isinstance(self.collector_backlog, Unset):
            collector_backlog = UNSET
        elif isinstance(
            self.collector_backlog, ResourceThresholdsCollectorBacklogType0
        ):
            collector_backlog = self.collector_backlog.to_dict()
        else:
            collector_backlog = self.collector_backlog

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if disk is not UNSET:
            field_dict["disk"] = disk
        if memory is not UNSET:
            field_dict["memory"] = memory
        if alert_channels is not UNSET:
            field_dict["alertChannels"] = alert_channels
        if source_storm is not UNSET:
            field_dict["sourceStorm"] = source_storm
        if collector_backlog is not UNSET:
            field_dict["collectorBacklog"] = collector_backlog

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.resource_threshold import ResourceThreshold
        from ..models.resource_thresholds_collector_backlog_type_0 import (
            ResourceThresholdsCollectorBacklogType0,
        )
        from ..models.resource_thresholds_source_storm_type_0 import (
            ResourceThresholdsSourceStormType0,
        )

        d = dict(src_dict)
        _disk = d.pop("disk", UNSET)
        disk: ResourceThreshold | Unset
        if isinstance(_disk, Unset):
            disk = UNSET
        else:
            disk = ResourceThreshold.from_dict(_disk)

        _memory = d.pop("memory", UNSET)
        memory: ResourceThreshold | Unset
        if isinstance(_memory, Unset):
            memory = UNSET
        else:
            memory = ResourceThreshold.from_dict(_memory)

        alert_channels = cast(list[str], d.pop("alertChannels", UNSET))

        def _parse_source_storm(
            data: object,
        ) -> None | ResourceThresholdsSourceStormType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                source_storm_type_0 = ResourceThresholdsSourceStormType0.from_dict(data)

                return source_storm_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ResourceThresholdsSourceStormType0 | Unset, data)

        source_storm = _parse_source_storm(d.pop("sourceStorm", UNSET))

        def _parse_collector_backlog(
            data: object,
        ) -> None | ResourceThresholdsCollectorBacklogType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                collector_backlog_type_0 = (
                    ResourceThresholdsCollectorBacklogType0.from_dict(data)
                )

                return collector_backlog_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ResourceThresholdsCollectorBacklogType0 | Unset, data)

        collector_backlog = _parse_collector_backlog(d.pop("collectorBacklog", UNSET))

        resource_thresholds = cls(
            disk=disk,
            memory=memory,
            alert_channels=alert_channels,
            source_storm=source_storm,
            collector_backlog=collector_backlog,
        )

        resource_thresholds.additional_properties = d
        return resource_thresholds

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
