from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.series_sample import SeriesSample


T = TypeVar("T", bound="Series")


@_attrs_define
class Series:
    """Observed past plus forecast future at a point (≤ 500 steps)

    Attributes:
        product_id (str):
        lon (float):
        lat (float):
        items (list[SeriesSample]):
        server_time (datetime.datetime):
        place_id (None | str | Unset):
        timezone (str | Unset):
        units (None | str | Unset):
        label (None | str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (None | str | Unset): Forecast run used for future steps
        attribution (Attribution | Unset): Structured attribution clients must render for a product or derived output
            (Weather-Imagery-Design §6.10)
    """

    product_id: str
    lon: float
    lat: float
    items: list[SeriesSample]
    server_time: datetime.datetime
    place_id: None | str | Unset = UNSET
    timezone: str | Unset = UNSET
    units: None | str | Unset = UNSET
    label: None | str | Unset = UNSET
    from_: datetime.datetime | Unset = UNSET
    to: datetime.datetime | Unset = UNSET
    run: None | str | Unset = UNSET
    attribution: Attribution | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        lon = self.lon

        lat = self.lat

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        server_time = self.server_time.isoformat()

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        timezone = self.timezone

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        attribution: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = self.attribution.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "lon": lon,
                "lat": lat,
                "items": items,
                "serverTime": server_time,
            }
        )
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if units is not UNSET:
            field_dict["units"] = units
        if label is not UNSET:
            field_dict["label"] = label
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if run is not UNSET:
            field_dict["run"] = run
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.series_sample import SeriesSample

        d = dict(src_dict)
        product_id = d.pop("productId")

        lon = d.pop("lon")

        lat = d.pop("lat")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = SeriesSample.from_dict(items_item_data)

            items.append(items_item)

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        timezone = d.pop("timezone", UNSET)

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

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

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        _attribution = d.pop("attribution", UNSET)
        attribution: Attribution | Unset
        if isinstance(_attribution, Unset):
            attribution = UNSET
        else:
            attribution = Attribution.from_dict(_attribution)

        series = cls(
            product_id=product_id,
            lon=lon,
            lat=lat,
            items=items,
            server_time=server_time,
            place_id=place_id,
            timezone=timezone,
            units=units,
            label=label,
            from_=from_,
            to=to,
            run=run,
            attribution=attribution,
        )

        series.additional_properties = d
        return series

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
