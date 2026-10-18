from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_info_mode import SystemInfoMode
from ..models.system_info_status import SystemInfoStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mqtt_connections import MqttConnections
    from ..models.system_info_archive import SystemInfoArchive
    from ..models.system_info_catch_up import SystemInfoCatchUp
    from ..models.system_info_database import SystemInfoDatabase
    from ..models.system_info_exports import SystemInfoExports
    from ..models.system_info_features import SystemInfoFeatures
    from ..models.system_info_history import SystemInfoHistory
    from ..models.system_info_holds import SystemInfoHolds
    from ..models.system_info_ingest_writer import SystemInfoIngestWriter
    from ..models.system_info_limits import SystemInfoLimits
    from ..models.system_info_llm import SystemInfoLlm
    from ..models.system_info_mcp import SystemInfoMcp
    from ..models.system_info_notifications import SystemInfoNotifications
    from ..models.system_info_raster_formats import SystemInfoRasterFormats
    from ..models.system_info_records import SystemInfoRecords
    from ..models.system_info_scheduler import SystemInfoScheduler
    from ..models.system_info_search import SystemInfoSearch
    from ..models.system_info_security import SystemInfoSecurity
    from ..models.system_info_sinks import SystemInfoSinks
    from ..models.system_info_sources import SystemInfoSources
    from ..models.system_info_streams import SystemInfoStreams
    from ..models.system_info_warnings_item import SystemInfoWarningsItem
    from ..models.system_rasters import SystemRasters


T = TypeVar("T", bound="SystemInfo")


@_attrs_define
class SystemInfo:
    """System document. Anonymous callers get only version, apiVersion, startedAt, uptimeSeconds, status, features and
    limits; any valid token gets the full document.

        Attributes:
            version (str):
            started_at (datetime.datetime):
            uptime_seconds (int | Unset):
            database (SystemInfoDatabase | Unset):
            records (SystemInfoRecords | Unset):
            sources (SystemInfoSources | Unset):
            scheduler (SystemInfoScheduler | Unset):
            streams (SystemInfoStreams | Unset):
            notifications (SystemInfoNotifications | Unset):
            llm (SystemInfoLlm | Unset):
            search (SystemInfoSearch | Unset): Semantic search state (027)
            mcp (SystemInfoMcp | Unset): MCP server state (025)
            api_version (str | Unset): The spec's info.version
            status (SystemInfoStatus | Unset):
            features (SystemInfoFeatures | Unset): Capability flags from FeatureContributor beans (notifications, retention,
                later llm, search, push, mcp, weather …); 113 adds federation and federationServing, and in edge mode reports
                embeddings, llm, search, ask, rasters, briefings, ogc, exports and federationServing false
            limits (SystemInfoLimits | Unset):
            history (SystemInfoHistory | Unset): Per-tier history storage from pg_partition_tree('record_version')
            catch_up (SystemInfoCatchUp | Unset):
            ingest_writer (SystemInfoIngestWriter | Unset):
            warnings (list[SystemInfoWarningsItem] | Unset):
            security (SystemInfoSecurity | Unset): Security posture (023, 035)
            rasters (SystemRasters | Unset): Raster subsystem block of `/v1/system` (042, extended by 043, 045, 048)
            mqtt (MqttConnections | Unset): `SystemInfo.mqtt`: state of the supervised MQTT client connections (060
                channels, 083/085/086 sources)
            raster_formats (SystemInfoRasterFormats | Unset): 108: optional encoder health
            sinks (SystemInfoSinks | Unset): Outbound sinks
            exports (SystemInfoExports | Unset): Export jobs
            archive (SystemInfoArchive | Unset): Cold archive
            holds (SystemInfoHolds | Unset): Holds
            mode (SystemInfoMode | Unset): Deployment profile; in edge mode `features` reports embeddings, LLM, rasters,
                briefings, OGC, exports and federation serving off
    """

    version: str
    started_at: datetime.datetime
    uptime_seconds: int | Unset = UNSET
    database: SystemInfoDatabase | Unset = UNSET
    records: SystemInfoRecords | Unset = UNSET
    sources: SystemInfoSources | Unset = UNSET
    scheduler: SystemInfoScheduler | Unset = UNSET
    streams: SystemInfoStreams | Unset = UNSET
    notifications: SystemInfoNotifications | Unset = UNSET
    llm: SystemInfoLlm | Unset = UNSET
    search: SystemInfoSearch | Unset = UNSET
    mcp: SystemInfoMcp | Unset = UNSET
    api_version: str | Unset = UNSET
    status: SystemInfoStatus | Unset = UNSET
    features: SystemInfoFeatures | Unset = UNSET
    limits: SystemInfoLimits | Unset = UNSET
    history: SystemInfoHistory | Unset = UNSET
    catch_up: SystemInfoCatchUp | Unset = UNSET
    ingest_writer: SystemInfoIngestWriter | Unset = UNSET
    warnings: list[SystemInfoWarningsItem] | Unset = UNSET
    security: SystemInfoSecurity | Unset = UNSET
    rasters: SystemRasters | Unset = UNSET
    mqtt: MqttConnections | Unset = UNSET
    raster_formats: SystemInfoRasterFormats | Unset = UNSET
    sinks: SystemInfoSinks | Unset = UNSET
    exports: SystemInfoExports | Unset = UNSET
    archive: SystemInfoArchive | Unset = UNSET
    holds: SystemInfoHolds | Unset = UNSET
    mode: SystemInfoMode | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        version = self.version

        started_at = self.started_at.isoformat()

        uptime_seconds = self.uptime_seconds

        database: dict[str, Any] | Unset = UNSET
        if not isinstance(self.database, Unset):
            database = self.database.to_dict()

        records: dict[str, Any] | Unset = UNSET
        if not isinstance(self.records, Unset):
            records = self.records.to_dict()

        sources: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources.to_dict()

        scheduler: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scheduler, Unset):
            scheduler = self.scheduler.to_dict()

        streams: dict[str, Any] | Unset = UNSET
        if not isinstance(self.streams, Unset):
            streams = self.streams.to_dict()

        notifications: dict[str, Any] | Unset = UNSET
        if not isinstance(self.notifications, Unset):
            notifications = self.notifications.to_dict()

        llm: dict[str, Any] | Unset = UNSET
        if not isinstance(self.llm, Unset):
            llm = self.llm.to_dict()

        search: dict[str, Any] | Unset = UNSET
        if not isinstance(self.search, Unset):
            search = self.search.to_dict()

        mcp: dict[str, Any] | Unset = UNSET
        if not isinstance(self.mcp, Unset):
            mcp = self.mcp.to_dict()

        api_version = self.api_version

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        features: dict[str, Any] | Unset = UNSET
        if not isinstance(self.features, Unset):
            features = self.features.to_dict()

        limits: dict[str, Any] | Unset = UNSET
        if not isinstance(self.limits, Unset):
            limits = self.limits.to_dict()

        history: dict[str, Any] | Unset = UNSET
        if not isinstance(self.history, Unset):
            history = self.history.to_dict()

        catch_up: dict[str, Any] | Unset = UNSET
        if not isinstance(self.catch_up, Unset):
            catch_up = self.catch_up.to_dict()

        ingest_writer: dict[str, Any] | Unset = UNSET
        if not isinstance(self.ingest_writer, Unset):
            ingest_writer = self.ingest_writer.to_dict()

        warnings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = []
            for warnings_item_data in self.warnings:
                warnings_item = warnings_item_data.to_dict()
                warnings.append(warnings_item)

        security: dict[str, Any] | Unset = UNSET
        if not isinstance(self.security, Unset):
            security = self.security.to_dict()

        rasters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.rasters, Unset):
            rasters = self.rasters.to_dict()

        mqtt: dict[str, Any] | Unset = UNSET
        if not isinstance(self.mqtt, Unset):
            mqtt = self.mqtt.to_dict()

        raster_formats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.raster_formats, Unset):
            raster_formats = self.raster_formats.to_dict()

        sinks: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sinks, Unset):
            sinks = self.sinks.to_dict()

        exports: dict[str, Any] | Unset = UNSET
        if not isinstance(self.exports, Unset):
            exports = self.exports.to_dict()

        archive: dict[str, Any] | Unset = UNSET
        if not isinstance(self.archive, Unset):
            archive = self.archive.to_dict()

        holds: dict[str, Any] | Unset = UNSET
        if not isinstance(self.holds, Unset):
            holds = self.holds.to_dict()

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "startedAt": started_at,
            }
        )
        if uptime_seconds is not UNSET:
            field_dict["uptimeSeconds"] = uptime_seconds
        if database is not UNSET:
            field_dict["database"] = database
        if records is not UNSET:
            field_dict["records"] = records
        if sources is not UNSET:
            field_dict["sources"] = sources
        if scheduler is not UNSET:
            field_dict["scheduler"] = scheduler
        if streams is not UNSET:
            field_dict["streams"] = streams
        if notifications is not UNSET:
            field_dict["notifications"] = notifications
        if llm is not UNSET:
            field_dict["llm"] = llm
        if search is not UNSET:
            field_dict["search"] = search
        if mcp is not UNSET:
            field_dict["mcp"] = mcp
        if api_version is not UNSET:
            field_dict["apiVersion"] = api_version
        if status is not UNSET:
            field_dict["status"] = status
        if features is not UNSET:
            field_dict["features"] = features
        if limits is not UNSET:
            field_dict["limits"] = limits
        if history is not UNSET:
            field_dict["history"] = history
        if catch_up is not UNSET:
            field_dict["catchUp"] = catch_up
        if ingest_writer is not UNSET:
            field_dict["ingestWriter"] = ingest_writer
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if security is not UNSET:
            field_dict["security"] = security
        if rasters is not UNSET:
            field_dict["rasters"] = rasters
        if mqtt is not UNSET:
            field_dict["mqtt"] = mqtt
        if raster_formats is not UNSET:
            field_dict["rasterFormats"] = raster_formats
        if sinks is not UNSET:
            field_dict["sinks"] = sinks
        if exports is not UNSET:
            field_dict["exports"] = exports
        if archive is not UNSET:
            field_dict["archive"] = archive
        if holds is not UNSET:
            field_dict["holds"] = holds
        if mode is not UNSET:
            field_dict["mode"] = mode

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mqtt_connections import MqttConnections
        from ..models.system_info_archive import SystemInfoArchive
        from ..models.system_info_catch_up import SystemInfoCatchUp
        from ..models.system_info_database import SystemInfoDatabase
        from ..models.system_info_exports import SystemInfoExports
        from ..models.system_info_features import SystemInfoFeatures
        from ..models.system_info_history import SystemInfoHistory
        from ..models.system_info_holds import SystemInfoHolds
        from ..models.system_info_ingest_writer import SystemInfoIngestWriter
        from ..models.system_info_limits import SystemInfoLimits
        from ..models.system_info_llm import SystemInfoLlm
        from ..models.system_info_mcp import SystemInfoMcp
        from ..models.system_info_notifications import SystemInfoNotifications
        from ..models.system_info_raster_formats import SystemInfoRasterFormats
        from ..models.system_info_records import SystemInfoRecords
        from ..models.system_info_scheduler import SystemInfoScheduler
        from ..models.system_info_search import SystemInfoSearch
        from ..models.system_info_security import SystemInfoSecurity
        from ..models.system_info_sinks import SystemInfoSinks
        from ..models.system_info_sources import SystemInfoSources
        from ..models.system_info_streams import SystemInfoStreams
        from ..models.system_info_warnings_item import SystemInfoWarningsItem
        from ..models.system_rasters import SystemRasters

        d = dict(src_dict)
        version = d.pop("version")

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        uptime_seconds = d.pop("uptimeSeconds", UNSET)

        _database = d.pop("database", UNSET)
        database: SystemInfoDatabase | Unset
        if isinstance(_database, Unset):
            database = UNSET
        else:
            database = SystemInfoDatabase.from_dict(_database)

        _records = d.pop("records", UNSET)
        records: SystemInfoRecords | Unset
        if isinstance(_records, Unset):
            records = UNSET
        else:
            records = SystemInfoRecords.from_dict(_records)

        _sources = d.pop("sources", UNSET)
        sources: SystemInfoSources | Unset
        if isinstance(_sources, Unset):
            sources = UNSET
        else:
            sources = SystemInfoSources.from_dict(_sources)

        _scheduler = d.pop("scheduler", UNSET)
        scheduler: SystemInfoScheduler | Unset
        if isinstance(_scheduler, Unset):
            scheduler = UNSET
        else:
            scheduler = SystemInfoScheduler.from_dict(_scheduler)

        _streams = d.pop("streams", UNSET)
        streams: SystemInfoStreams | Unset
        if isinstance(_streams, Unset):
            streams = UNSET
        else:
            streams = SystemInfoStreams.from_dict(_streams)

        _notifications = d.pop("notifications", UNSET)
        notifications: SystemInfoNotifications | Unset
        if isinstance(_notifications, Unset):
            notifications = UNSET
        else:
            notifications = SystemInfoNotifications.from_dict(_notifications)

        _llm = d.pop("llm", UNSET)
        llm: SystemInfoLlm | Unset
        if isinstance(_llm, Unset):
            llm = UNSET
        else:
            llm = SystemInfoLlm.from_dict(_llm)

        _search = d.pop("search", UNSET)
        search: SystemInfoSearch | Unset
        if isinstance(_search, Unset):
            search = UNSET
        else:
            search = SystemInfoSearch.from_dict(_search)

        _mcp = d.pop("mcp", UNSET)
        mcp: SystemInfoMcp | Unset
        if isinstance(_mcp, Unset):
            mcp = UNSET
        else:
            mcp = SystemInfoMcp.from_dict(_mcp)

        api_version = d.pop("apiVersion", UNSET)

        _status = d.pop("status", UNSET)
        status: SystemInfoStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = SystemInfoStatus(_status)

        _features = d.pop("features", UNSET)
        features: SystemInfoFeatures | Unset
        if isinstance(_features, Unset):
            features = UNSET
        else:
            features = SystemInfoFeatures.from_dict(_features)

        _limits = d.pop("limits", UNSET)
        limits: SystemInfoLimits | Unset
        if isinstance(_limits, Unset):
            limits = UNSET
        else:
            limits = SystemInfoLimits.from_dict(_limits)

        _history = d.pop("history", UNSET)
        history: SystemInfoHistory | Unset
        if isinstance(_history, Unset):
            history = UNSET
        else:
            history = SystemInfoHistory.from_dict(_history)

        _catch_up = d.pop("catchUp", UNSET)
        catch_up: SystemInfoCatchUp | Unset
        if isinstance(_catch_up, Unset):
            catch_up = UNSET
        else:
            catch_up = SystemInfoCatchUp.from_dict(_catch_up)

        _ingest_writer = d.pop("ingestWriter", UNSET)
        ingest_writer: SystemInfoIngestWriter | Unset
        if isinstance(_ingest_writer, Unset):
            ingest_writer = UNSET
        else:
            ingest_writer = SystemInfoIngestWriter.from_dict(_ingest_writer)

        _warnings = d.pop("warnings", UNSET)
        warnings: list[SystemInfoWarningsItem] | Unset = UNSET
        if _warnings is not UNSET:
            warnings = []
            for warnings_item_data in _warnings:
                warnings_item = SystemInfoWarningsItem.from_dict(warnings_item_data)

                warnings.append(warnings_item)

        _security = d.pop("security", UNSET)
        security: SystemInfoSecurity | Unset
        if isinstance(_security, Unset):
            security = UNSET
        else:
            security = SystemInfoSecurity.from_dict(_security)

        _rasters = d.pop("rasters", UNSET)
        rasters: SystemRasters | Unset
        if isinstance(_rasters, Unset):
            rasters = UNSET
        else:
            rasters = SystemRasters.from_dict(_rasters)

        _mqtt = d.pop("mqtt", UNSET)
        mqtt: MqttConnections | Unset
        if isinstance(_mqtt, Unset):
            mqtt = UNSET
        else:
            mqtt = MqttConnections.from_dict(_mqtt)

        _raster_formats = d.pop("rasterFormats", UNSET)
        raster_formats: SystemInfoRasterFormats | Unset
        if isinstance(_raster_formats, Unset):
            raster_formats = UNSET
        else:
            raster_formats = SystemInfoRasterFormats.from_dict(_raster_formats)

        _sinks = d.pop("sinks", UNSET)
        sinks: SystemInfoSinks | Unset
        if isinstance(_sinks, Unset):
            sinks = UNSET
        else:
            sinks = SystemInfoSinks.from_dict(_sinks)

        _exports = d.pop("exports", UNSET)
        exports: SystemInfoExports | Unset
        if isinstance(_exports, Unset):
            exports = UNSET
        else:
            exports = SystemInfoExports.from_dict(_exports)

        _archive = d.pop("archive", UNSET)
        archive: SystemInfoArchive | Unset
        if isinstance(_archive, Unset):
            archive = UNSET
        else:
            archive = SystemInfoArchive.from_dict(_archive)

        _holds = d.pop("holds", UNSET)
        holds: SystemInfoHolds | Unset
        if isinstance(_holds, Unset):
            holds = UNSET
        else:
            holds = SystemInfoHolds.from_dict(_holds)

        _mode = d.pop("mode", UNSET)
        mode: SystemInfoMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = SystemInfoMode(_mode)

        system_info = cls(
            version=version,
            started_at=started_at,
            uptime_seconds=uptime_seconds,
            database=database,
            records=records,
            sources=sources,
            scheduler=scheduler,
            streams=streams,
            notifications=notifications,
            llm=llm,
            search=search,
            mcp=mcp,
            api_version=api_version,
            status=status,
            features=features,
            limits=limits,
            history=history,
            catch_up=catch_up,
            ingest_writer=ingest_writer,
            warnings=warnings,
            security=security,
            rasters=rasters,
            mqtt=mqtt,
            raster_formats=raster_formats,
            sinks=sinks,
            exports=exports,
            archive=archive,
            holds=holds,
            mode=mode,
        )

        system_info.additional_properties = d
        return system_info

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
