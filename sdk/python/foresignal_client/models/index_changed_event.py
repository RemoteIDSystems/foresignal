from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.index_changed_event_weights_origin import IndexChangedEventWeightsOrigin
from ..types import UNSET, Unset

T = TypeVar("T", bound="IndexChangedEvent")


@_attrs_define
class IndexChangedEvent:
    """SSE payload for `index.changed` (≥ 5 points; topic `analytics`)

    Attributes:
        index_id (str):
        region (str):
        day (datetime.date):
        value (float):
        previous_value (float | None | Unset):
        delta24h (float | None | Unset):
        delta7d (float | None | Unset):
        weights_origin (IndexChangedEventWeightsOrigin | Unset):
    """

    index_id: str
    region: str
    day: datetime.date
    value: float
    previous_value: float | None | Unset = UNSET
    delta24h: float | None | Unset = UNSET
    delta7d: float | None | Unset = UNSET
    weights_origin: IndexChangedEventWeightsOrigin | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index_id = self.index_id

        region = self.region

        day = self.day.isoformat()

        value = self.value

        previous_value: float | None | Unset
        if isinstance(self.previous_value, Unset):
            previous_value = UNSET
        else:
            previous_value = self.previous_value

        delta24h: float | None | Unset
        if isinstance(self.delta24h, Unset):
            delta24h = UNSET
        else:
            delta24h = self.delta24h

        delta7d: float | None | Unset
        if isinstance(self.delta7d, Unset):
            delta7d = UNSET
        else:
            delta7d = self.delta7d

        weights_origin: str | Unset = UNSET
        if not isinstance(self.weights_origin, Unset):
            weights_origin = self.weights_origin.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "indexId": index_id,
                "region": region,
                "day": day,
                "value": value,
            }
        )
        if previous_value is not UNSET:
            field_dict["previousValue"] = previous_value
        if delta24h is not UNSET:
            field_dict["delta24h"] = delta24h
        if delta7d is not UNSET:
            field_dict["delta7d"] = delta7d
        if weights_origin is not UNSET:
            field_dict["weightsOrigin"] = weights_origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        index_id = d.pop("indexId")

        region = d.pop("region")

        day = datetime.date.fromisoformat(d.pop("day"))

        value = d.pop("value")

        def _parse_previous_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        previous_value = _parse_previous_value(d.pop("previousValue", UNSET))

        def _parse_delta24h(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        delta24h = _parse_delta24h(d.pop("delta24h", UNSET))

        def _parse_delta7d(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        delta7d = _parse_delta7d(d.pop("delta7d", UNSET))

        _weights_origin = d.pop("weightsOrigin", UNSET)
        weights_origin: IndexChangedEventWeightsOrigin | Unset
        if isinstance(_weights_origin, Unset):
            weights_origin = UNSET
        else:
            weights_origin = IndexChangedEventWeightsOrigin(_weights_origin)

        index_changed_event = cls(
            index_id=index_id,
            region=region,
            day=day,
            value=value,
            previous_value=previous_value,
            delta24h=delta24h,
            delta7d=delta7d,
            weights_origin=weights_origin,
        )

        index_changed_event.additional_properties = d
        return index_changed_event

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
