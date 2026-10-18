from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RasterFrameForecastRun")


@_attrs_define
class RasterFrameForecastRun:
    """Forecast variant: one version per completed run

    Attributes:
        product_id (str):
        run (str): Run reference time (ISO-8601)
        steps (list[int]): Forecast steps (hours)
        valid_times (list[datetime.datetime]): Valid time per step
        tiles_template (str): Tile template with `{timeKey}` (or `{frameKey}`) plus `{z}/{x}/{y}`, run pinned
        partial (bool | Unset): True only on the optional `{productId}:partial` record (run in progress)
    """

    product_id: str
    run: str
    steps: list[int]
    valid_times: list[datetime.datetime]
    tiles_template: str
    partial: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        run = self.run

        steps = self.steps

        valid_times = []
        for valid_times_item_data in self.valid_times:
            valid_times_item = valid_times_item_data.isoformat()
            valid_times.append(valid_times_item)

        tiles_template = self.tiles_template

        partial = self.partial

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "run": run,
                "steps": steps,
                "validTimes": valid_times,
                "tilesTemplate": tiles_template,
            }
        )
        if partial is not UNSET:
            field_dict["partial"] = partial

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        product_id = d.pop("productId")

        run = d.pop("run")

        steps = cast(list[int], d.pop("steps"))

        valid_times = []
        _valid_times = d.pop("validTimes")
        for valid_times_item_data in _valid_times:
            valid_times_item = datetime.datetime.fromisoformat(valid_times_item_data)

            valid_times.append(valid_times_item)

        tiles_template = d.pop("tilesTemplate")

        partial = d.pop("partial", UNSET)

        raster_frame_forecast_run = cls(
            product_id=product_id,
            run=run,
            steps=steps,
            valid_times=valid_times,
            tiles_template=tiles_template,
            partial=partial,
        )

        raster_frame_forecast_run.additional_properties = d
        return raster_frame_forecast_run

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
