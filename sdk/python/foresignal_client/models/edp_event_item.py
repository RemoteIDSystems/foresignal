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
    from ..models.edp_event_item_properties import EdpEventItemProperties
    from ..models.edp_geometry import EdpGeometry
    from ..models.edp_origin import EdpOrigin


T = TypeVar("T", bound="EdpEventItem")


@_attrs_define
class EdpEventItem:
    """A discrete occurrence

    Attributes:
        stream (str):
        type_ (Literal['event']):
        id (str):
        event_time (datetime.datetime):
        observed_at (datetime.datetime | Unset):
        properties (EdpEventItemProperties | Unset): Item properties, validated against the stream's payloadSchema
        geometry (EdpGeometry | None | Unset):
        upstream_event_id (None | str | Unset):
        origin (EdpOrigin | None | Unset):
    """

    stream: str
    type_: Literal["event"]
    id: str
    event_time: datetime.datetime
    observed_at: datetime.datetime | Unset = UNSET
    properties: EdpEventItemProperties | Unset = UNSET
    geometry: EdpGeometry | None | Unset = UNSET
    upstream_event_id: None | str | Unset = UNSET
    origin: EdpOrigin | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_geometry import EdpGeometry
        from ..models.edp_origin import EdpOrigin

        stream = self.stream

        type_ = self.type_

        id = self.id

        event_time = self.event_time.isoformat()

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

        upstream_event_id: None | str | Unset
        if isinstance(self.upstream_event_id, Unset):
            upstream_event_id = UNSET
        else:
            upstream_event_id = self.upstream_event_id

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
                "eventTime": event_time,
            }
        )
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if properties is not UNSET:
            field_dict["properties"] = properties
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if upstream_event_id is not UNSET:
            field_dict["upstreamEventId"] = upstream_event_id
        if origin is not UNSET:
            field_dict["origin"] = origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_event_item_properties import EdpEventItemProperties
        from ..models.edp_geometry import EdpGeometry
        from ..models.edp_origin import EdpOrigin

        d = dict(src_dict)
        stream = d.pop("stream")

        type_ = cast(Literal["event"], d.pop("type"))
        if type_ != "event":
            raise ValueError(f"type must match const 'event', got '{type_}'")

        id = d.pop("id")

        event_time = datetime.datetime.fromisoformat(d.pop("eventTime"))

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _properties = d.pop("properties", UNSET)
        properties: EdpEventItemProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = EdpEventItemProperties.from_dict(_properties)

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

        def _parse_upstream_event_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        upstream_event_id = _parse_upstream_event_id(d.pop("upstreamEventId", UNSET))

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

        edp_event_item = cls(
            stream=stream,
            type_=type_,
            id=id,
            event_time=event_time,
            observed_at=observed_at,
            properties=properties,
            geometry=geometry,
            upstream_event_id=upstream_event_id,
            origin=origin,
        )

        edp_event_item.additional_properties = d
        return edp_event_item

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
