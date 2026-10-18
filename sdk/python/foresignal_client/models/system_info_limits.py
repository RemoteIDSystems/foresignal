from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoLimits")


@_attrs_define
class SystemInfoLimits:
    """
    Attributes:
        page_max (int | Unset):
        history_page_max (int | Unset):
        sse_client_buffer (int | Unset):
        live_max_entities (int | Unset):
        ingest_max_bytes (int | Unset):
        ingest_max_records (int | Unset):
        tile_max_features (int | Unset):
    """

    page_max: int | Unset = UNSET
    history_page_max: int | Unset = UNSET
    sse_client_buffer: int | Unset = UNSET
    live_max_entities: int | Unset = UNSET
    ingest_max_bytes: int | Unset = UNSET
    ingest_max_records: int | Unset = UNSET
    tile_max_features: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        page_max = self.page_max

        history_page_max = self.history_page_max

        sse_client_buffer = self.sse_client_buffer

        live_max_entities = self.live_max_entities

        ingest_max_bytes = self.ingest_max_bytes

        ingest_max_records = self.ingest_max_records

        tile_max_features = self.tile_max_features

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if page_max is not UNSET:
            field_dict["pageMax"] = page_max
        if history_page_max is not UNSET:
            field_dict["historyPageMax"] = history_page_max
        if sse_client_buffer is not UNSET:
            field_dict["sseClientBuffer"] = sse_client_buffer
        if live_max_entities is not UNSET:
            field_dict["liveMaxEntities"] = live_max_entities
        if ingest_max_bytes is not UNSET:
            field_dict["ingestMaxBytes"] = ingest_max_bytes
        if ingest_max_records is not UNSET:
            field_dict["ingestMaxRecords"] = ingest_max_records
        if tile_max_features is not UNSET:
            field_dict["tileMaxFeatures"] = tile_max_features

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        page_max = d.pop("pageMax", UNSET)

        history_page_max = d.pop("historyPageMax", UNSET)

        sse_client_buffer = d.pop("sseClientBuffer", UNSET)

        live_max_entities = d.pop("liveMaxEntities", UNSET)

        ingest_max_bytes = d.pop("ingestMaxBytes", UNSET)

        ingest_max_records = d.pop("ingestMaxRecords", UNSET)

        tile_max_features = d.pop("tileMaxFeatures", UNSET)

        system_info_limits = cls(
            page_max=page_max,
            history_page_max=history_page_max,
            sse_client_buffer=sse_client_buffer,
            live_max_entities=live_max_entities,
            ingest_max_bytes=ingest_max_bytes,
            ingest_max_records=ingest_max_records,
            tile_max_features=tile_max_features,
        )

        system_info_limits.additional_properties = d
        return system_info_limits

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
