from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.archive_files_item_tier import ArchiveFilesItemTier
from ..types import UNSET, Unset

T = TypeVar("T", bound="ArchiveFilesItem")


@_attrs_define
class ArchiveFilesItem:
    """
    Attributes:
        name (str | Unset):
        source_id (str | Unset):
        tier (ArchiveFilesItemTier | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        from_time (datetime.datetime | None | Unset):
        to_time (datetime.datetime | None | Unset):
        rows (int | Unset):
        bytes_ (int | Unset):
        sha256 (str | Unset):
        location (None | str | Unset):
    """

    name: str | Unset = UNSET
    source_id: str | Unset = UNSET
    tier: ArchiveFilesItemTier | Unset = UNSET
    from_seq: int | Unset = UNSET
    to_seq: int | Unset = UNSET
    from_time: datetime.datetime | None | Unset = UNSET
    to_time: datetime.datetime | None | Unset = UNSET
    rows: int | Unset = UNSET
    bytes_: int | Unset = UNSET
    sha256: str | Unset = UNSET
    location: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        source_id = self.source_id

        tier: str | Unset = UNSET
        if not isinstance(self.tier, Unset):
            tier = self.tier.value

        from_seq = self.from_seq

        to_seq = self.to_seq

        from_time: None | str | Unset
        if isinstance(self.from_time, Unset):
            from_time = UNSET
        elif isinstance(self.from_time, datetime.datetime):
            from_time = self.from_time.isoformat()
        else:
            from_time = self.from_time

        to_time: None | str | Unset
        if isinstance(self.to_time, Unset):
            to_time = UNSET
        elif isinstance(self.to_time, datetime.datetime):
            to_time = self.to_time.isoformat()
        else:
            to_time = self.to_time

        rows = self.rows

        bytes_ = self.bytes_

        sha256 = self.sha256

        location: None | str | Unset
        if isinstance(self.location, Unset):
            location = UNSET
        else:
            location = self.location

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if tier is not UNSET:
            field_dict["tier"] = tier
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if from_time is not UNSET:
            field_dict["fromTime"] = from_time
        if to_time is not UNSET:
            field_dict["toTime"] = to_time
        if rows is not UNSET:
            field_dict["rows"] = rows
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if sha256 is not UNSET:
            field_dict["sha256"] = sha256
        if location is not UNSET:
            field_dict["location"] = location

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name", UNSET)

        source_id = d.pop("sourceId", UNSET)

        _tier = d.pop("tier", UNSET)
        tier: ArchiveFilesItemTier | Unset
        if isinstance(_tier, Unset):
            tier = UNSET
        else:
            tier = ArchiveFilesItemTier(_tier)

        from_seq = d.pop("fromSeq", UNSET)

        to_seq = d.pop("toSeq", UNSET)

        def _parse_from_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_time_type_0 = datetime.datetime.fromisoformat(data)

                return from_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_time = _parse_from_time(d.pop("fromTime", UNSET))

        def _parse_to_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_time_type_0 = datetime.datetime.fromisoformat(data)

                return to_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to_time = _parse_to_time(d.pop("toTime", UNSET))

        rows = d.pop("rows", UNSET)

        bytes_ = d.pop("bytes", UNSET)

        sha256 = d.pop("sha256", UNSET)

        def _parse_location(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        location = _parse_location(d.pop("location", UNSET))

        archive_files_item = cls(
            name=name,
            source_id=source_id,
            tier=tier,
            from_seq=from_seq,
            to_seq=to_seq,
            from_time=from_time,
            to_time=to_time,
            rows=rows,
            bytes_=bytes_,
            sha256=sha256,
            location=location,
        )

        archive_files_item.additional_properties = d
        return archive_files_item

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
