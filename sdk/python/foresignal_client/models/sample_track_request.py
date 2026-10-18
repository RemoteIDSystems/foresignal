from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.sample_track_request_units import SampleTrackRequestUnits
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sample_track_request_points_item import SampleTrackRequestPointsItem


T = TypeVar("T", bound="SampleTrackRequest")


@_attrs_define
class SampleTrackRequest:
    """Sample products along a track: a record (points from the 037 /track simplification over from/to) or ≤ 500 explicit
    points

        Attributes:
            products (list[str]):
            record (str | Unset): `{sourceId}/{externalId}` Example: adsblol-mil/ae1234.
            from_ (datetime.datetime | Unset):
            to (datetime.datetime | Unset):
            points (list[SampleTrackRequestPointsItem] | Unset):
            max_gap (str | Unset): Nearest-frame tolerance; default per-product maxGap. ISO-8601 duration or shorthand (30m,
                6h, 7d).
            run (str | Unset): Forecast run for future times: latest | previous | <runIso>
            units (SampleTrackRequestUnits | Unset):  Default: SampleTrackRequestUnits.METRIC.
    """

    products: list[str]
    record: str | Unset = UNSET
    from_: datetime.datetime | Unset = UNSET
    to: datetime.datetime | Unset = UNSET
    points: list[SampleTrackRequestPointsItem] | Unset = UNSET
    max_gap: str | Unset = UNSET
    run: str | Unset = UNSET
    units: SampleTrackRequestUnits | Unset = SampleTrackRequestUnits.METRIC

    def to_dict(self) -> dict[str, Any]:
        products = self.products

        record = self.record

        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        points: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.points, Unset):
            points = []
            for points_item_data in self.points:
                points_item = points_item_data.to_dict()
                points.append(points_item)

        max_gap = self.max_gap

        run = self.run

        units: str | Unset = UNSET
        if not isinstance(self.units, Unset):
            units = self.units.value

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "products": products,
            }
        )
        if record is not UNSET:
            field_dict["record"] = record
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if points is not UNSET:
            field_dict["points"] = points
        if max_gap is not UNSET:
            field_dict["maxGap"] = max_gap
        if run is not UNSET:
            field_dict["run"] = run
        if units is not UNSET:
            field_dict["units"] = units

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sample_track_request_points_item import (
            SampleTrackRequestPointsItem,
        )

        d = dict(src_dict)
        products = cast(list[str], d.pop("products"))

        record = d.pop("record", UNSET)

        _from_ = d.pop("from", UNSET)
        from_: datetime.datetime | Unset
        if isinstance(_from_, Unset):
            from_ = UNSET
        else:
            from_ = datetime.datetime.fromisoformat(_from_)

        _to = d.pop("to", UNSET)
        to: datetime.datetime | Unset
        if isinstance(_to, Unset):
            to = UNSET
        else:
            to = datetime.datetime.fromisoformat(_to)

        _points = d.pop("points", UNSET)
        points: list[SampleTrackRequestPointsItem] | Unset = UNSET
        if _points is not UNSET:
            points = []
            for points_item_data in _points:
                points_item = SampleTrackRequestPointsItem.from_dict(points_item_data)

                points.append(points_item)

        max_gap = d.pop("maxGap", UNSET)

        run = d.pop("run", UNSET)

        _units = d.pop("units", UNSET)
        units: SampleTrackRequestUnits | Unset
        if isinstance(_units, Unset):
            units = UNSET
        else:
            units = SampleTrackRequestUnits(_units)

        sample_track_request = cls(
            products=products,
            record=record,
            from_=from_,
            to=to,
            points=points,
            max_gap=max_gap,
            run=run,
            units=units,
        )

        return sample_track_request
