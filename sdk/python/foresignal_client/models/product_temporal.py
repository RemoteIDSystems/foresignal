from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.product_temporal_type import ProductTemporalType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.product_temporal_retention import ProductTemporalRetention
    from ..models.product_temporal_runs_item import ProductTemporalRunsItem


T = TypeVar("T", bound="ProductTemporal")


@_attrs_define
class ProductTemporal:
    """Temporal facts of a product: frame type, cadence, gap tolerance, available range, retention and runs

    Attributes:
        type_ (ProductTemporalType): observation: one frame per scan; forecast: runs of steps; pass: one frame per
            satellite pass
        cadence (str | Unset): Nominal interval between frames (forecasts: between steps). Absent for an external
            catalog entry without a fixed time dimension Example: 2m.
        max_gap (str | Unset): Largest gap at which a frame still counts as current (`now` alias, sampling `stale`).
            Absent for an external catalog entry without a fixed time dimension Example: 6m.
        earliest (datetime.datetime | None | Unset): Valid time of the oldest retained frame
        latest (datetime.datetime | None | Unset): Valid time of the newest complete frame
        retention (ProductTemporalRetention | Unset): Frame retention ladder
        runs (list[ProductTemporalRunsItem] | Unset): Forecast runs, newest first (forecast products only)
    """

    type_: ProductTemporalType
    cadence: str | Unset = UNSET
    max_gap: str | Unset = UNSET
    earliest: datetime.datetime | None | Unset = UNSET
    latest: datetime.datetime | None | Unset = UNSET
    retention: ProductTemporalRetention | Unset = UNSET
    runs: list[ProductTemporalRunsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        cadence = self.cadence

        max_gap = self.max_gap

        earliest: None | str | Unset
        if isinstance(self.earliest, Unset):
            earliest = UNSET
        elif isinstance(self.earliest, datetime.datetime):
            earliest = self.earliest.isoformat()
        else:
            earliest = self.earliest

        latest: None | str | Unset
        if isinstance(self.latest, Unset):
            latest = UNSET
        elif isinstance(self.latest, datetime.datetime):
            latest = self.latest.isoformat()
        else:
            latest = self.latest

        retention: dict[str, Any] | Unset = UNSET
        if not isinstance(self.retention, Unset):
            retention = self.retention.to_dict()

        runs: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.runs, Unset):
            runs = []
            for runs_item_data in self.runs:
                runs_item = runs_item_data.to_dict()
                runs.append(runs_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if cadence is not UNSET:
            field_dict["cadence"] = cadence
        if max_gap is not UNSET:
            field_dict["maxGap"] = max_gap
        if earliest is not UNSET:
            field_dict["earliest"] = earliest
        if latest is not UNSET:
            field_dict["latest"] = latest
        if retention is not UNSET:
            field_dict["retention"] = retention
        if runs is not UNSET:
            field_dict["runs"] = runs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.product_temporal_retention import ProductTemporalRetention
        from ..models.product_temporal_runs_item import ProductTemporalRunsItem

        d = dict(src_dict)
        type_ = ProductTemporalType(d.pop("type"))

        cadence = d.pop("cadence", UNSET)

        max_gap = d.pop("maxGap", UNSET)

        def _parse_earliest(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                earliest_type_0 = datetime.datetime.fromisoformat(data)

                return earliest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        earliest = _parse_earliest(d.pop("earliest", UNSET))

        def _parse_latest(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                latest_type_0 = datetime.datetime.fromisoformat(data)

                return latest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        latest = _parse_latest(d.pop("latest", UNSET))

        _retention = d.pop("retention", UNSET)
        retention: ProductTemporalRetention | Unset
        if isinstance(_retention, Unset):
            retention = UNSET
        else:
            retention = ProductTemporalRetention.from_dict(_retention)

        _runs = d.pop("runs", UNSET)
        runs: list[ProductTemporalRunsItem] | Unset = UNSET
        if _runs is not UNSET:
            runs = []
            for runs_item_data in _runs:
                runs_item = ProductTemporalRunsItem.from_dict(runs_item_data)

                runs.append(runs_item)

        product_temporal = cls(
            type_=type_,
            cadence=cadence,
            max_gap=max_gap,
            earliest=earliest,
            latest=latest,
            retention=retention,
            runs=runs,
        )

        product_temporal.additional_properties = d
        return product_temporal

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
