from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.product_timeline_mode import ProductTimelineMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.product_timeline_runs import ProductTimelineRuns
    from ..models.timeline_tick import TimelineTick


T = TypeVar("T", bound="ProductTimeline")


@_attrs_define
class ProductTimeline:
    """Cross-product timeline (`/v1/products/timeline`, 051)

    Attributes:
        mode (ProductTimelineMode):
        products (list[str]):
        ticks (list[TimelineTick]):
        server_time (datetime.datetime):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        runs (ProductTimelineRuns | Unset): Run used per forecast product (one run, never mixed)
        now_index (int | None | Unset): Index of the tick nearest now
    """

    mode: ProductTimelineMode
    products: list[str]
    ticks: list[TimelineTick]
    server_time: datetime.datetime
    from_: datetime.datetime | Unset = UNSET
    to: datetime.datetime | Unset = UNSET
    runs: ProductTimelineRuns | Unset = UNSET
    now_index: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        products = self.products

        ticks = []
        for ticks_item_data in self.ticks:
            ticks_item = ticks_item_data.to_dict()
            ticks.append(ticks_item)

        server_time = self.server_time.isoformat()

        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        runs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.runs, Unset):
            runs = self.runs.to_dict()

        now_index: int | None | Unset
        if isinstance(self.now_index, Unset):
            now_index = UNSET
        else:
            now_index = self.now_index

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mode": mode,
                "products": products,
                "ticks": ticks,
                "serverTime": server_time,
            }
        )
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if runs is not UNSET:
            field_dict["runs"] = runs
        if now_index is not UNSET:
            field_dict["nowIndex"] = now_index

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.product_timeline_runs import ProductTimelineRuns
        from ..models.timeline_tick import TimelineTick

        d = dict(src_dict)
        mode = ProductTimelineMode(d.pop("mode"))

        products = cast(list[str], d.pop("products"))

        ticks = []
        _ticks = d.pop("ticks")
        for ticks_item_data in _ticks:
            ticks_item = TimelineTick.from_dict(ticks_item_data)

            ticks.append(ticks_item)

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

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

        _runs = d.pop("runs", UNSET)
        runs: ProductTimelineRuns | Unset
        if isinstance(_runs, Unset):
            runs = UNSET
        else:
            runs = ProductTimelineRuns.from_dict(_runs)

        def _parse_now_index(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        now_index = _parse_now_index(d.pop("nowIndex", UNSET))

        product_timeline = cls(
            mode=mode,
            products=products,
            ticks=ticks,
            server_time=server_time,
            from_=from_,
            to=to,
            runs=runs,
            now_index=now_index,
        )

        product_timeline.additional_properties = d
        return product_timeline

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
