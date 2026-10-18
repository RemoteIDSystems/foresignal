from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoCatchUp")


@_attrs_define
class SystemInfoCatchUp:
    """
    Attributes:
        trigger_lag_seq (int | Unset):
        ingest_queue_depth (int | Unset):
        catching_up (bool | Unset): lag > 10,000 seq or ingest queue > 80% for > 1 min
    """

    trigger_lag_seq: int | Unset = UNSET
    ingest_queue_depth: int | Unset = UNSET
    catching_up: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        trigger_lag_seq = self.trigger_lag_seq

        ingest_queue_depth = self.ingest_queue_depth

        catching_up = self.catching_up

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if trigger_lag_seq is not UNSET:
            field_dict["triggerLagSeq"] = trigger_lag_seq
        if ingest_queue_depth is not UNSET:
            field_dict["ingestQueueDepth"] = ingest_queue_depth
        if catching_up is not UNSET:
            field_dict["catchingUp"] = catching_up

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        trigger_lag_seq = d.pop("triggerLagSeq", UNSET)

        ingest_queue_depth = d.pop("ingestQueueDepth", UNSET)

        catching_up = d.pop("catchingUp", UNSET)

        system_info_catch_up = cls(
            trigger_lag_seq=trigger_lag_seq,
            ingest_queue_depth=ingest_queue_depth,
            catching_up=catching_up,
        )

        system_info_catch_up.additional_properties = d
        return system_info_catch_up

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
