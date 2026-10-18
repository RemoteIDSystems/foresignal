from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.object_envelope_text_origin import ObjectEnvelopeTextOrigin
from ..models.sensor_sensor_kind import SensorSensorKind
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.object_action import ObjectAction
    from ..models.object_envelope_links import ObjectEnvelopeLinks
    from ..models.object_envelope_time import ObjectEnvelopeTime
    from ..models.provenance import Provenance
    from ..models.sensor_capabilities_type_0 import SensorCapabilitiesType0
    from ..models.sensor_passes_item import SensorPassesItem


T = TypeVar("T", bound="Sensor")


@_attrs_define
class Sensor:
    """Sensor envelope: something that can observe an area in a window (no tasking)

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (str):
        title (str):
        sensor_kind (SensorSensorKind):
        summary (None | str | Unset):
        text_origin (ObjectEnvelopeTextOrigin | Unset):
        status (None | str | Unset):
        severity (None | str | Unset):
        time (ObjectEnvelopeTime | Unset):
        geometry (Geometry | None | Unset):
        bbox (list[float] | None | Unset):
        private (bool | Unset):
        tlp (None | Tlp | Unset):
        tags (list[str] | Unset):
        provenance (Provenance | Unset):
        links (ObjectEnvelopeLinks | Unset):
        actions (list[ObjectAction] | Unset):
        p_detect (float | None | Unset):
        method (None | str | Unset):
        families (list[str] | Unset):
        range_km (float | None | Unset):
        passes (list[SensorPassesItem] | Unset):
        capabilities (None | SensorCapabilitiesType0 | Unset):
        footprint (Geometry | None | Unset):
        acquired_at (datetime.datetime | None | Unset):
    """

    ref: str
    type_: str
    title: str
    sensor_kind: SensorSensorKind
    summary: None | str | Unset = UNSET
    text_origin: ObjectEnvelopeTextOrigin | Unset = UNSET
    status: None | str | Unset = UNSET
    severity: None | str | Unset = UNSET
    time: ObjectEnvelopeTime | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    private: bool | Unset = UNSET
    tlp: None | Tlp | Unset = UNSET
    tags: list[str] | Unset = UNSET
    provenance: Provenance | Unset = UNSET
    links: ObjectEnvelopeLinks | Unset = UNSET
    actions: list[ObjectAction] | Unset = UNSET
    p_detect: float | None | Unset = UNSET
    method: None | str | Unset = UNSET
    families: list[str] | Unset = UNSET
    range_km: float | None | Unset = UNSET
    passes: list[SensorPassesItem] | Unset = UNSET
    capabilities: None | SensorCapabilitiesType0 | Unset = UNSET
    footprint: Geometry | None | Unset = UNSET
    acquired_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.sensor_capabilities_type_0 import SensorCapabilitiesType0

        ref = self.ref

        type_ = self.type_

        title = self.title

        sensor_kind = self.sensor_kind.value

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

        text_origin: str | Unset = UNSET
        if not isinstance(self.text_origin, Unset):
            text_origin = self.text_origin.value

        status: None | str | Unset
        if isinstance(self.status, Unset):
            status = UNSET
        else:
            status = self.status

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        else:
            severity = self.severity

        time: dict[str, Any] | Unset = UNSET
        if not isinstance(self.time, Unset):
            time = self.time.to_dict()

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        private = self.private

        tlp: None | str | Unset
        if isinstance(self.tlp, Unset):
            tlp = UNSET
        elif isinstance(self.tlp, Tlp):
            tlp = self.tlp.value
        else:
            tlp = self.tlp

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        provenance: dict[str, Any] | Unset = UNSET
        if not isinstance(self.provenance, Unset):
            provenance = self.provenance.to_dict()

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        actions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.actions, Unset):
            actions = []
            for actions_item_data in self.actions:
                actions_item = actions_item_data.to_dict()
                actions.append(actions_item)

        p_detect: float | None | Unset
        if isinstance(self.p_detect, Unset):
            p_detect = UNSET
        else:
            p_detect = self.p_detect

        method: None | str | Unset
        if isinstance(self.method, Unset):
            method = UNSET
        else:
            method = self.method

        families: list[str] | Unset = UNSET
        if not isinstance(self.families, Unset):
            families = self.families

        range_km: float | None | Unset
        if isinstance(self.range_km, Unset):
            range_km = UNSET
        else:
            range_km = self.range_km

        passes: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.passes, Unset):
            passes = []
            for passes_item_data in self.passes:
                passes_item = passes_item_data.to_dict()
                passes.append(passes_item)

        capabilities: dict[str, Any] | None | Unset
        if isinstance(self.capabilities, Unset):
            capabilities = UNSET
        elif isinstance(self.capabilities, SensorCapabilitiesType0):
            capabilities = self.capabilities.to_dict()
        else:
            capabilities = self.capabilities

        footprint: dict[str, Any] | None | Unset
        if isinstance(self.footprint, Unset):
            footprint = UNSET
        elif isinstance(self.footprint, Geometry):
            footprint = self.footprint.to_dict()
        else:
            footprint = self.footprint

        acquired_at: None | str | Unset
        if isinstance(self.acquired_at, Unset):
            acquired_at = UNSET
        elif isinstance(self.acquired_at, datetime.datetime):
            acquired_at = self.acquired_at.isoformat()
        else:
            acquired_at = self.acquired_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "type": type_,
                "title": title,
                "sensorKind": sensor_kind,
            }
        )
        if summary is not UNSET:
            field_dict["summary"] = summary
        if text_origin is not UNSET:
            field_dict["textOrigin"] = text_origin
        if status is not UNSET:
            field_dict["status"] = status
        if severity is not UNSET:
            field_dict["severity"] = severity
        if time is not UNSET:
            field_dict["time"] = time
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if private is not UNSET:
            field_dict["private"] = private
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if tags is not UNSET:
            field_dict["tags"] = tags
        if provenance is not UNSET:
            field_dict["provenance"] = provenance
        if links is not UNSET:
            field_dict["links"] = links
        if actions is not UNSET:
            field_dict["actions"] = actions
        if p_detect is not UNSET:
            field_dict["pDetect"] = p_detect
        if method is not UNSET:
            field_dict["method"] = method
        if families is not UNSET:
            field_dict["families"] = families
        if range_km is not UNSET:
            field_dict["rangeKm"] = range_km
        if passes is not UNSET:
            field_dict["passes"] = passes
        if capabilities is not UNSET:
            field_dict["capabilities"] = capabilities
        if footprint is not UNSET:
            field_dict["footprint"] = footprint
        if acquired_at is not UNSET:
            field_dict["acquiredAt"] = acquired_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.object_action import ObjectAction
        from ..models.object_envelope_links import ObjectEnvelopeLinks
        from ..models.object_envelope_time import ObjectEnvelopeTime
        from ..models.provenance import Provenance
        from ..models.sensor_capabilities_type_0 import SensorCapabilitiesType0
        from ..models.sensor_passes_item import SensorPassesItem

        d = dict(src_dict)
        ref = d.pop("ref")

        type_ = d.pop("type")

        title = d.pop("title")

        sensor_kind = SensorSensorKind(d.pop("sensorKind"))

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

        _text_origin = d.pop("textOrigin", UNSET)
        text_origin: ObjectEnvelopeTextOrigin | Unset
        if isinstance(_text_origin, Unset):
            text_origin = UNSET
        else:
            text_origin = ObjectEnvelopeTextOrigin(_text_origin)

        def _parse_status(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status = _parse_status(d.pop("status", UNSET))

        def _parse_severity(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        _time = d.pop("time", UNSET)
        time: ObjectEnvelopeTime | Unset
        if isinstance(_time, Unset):
            time = UNSET
        else:
            time = ObjectEnvelopeTime.from_dict(_time)

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        private = d.pop("private", UNSET)

        def _parse_tlp(data: object) -> None | Tlp | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                tlp_type_0 = Tlp(data)

                return tlp_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Tlp | Unset, data)

        tlp = _parse_tlp(d.pop("tlp", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        _provenance = d.pop("provenance", UNSET)
        provenance: Provenance | Unset
        if isinstance(_provenance, Unset):
            provenance = UNSET
        else:
            provenance = Provenance.from_dict(_provenance)

        _links = d.pop("links", UNSET)
        links: ObjectEnvelopeLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = ObjectEnvelopeLinks.from_dict(_links)

        _actions = d.pop("actions", UNSET)
        actions: list[ObjectAction] | Unset = UNSET
        if _actions is not UNSET:
            actions = []
            for actions_item_data in _actions:
                actions_item = ObjectAction.from_dict(actions_item_data)

                actions.append(actions_item)

        def _parse_p_detect(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_detect = _parse_p_detect(d.pop("pDetect", UNSET))

        def _parse_method(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        method = _parse_method(d.pop("method", UNSET))

        families = cast(list[str], d.pop("families", UNSET))

        def _parse_range_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        range_km = _parse_range_km(d.pop("rangeKm", UNSET))

        _passes = d.pop("passes", UNSET)
        passes: list[SensorPassesItem] | Unset = UNSET
        if _passes is not UNSET:
            passes = []
            for passes_item_data in _passes:
                passes_item = SensorPassesItem.from_dict(passes_item_data)

                passes.append(passes_item)

        def _parse_capabilities(data: object) -> None | SensorCapabilitiesType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                capabilities_type_0 = SensorCapabilitiesType0.from_dict(data)

                return capabilities_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SensorCapabilitiesType0 | Unset, data)

        capabilities = _parse_capabilities(d.pop("capabilities", UNSET))

        def _parse_footprint(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                footprint_type_0 = Geometry.from_dict(data)

                return footprint_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        footprint = _parse_footprint(d.pop("footprint", UNSET))

        def _parse_acquired_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                acquired_at_type_0 = datetime.datetime.fromisoformat(data)

                return acquired_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        acquired_at = _parse_acquired_at(d.pop("acquiredAt", UNSET))

        sensor = cls(
            ref=ref,
            type_=type_,
            title=title,
            sensor_kind=sensor_kind,
            summary=summary,
            text_origin=text_origin,
            status=status,
            severity=severity,
            time=time,
            geometry=geometry,
            bbox=bbox,
            private=private,
            tlp=tlp,
            tags=tags,
            provenance=provenance,
            links=links,
            actions=actions,
            p_detect=p_detect,
            method=method,
            families=families,
            range_km=range_km,
            passes=passes,
            capabilities=capabilities,
            footprint=footprint,
            acquired_at=acquired_at,
        )

        sensor.additional_properties = d
        return sensor

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
