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
    from ..models.edp_observation_item_properties import EdpObservationItemProperties
    from ..models.edp_origin import EdpOrigin


T = TypeVar("T", bound="EdpObservationItem")


@_attrs_define
class EdpObservationItem:
    """A value of a series

    Attributes:
        stream (str):
        type_ (Literal['observation']):
        id (str):
        series_key (str):
        t (str): Period or instant: YYYY, YYYY-Qn, YYYY-MM, YYYY-Www (ISO week), YYYY-MM-DD, or an RFC 3339 timestamp
            with offset. Periods are UTC intervals [start, end); one series uses one granularity.
        value (float | None):
        observed_at (datetime.datetime | Unset):
        properties (EdpObservationItemProperties | Unset): Item properties, validated against the stream's payloadSchema
        known_at (datetime.datetime | None | Unset): Provider-claimed availability time; stored as given. Foresignal
            uses max(knownAt, its own receipt time) for as-of and look-ahead rules.
        unit (None | str | Unset):
        origin (EdpOrigin | None | Unset):
    """

    stream: str
    type_: Literal["observation"]
    id: str
    series_key: str
    t: str
    value: float | None
    observed_at: datetime.datetime | Unset = UNSET
    properties: EdpObservationItemProperties | Unset = UNSET
    known_at: datetime.datetime | None | Unset = UNSET
    unit: None | str | Unset = UNSET
    origin: EdpOrigin | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_origin import EdpOrigin

        stream = self.stream

        type_ = self.type_

        id = self.id

        series_key = self.series_key

        t = self.t

        value: float | None
        value = self.value

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        known_at: None | str | Unset
        if isinstance(self.known_at, Unset):
            known_at = UNSET
        elif isinstance(self.known_at, datetime.datetime):
            known_at = self.known_at.isoformat()
        else:
            known_at = self.known_at

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

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
                "seriesKey": series_key,
                "t": t,
                "value": value,
            }
        )
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if properties is not UNSET:
            field_dict["properties"] = properties
        if known_at is not UNSET:
            field_dict["knownAt"] = known_at
        if unit is not UNSET:
            field_dict["unit"] = unit
        if origin is not UNSET:
            field_dict["origin"] = origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_observation_item_properties import (
            EdpObservationItemProperties,
        )
        from ..models.edp_origin import EdpOrigin

        d = dict(src_dict)
        stream = d.pop("stream")

        type_ = cast(Literal["observation"], d.pop("type"))
        if type_ != "observation":
            raise ValueError(f"type must match const 'observation', got '{type_}'")

        id = d.pop("id")

        series_key = d.pop("seriesKey")

        t = d.pop("t")

        def _parse_value(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        value = _parse_value(d.pop("value"))

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _properties = d.pop("properties", UNSET)
        properties: EdpObservationItemProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = EdpObservationItemProperties.from_dict(_properties)

        def _parse_known_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                known_at_type_0 = datetime.datetime.fromisoformat(data)

                return known_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        known_at = _parse_known_at(d.pop("knownAt", UNSET))

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

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

        edp_observation_item = cls(
            stream=stream,
            type_=type_,
            id=id,
            series_key=series_key,
            t=t,
            value=value,
            observed_at=observed_at,
            properties=properties,
            known_at=known_at,
            unit=unit,
            origin=origin,
        )

        edp_observation_item.additional_properties = d
        return edp_observation_item

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
