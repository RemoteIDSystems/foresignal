from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_archive_type_0_state import ChronicleArchiveType0State
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleArchiveType0")


@_attrs_define
class ChronicleArchiveType0:
    """Cold-archive state (111)

    Attributes:
        state (ChronicleArchiveType0State):
        export_id (None | str | Unset):
        archived_at (datetime.datetime | None | Unset):
        bytes_ (int | None | Unset):
        error (None | str | Unset):
    """

    state: ChronicleArchiveType0State
    export_id: None | str | Unset = UNSET
    archived_at: datetime.datetime | None | Unset = UNSET
    bytes_: int | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        state = self.state.value

        export_id: None | str | Unset
        if isinstance(self.export_id, Unset):
            export_id = UNSET
        else:
            export_id = self.export_id

        archived_at: None | str | Unset
        if isinstance(self.archived_at, Unset):
            archived_at = UNSET
        elif isinstance(self.archived_at, datetime.datetime):
            archived_at = self.archived_at.isoformat()
        else:
            archived_at = self.archived_at

        bytes_: int | None | Unset
        if isinstance(self.bytes_, Unset):
            bytes_ = UNSET
        else:
            bytes_ = self.bytes_

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "state": state,
            }
        )
        if export_id is not UNSET:
            field_dict["exportId"] = export_id
        if archived_at is not UNSET:
            field_dict["archivedAt"] = archived_at
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        state = ChronicleArchiveType0State(d.pop("state"))

        def _parse_export_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        export_id = _parse_export_id(d.pop("exportId", UNSET))

        def _parse_archived_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                archived_at_type_0 = datetime.datetime.fromisoformat(data)

                return archived_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        archived_at = _parse_archived_at(d.pop("archivedAt", UNSET))

        def _parse_bytes_(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_ = _parse_bytes_(d.pop("bytes", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        chronicle_archive_type_0 = cls(
            state=state,
            export_id=export_id,
            archived_at=archived_at,
            bytes_=bytes_,
            error=error,
        )

        chronicle_archive_type_0.additional_properties = d
        return chronicle_archive_type_0

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
