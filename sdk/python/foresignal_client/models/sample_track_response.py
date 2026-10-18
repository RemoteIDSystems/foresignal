from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.sample_track_response_points_item import SampleTrackResponsePointsItem


T = TypeVar("T", bound="SampleTrackResponse")


@_attrs_define
class SampleTrackResponse:
    """Per-point samples along a track; each point and value carries a status

    Attributes:
        products (list[str]):
        points (list[SampleTrackResponsePointsItem]):
        record (None | str | Unset):
        attribution (list[Attribution] | Unset):
        server_time (datetime.datetime | Unset):
    """

    products: list[str]
    points: list[SampleTrackResponsePointsItem]
    record: None | str | Unset = UNSET
    attribution: list[Attribution] | Unset = UNSET
    server_time: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        products = self.products

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        record: None | str | Unset
        if isinstance(self.record, Unset):
            record = UNSET
        else:
            record = self.record

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        server_time: str | Unset = UNSET
        if not isinstance(self.server_time, Unset):
            server_time = self.server_time.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "products": products,
                "points": points,
            }
        )
        if record is not UNSET:
            field_dict["record"] = record
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if server_time is not UNSET:
            field_dict["serverTime"] = server_time

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.sample_track_response_points_item import (
            SampleTrackResponsePointsItem,
        )

        d = dict(src_dict)
        products = cast(list[str], d.pop("products"))

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = SampleTrackResponsePointsItem.from_dict(points_item_data)

            points.append(points_item)

        def _parse_record(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record = _parse_record(d.pop("record", UNSET))

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        _server_time = d.pop("serverTime", UNSET)
        server_time: datetime.datetime | Unset
        if isinstance(_server_time, Unset):
            server_time = UNSET
        else:
            server_time = datetime.datetime.fromisoformat(_server_time)

        sample_track_response = cls(
            products=products,
            points=points,
            record=record,
            attribution=attribution,
            server_time=server_time,
        )

        sample_track_response.additional_properties = d
        return sample_track_response

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
