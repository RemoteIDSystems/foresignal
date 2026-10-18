from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_feature_item_properties import EdpFeatureItemProperties
    from ..models.edp_geometry import EdpGeometry
    from ..models.edp_origin import EdpOrigin


T = TypeVar("T", bound="EdpFeatureItem")


@_attrs_define
class EdpFeatureItem:
    """Current state of an entity (layer streams)

    Attributes:
        stream (str):
        type_ (Literal['feature']):
        id (str):
        observed_at (datetime.datetime | Unset):
        properties (EdpFeatureItemProperties | Unset): Item properties, validated against the stream's payloadSchema
        geometry (EdpGeometry | None | Unset):
        event_time (datetime.datetime | None | Unset):
        valid_until (datetime.datetime | None | Unset):
        origin (EdpOrigin | None | Unset):
    """

    stream: str
    type_: Literal["feature"]
    id: str
    observed_at: datetime.datetime | Unset = UNSET
    properties: EdpFeatureItemProperties | Unset = UNSET
    geometry: EdpGeometry | None | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    valid_until: datetime.datetime | None | Unset = UNSET
    origin: EdpOrigin | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_geometry import EdpGeometry
        from ..models.edp_origin import EdpOrigin

        stream = self.stream

        type_ = self.type_

        id = self.id

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, EdpGeometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        event_time: None | str | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, datetime.datetime):
            event_time = self.event_time.isoformat()
        else:
            event_time = self.event_time

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

        origin: dict[str, Any] | None | Unset
        if isinstance(self.origin, Unset):
            origin = UNSET
        elif isinstance(self.origin, EdpOrigin):
            origin = self.origin.to_dict()
        else:
            origin = self.origin

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "stream": stream,
                "type": type_,
                "id": id,
            }
        )
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if properties is not UNSET:
            field_dict["properties"] = properties
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if origin is not UNSET:
            field_dict["origin"] = origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_feature_item_properties import EdpFeatureItemProperties
        from ..models.edp_geometry import EdpGeometry
        from ..models.edp_origin import EdpOrigin

        d = dict(src_dict)
        stream = d.pop("stream")

        type_ = cast(Literal["feature"], d.pop("type"))
        if type_ != "feature":
            raise ValueError(f"type must match const 'feature', got '{type_}'")

        id = d.pop("id")

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _properties = d.pop("properties", UNSET)
        properties: EdpFeatureItemProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = EdpFeatureItemProperties.from_dict(_properties)

        def _parse_geometry(data: object) -> EdpGeometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = EdpGeometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpGeometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_event_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                event_time_type_0 = datetime.datetime.fromisoformat(data)

                return event_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        event_time = _parse_event_time(d.pop("eventTime", UNSET))

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

        def _parse_origin(data: object) -> EdpOrigin | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                origin_type_0 = EdpOrigin.from_dict(data)

                return origin_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpOrigin | None | Unset, data)

        origin = _parse_origin(d.pop("origin", UNSET))

        edp_feature_item = cls(
            stream=stream,
            type_=type_,
            id=id,
            observed_at=observed_at,
            properties=properties,
            geometry=geometry,
            event_time=event_time,
            valid_until=valid_until,
            origin=origin,
        )

        edp_feature_item.additional_properties = d
        return edp_feature_item

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
