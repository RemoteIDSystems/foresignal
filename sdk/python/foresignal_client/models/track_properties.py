from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.track_properties_values import TrackPropertiesValues


T = TypeVar("T", bound="TrackProperties")


@_attrs_define
class TrackProperties:
    """Track properties

    Attributes:
        source_id (str):
        external_id (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        times (list[list[int]]): Epoch-ms per point, parallel to each segment
        points_returned (int):
        segments (int):
        watermark_seq (int):
        kind (str | Unset):
        values (TrackPropertiesValues | Unset): Requested numeric fields, parallel arrays per segment
        live (bool | Unset): The last point is the current record_live position, newer than the last version
        points_scanned (int | Unset):
        tolerance_meters (float | Unset):
        approximate (bool | Unset):
        downsampled (bool | Unset):
        truncated (bool | Unset): More than 200,000 rows existed
    """

    source_id: str
    external_id: str
    from_: datetime.datetime
    to: datetime.datetime
    times: list[list[int]]
    points_returned: int
    segments: int
    watermark_seq: int
    kind: str | Unset = UNSET
    values: TrackPropertiesValues | Unset = UNSET
    live: bool | Unset = UNSET
    points_scanned: int | Unset = UNSET
    tolerance_meters: float | Unset = UNSET
    approximate: bool | Unset = UNSET
    downsampled: bool | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        external_id = self.external_id

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        times = []
        for times_item_data in self.times:
            times_item = times_item_data

            times.append(times_item)

        points_returned = self.points_returned

        segments = self.segments

        watermark_seq = self.watermark_seq

        kind = self.kind

        values: dict[str, Any] | Unset = UNSET
        if not isinstance(self.values, Unset):
            values = self.values.to_dict()

        live = self.live

        points_scanned = self.points_scanned

        tolerance_meters = self.tolerance_meters

        approximate = self.approximate

        downsampled = self.downsampled

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "externalId": external_id,
                "from": from_,
                "to": to,
                "times": times,
                "pointsReturned": points_returned,
                "segments": segments,
                "watermarkSeq": watermark_seq,
            }
        )
        if kind is not UNSET:
            field_dict["kind"] = kind
        if values is not UNSET:
            field_dict["values"] = values
        if live is not UNSET:
            field_dict["live"] = live
        if points_scanned is not UNSET:
            field_dict["pointsScanned"] = points_scanned
        if tolerance_meters is not UNSET:
            field_dict["toleranceMeters"] = tolerance_meters
        if approximate is not UNSET:
            field_dict["approximate"] = approximate
        if downsampled is not UNSET:
            field_dict["downsampled"] = downsampled
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.track_properties_values import TrackPropertiesValues

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        times = []
        _times = d.pop("times")
        for times_item_data in _times:
            times_item = cast(list[int], times_item_data)

            times.append(times_item)

        points_returned = d.pop("pointsReturned")

        segments = d.pop("segments")

        watermark_seq = d.pop("watermarkSeq")

        kind = d.pop("kind", UNSET)

        _values = d.pop("values", UNSET)
        values: TrackPropertiesValues | Unset
        if isinstance(_values, Unset):
            values = UNSET
        else:
            values = TrackPropertiesValues.from_dict(_values)

        live = d.pop("live", UNSET)

        points_scanned = d.pop("pointsScanned", UNSET)

        tolerance_meters = d.pop("toleranceMeters", UNSET)

        approximate = d.pop("approximate", UNSET)

        downsampled = d.pop("downsampled", UNSET)

        truncated = d.pop("truncated", UNSET)

        track_properties = cls(
            source_id=source_id,
            external_id=external_id,
            from_=from_,
            to=to,
            times=times,
            points_returned=points_returned,
            segments=segments,
            watermark_seq=watermark_seq,
            kind=kind,
            values=values,
            live=live,
            points_scanned=points_scanned,
            tolerance_meters=tolerance_meters,
            approximate=approximate,
            downsampled=downsampled,
            truncated=truncated,
        )

        track_properties.additional_properties = d
        return track_properties

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
