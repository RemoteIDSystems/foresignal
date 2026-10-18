from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.system_info_history_tiers_item import SystemInfoHistoryTiersItem


T = TypeVar("T", bound="SystemInfoHistory")


@_attrs_define
class SystemInfoHistory:
    """Per-tier history storage from pg_partition_tree('record_version')

    Attributes:
        tiers (list[SystemInfoHistoryTiersItem] | Unset):
        versions_per_minute (float | Unset):
        projected_bytes (int | Unset):
    """

    tiers: list[SystemInfoHistoryTiersItem] | Unset = UNSET
    versions_per_minute: float | Unset = UNSET
    projected_bytes: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tiers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.tiers, Unset):
            tiers = []
            for tiers_item_data in self.tiers:
                tiers_item = tiers_item_data.to_dict()
                tiers.append(tiers_item)

        versions_per_minute = self.versions_per_minute

        projected_bytes = self.projected_bytes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if tiers is not UNSET:
            field_dict["tiers"] = tiers
        if versions_per_minute is not UNSET:
            field_dict["versionsPerMinute"] = versions_per_minute
        if projected_bytes is not UNSET:
            field_dict["projectedBytes"] = projected_bytes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.system_info_history_tiers_item import SystemInfoHistoryTiersItem

        d = dict(src_dict)
        _tiers = d.pop("tiers", UNSET)
        tiers: list[SystemInfoHistoryTiersItem] | Unset = UNSET
        if _tiers is not UNSET:
            tiers = []
            for tiers_item_data in _tiers:
                tiers_item = SystemInfoHistoryTiersItem.from_dict(tiers_item_data)

                tiers.append(tiers_item)

        versions_per_minute = d.pop("versionsPerMinute", UNSET)

        projected_bytes = d.pop("projectedBytes", UNSET)

        system_info_history = cls(
            tiers=tiers,
            versions_per_minute=versions_per_minute,
            projected_bytes=projected_bytes,
        )

        system_info_history.additional_properties = d
        return system_info_history

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
