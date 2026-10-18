from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.product_temporal_retention_ladder_item import (
        ProductTemporalRetentionLadderItem,
    )


T = TypeVar("T", bound="ProductTemporalRetention")


@_attrs_define
class ProductTemporalRetention:
    """Frame retention ladder

    Attributes:
        ladder (list[ProductTemporalRetentionLadderItem] | Unset): Thinning rungs, ascending
        max_age (None | str | Unset): Frames older than this are deleted (observation products); null keeps them until
            the disk quota evicts them. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 48h.
        runs (int | None | Unset): Complete forecast runs kept (forecasts: 2)
        summary (str | Unset): One-line description Example: every frame for 6 h, then 10-min frames to 48 h.
    """

    ladder: list[ProductTemporalRetentionLadderItem] | Unset = UNSET
    max_age: None | str | Unset = UNSET
    runs: int | None | Unset = UNSET
    summary: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ladder: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.ladder, Unset):
            ladder = []
            for ladder_item_data in self.ladder:
                ladder_item = ladder_item_data.to_dict()
                ladder.append(ladder_item)

        max_age: None | str | Unset
        if isinstance(self.max_age, Unset):
            max_age = UNSET
        else:
            max_age = self.max_age

        runs: int | None | Unset
        if isinstance(self.runs, Unset):
            runs = UNSET
        else:
            runs = self.runs

        summary = self.summary

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if ladder is not UNSET:
            field_dict["ladder"] = ladder
        if max_age is not UNSET:
            field_dict["maxAge"] = max_age
        if runs is not UNSET:
            field_dict["runs"] = runs
        if summary is not UNSET:
            field_dict["summary"] = summary

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.product_temporal_retention_ladder_item import (
            ProductTemporalRetentionLadderItem,
        )

        d = dict(src_dict)
        _ladder = d.pop("ladder", UNSET)
        ladder: list[ProductTemporalRetentionLadderItem] | Unset = UNSET
        if _ladder is not UNSET:
            ladder = []
            for ladder_item_data in _ladder:
                ladder_item = ProductTemporalRetentionLadderItem.from_dict(
                    ladder_item_data
                )

                ladder.append(ladder_item)

        def _parse_max_age(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        max_age = _parse_max_age(d.pop("maxAge", UNSET))

        def _parse_runs(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        runs = _parse_runs(d.pop("runs", UNSET))

        summary = d.pop("summary", UNSET)

        product_temporal_retention = cls(
            ladder=ladder,
            max_age=max_age,
            runs=runs,
            summary=summary,
        )

        product_temporal_retention.additional_properties = d
        return product_temporal_retention

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
