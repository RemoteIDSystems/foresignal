from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.live_event_properties import LiveEventProperties
    from ..models.quality import Quality


T = TypeVar("T", bound="LiveEvent")


@_attrs_define
class LiveEvent:
    """SSE `live` event: a non-persisted position or kinematic update (conflated per entity, no `id:` line)

    Attributes:
        source_id (str):
        external_id (str):
        kind (str):
        observed_at (datetime.datetime):
        version (int): Persisted version this live state builds on
        valid_until (datetime.datetime | None | Unset):
        geometry (Geometry | None | Unset):
        bbox (list[float] | None | Unset): Current bounding box
        prev_bbox (list[float] | None | Unset): Bounding box before this update (used by the spatial filter)
        properties (LiveEventProperties | Unset): Kinematic (non-significant) properties after this update, over the
            persisted payload; a JSON null value means the key was removed
        quality (None | Quality | Unset):
        left_filter (bool | Unset): True when only `prevBbox` matched the spatial filter: remove the feature Default:
            False.
    """

    source_id: str
    external_id: str
    kind: str
    observed_at: datetime.datetime
    version: int
    valid_until: datetime.datetime | None | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    prev_bbox: list[float] | None | Unset = UNSET
    properties: LiveEventProperties | Unset = UNSET
    quality: None | Quality | Unset = UNSET
    left_filter: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.quality import Quality

        source_id = self.source_id

        external_id = self.external_id

        kind = self.kind

        observed_at = self.observed_at.isoformat()

        version = self.version

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

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

        prev_bbox: list[float] | None | Unset
        if isinstance(self.prev_bbox, Unset):
            prev_bbox = UNSET
        elif isinstance(self.prev_bbox, list):
            prev_bbox = self.prev_bbox

        else:
            prev_bbox = self.prev_bbox

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        quality: dict[str, Any] | None | Unset
        if isinstance(self.quality, Unset):
            quality = UNSET
        elif isinstance(self.quality, Quality):
            quality = self.quality.to_dict()
        else:
            quality = self.quality

        left_filter = self.left_filter

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "externalId": external_id,
                "kind": kind,
                "observedAt": observed_at,
                "version": version,
            }
        )
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if prev_bbox is not UNSET:
            field_dict["prevBbox"] = prev_bbox
        if properties is not UNSET:
            field_dict["properties"] = properties
        if quality is not UNSET:
            field_dict["quality"] = quality
        if left_filter is not UNSET:
            field_dict["leftFilter"] = left_filter

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.live_event_properties import LiveEventProperties
        from ..models.quality import Quality

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        kind = d.pop("kind")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        version = d.pop("version")

        def _parse_valid_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_until_type_0 = datetime.datetime.fromisoformat(data)

                return valid_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_until = _parse_valid_until(d.pop("validUntil", UNSET))

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

        def _parse_prev_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                prev_bbox_type_0 = cast(list[float], data)

                return prev_bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        prev_bbox = _parse_prev_bbox(d.pop("prevBbox", UNSET))

        _properties = d.pop("properties", UNSET)
        properties: LiveEventProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = LiveEventProperties.from_dict(_properties)

        def _parse_quality(data: object) -> None | Quality | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quality_type_0 = Quality.from_dict(data)

                return quality_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Quality | Unset, data)

        quality = _parse_quality(d.pop("quality", UNSET))

        left_filter = d.pop("leftFilter", UNSET)

        live_event = cls(
            source_id=source_id,
            external_id=external_id,
            kind=kind,
            observed_at=observed_at,
            version=version,
            valid_until=valid_until,
            geometry=geometry,
            bbox=bbox,
            prev_bbox=prev_bbox,
            properties=properties,
            quality=quality,
            left_filter=left_filter,
        )

        live_event.additional_properties = d
        return live_event

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
