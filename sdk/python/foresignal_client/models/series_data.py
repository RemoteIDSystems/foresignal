from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_data_frequency import SeriesDataFrequency
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_point import SeriesPoint
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="SeriesData")


@_attrs_define
class SeriesData:
    """Points of one series read through the `SeriesReader`, with explicit gaps

    Attributes:
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
        from_ (datetime.datetime):
        to (datetime.datetime):
        frequency (SeriesDataFrequency):
        values_withheld (bool): True when raw values are withheld (`LOCAL_ONLY` source and no local egress)
        points (list[SeriesPoint]):
        gaps (list[TimeRange]): Periods with no point or no coverage
        as_of (datetime.datetime | None | Unset): Knowledge cutoff applied: only points with `knownAt` ≤ asOf
        unit (None | str | Unset):
        family (None | str | Unset): 117 series family or 119 signal family
    """

    ref: str
    from_: datetime.datetime
    to: datetime.datetime
    frequency: SeriesDataFrequency
    values_withheld: bool
    points: list[SeriesPoint]
    gaps: list[TimeRange]
    as_of: datetime.datetime | None | Unset = UNSET
    unit: None | str | Unset = UNSET
    family: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        frequency = self.frequency.value

        values_withheld = self.values_withheld

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        gaps = []
        for gaps_item_data in self.gaps:
            gaps_item = gaps_item_data.to_dict()
            gaps.append(gaps_item)

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

        family: None | str | Unset
        if isinstance(self.family, Unset):
            family = UNSET
        else:
            family = self.family

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "from": from_,
                "to": to,
                "frequency": frequency,
                "valuesWithheld": values_withheld,
                "points": points,
                "gaps": gaps,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if unit is not UNSET:
            field_dict["unit"] = unit
        if family is not UNSET:
            field_dict["family"] = family

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_point import SeriesPoint
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        ref = d.pop("ref")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        frequency = SeriesDataFrequency(d.pop("frequency"))

        values_withheld = d.pop("valuesWithheld")

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = SeriesPoint.from_dict(points_item_data)

            points.append(points_item)

        gaps = []
        _gaps = d.pop("gaps")
        for gaps_item_data in _gaps:
            gaps_item = TimeRange.from_dict(gaps_item_data)

            gaps.append(gaps_item)

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

        def _parse_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        family = _parse_family(d.pop("family", UNSET))

        series_data = cls(
            ref=ref,
            from_=from_,
            to=to,
            frequency=frequency,
            values_withheld=values_withheld,
            points=points,
            gaps=gaps,
            as_of=as_of,
            unit=unit,
            family=family,
        )

        series_data.additional_properties = d
        return series_data

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
