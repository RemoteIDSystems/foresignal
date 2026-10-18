from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.history_tier import HistoryTier
from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoHistoryTiersItem")


@_attrs_define
class SystemInfoHistoryTiersItem:
    """
    Attributes:
        tier (HistoryTier): History tier: STANDARD (per-source windows, chunked deletes) or TRACK (one `trackWindow`,
            partition drops)
        partitions (int):
        rows (int): Σ reltuples
        bytes_ (int): Σ pg_total_relation_size
        headroom_seq (int | Unset):
        window_days (float | Unset):
        downsampled_leaves (int | Unset): TRACK leaves thinned by history downsampling (044); 0 for STANDARD
        downsampled_rows (int | Unset): Rows the downsampled leaves kept (044)
    """

    tier: HistoryTier
    partitions: int
    rows: int
    bytes_: int
    headroom_seq: int | Unset = UNSET
    window_days: float | Unset = UNSET
    downsampled_leaves: int | Unset = UNSET
    downsampled_rows: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tier = self.tier.value

        partitions = self.partitions

        rows = self.rows

        bytes_ = self.bytes_

        headroom_seq = self.headroom_seq

        window_days = self.window_days

        downsampled_leaves = self.downsampled_leaves

        downsampled_rows = self.downsampled_rows

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tier": tier,
                "partitions": partitions,
                "rows": rows,
                "bytes": bytes_,
            }
        )
        if headroom_seq is not UNSET:
            field_dict["headroomSeq"] = headroom_seq
        if window_days is not UNSET:
            field_dict["windowDays"] = window_days
        if downsampled_leaves is not UNSET:
            field_dict["downsampledLeaves"] = downsampled_leaves
        if downsampled_rows is not UNSET:
            field_dict["downsampledRows"] = downsampled_rows

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        tier = HistoryTier(d.pop("tier"))

        partitions = d.pop("partitions")

        rows = d.pop("rows")

        bytes_ = d.pop("bytes")

        headroom_seq = d.pop("headroomSeq", UNSET)

        window_days = d.pop("windowDays", UNSET)

        downsampled_leaves = d.pop("downsampledLeaves", UNSET)

        downsampled_rows = d.pop("downsampledRows", UNSET)

        system_info_history_tiers_item = cls(
            tier=tier,
            partitions=partitions,
            rows=rows,
            bytes_=bytes_,
            headroom_seq=headroom_seq,
            window_days=window_days,
            downsampled_leaves=downsampled_leaves,
            downsampled_rows=downsampled_rows,
        )

        system_info_history_tiers_item.additional_properties = d
        return system_info_history_tiers_item

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
