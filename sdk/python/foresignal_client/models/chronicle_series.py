from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_point import SeriesPoint


T = TypeVar("T", bound="ChronicleSeries")


@_attrs_define
class ChronicleSeries:
    """Captured series points and rollups from chronicle storage

    Attributes:
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series;
            `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count
            with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left,
            center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135:
            hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement,
            every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the
            platform-language denominator; complete and paged collection only) Example: series:cftc-
            cot/067651.disagg.mm_net.
        points (list[SeriesPoint]):
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        backfilled (bool | Unset):
    """

    chronicle_id: str
    ref: str
    points: list[SeriesPoint]
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    backfilled: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_id = self.chronicle_id

        ref = self.ref

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

        backfilled = self.backfilled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chronicleId": chronicle_id,
                "ref": ref,
                "points": points,
            }
        )
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if backfilled is not UNSET:
            field_dict["backfilled"] = backfilled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_point import SeriesPoint

        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        ref = d.pop("ref")

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = SeriesPoint.from_dict(points_item_data)

            points.append(points_item)

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        backfilled = d.pop("backfilled", UNSET)

        chronicle_series = cls(
            chronicle_id=chronicle_id,
            ref=ref,
            points=points,
            from_=from_,
            to=to,
            backfilled=backfilled,
        )

        chronicle_series.additional_properties = d
        return chronicle_series

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
