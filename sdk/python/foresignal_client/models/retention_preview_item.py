from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RetentionPreviewItem")


@_attrs_define
class RetentionPreviewItem:
    """What a retention policy would prune from one subsystem

    Attributes:
        subsystem (str): `records` (STANDARD history, per kind), `track` (TRACK history leaves), `frames`, `media` or
            `chronicles` (stopped or archived chronicles whose history would be removed)
        affected_count (int):
        kind (None | str | Unset): The record kind of a `records` item; null for whole-source scopes and other
            subsystems
        cutoff (datetime.datetime | None | Unset): Oldest data kept (for a `records` kind, under its longest window);
            null when no single cutoff applies
        bytes_estimate (int | None | Unset): Estimated bytes: records from the average row size of record_version, TRACK
            leaves and frames from their sizes, media from its rows, chronicles from their bytesUsed
    """

    subsystem: str
    affected_count: int
    kind: None | str | Unset = UNSET
    cutoff: datetime.datetime | None | Unset = UNSET
    bytes_estimate: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subsystem = self.subsystem

        affected_count = self.affected_count

        kind: None | str | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        else:
            kind = self.kind

        cutoff: None | str | Unset
        if isinstance(self.cutoff, Unset):
            cutoff = UNSET
        elif isinstance(self.cutoff, datetime.datetime):
            cutoff = self.cutoff.isoformat()
        else:
            cutoff = self.cutoff

        bytes_estimate: int | None | Unset
        if isinstance(self.bytes_estimate, Unset):
            bytes_estimate = UNSET
        else:
            bytes_estimate = self.bytes_estimate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subsystem": subsystem,
                "affectedCount": affected_count,
            }
        )
        if kind is not UNSET:
            field_dict["kind"] = kind
        if cutoff is not UNSET:
            field_dict["cutoff"] = cutoff
        if bytes_estimate is not UNSET:
            field_dict["bytesEstimate"] = bytes_estimate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        subsystem = d.pop("subsystem")

        affected_count = d.pop("affectedCount")

        def _parse_kind(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind = _parse_kind(d.pop("kind", UNSET))

        def _parse_cutoff(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                cutoff_type_0 = datetime.datetime.fromisoformat(data)

                return cutoff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        cutoff = _parse_cutoff(d.pop("cutoff", UNSET))

        def _parse_bytes_estimate(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_estimate = _parse_bytes_estimate(d.pop("bytesEstimate", UNSET))

        retention_preview_item = cls(
            subsystem=subsystem,
            affected_count=affected_count,
            kind=kind,
            cutoff=cutoff,
            bytes_estimate=bytes_estimate,
        )

        retention_preview_item.additional_properties = d
        return retention_preview_item

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
