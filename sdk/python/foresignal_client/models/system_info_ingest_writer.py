from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_info_ingest_writer_state import SystemInfoIngestWriterState
from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoIngestWriter")


@_attrs_define
class SystemInfoIngestWriter:
    """
    Attributes:
        state (SystemInfoIngestWriterState | Unset):
        warnings (list[str] | Unset):
        last_seq (int | Unset):
        last_ingested_at (datetime.datetime | None | Unset):
        queue_depth (int | Unset):
    """

    state: SystemInfoIngestWriterState | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    last_seq: int | Unset = UNSET
    last_ingested_at: datetime.datetime | None | Unset = UNSET
    queue_depth: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        state: str | Unset = UNSET
        if not isinstance(self.state, Unset):
            state = self.state.value

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        last_seq = self.last_seq

        last_ingested_at: None | str | Unset
        if isinstance(self.last_ingested_at, Unset):
            last_ingested_at = UNSET
        elif isinstance(self.last_ingested_at, datetime.datetime):
            last_ingested_at = self.last_ingested_at.isoformat()
        else:
            last_ingested_at = self.last_ingested_at

        queue_depth = self.queue_depth

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if state is not UNSET:
            field_dict["state"] = state
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if last_seq is not UNSET:
            field_dict["lastSeq"] = last_seq
        if last_ingested_at is not UNSET:
            field_dict["lastIngestedAt"] = last_ingested_at
        if queue_depth is not UNSET:
            field_dict["queueDepth"] = queue_depth

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _state = d.pop("state", UNSET)
        state: SystemInfoIngestWriterState | Unset
        if isinstance(_state, Unset):
            state = UNSET
        else:
            state = SystemInfoIngestWriterState(_state)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        last_seq = d.pop("lastSeq", UNSET)

        def _parse_last_ingested_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_ingested_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_ingested_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_ingested_at = _parse_last_ingested_at(d.pop("lastIngestedAt", UNSET))

        queue_depth = d.pop("queueDepth", UNSET)

        system_info_ingest_writer = cls(
            state=state,
            warnings=warnings,
            last_seq=last_seq,
            last_ingested_at=last_ingested_at,
            queue_depth=queue_depth,
        )

        system_info_ingest_writer.additional_properties = d
        return system_info_ingest_writer

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
