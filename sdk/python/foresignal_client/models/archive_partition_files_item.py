from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ArchivePartitionFilesItem")


@_attrs_define
class ArchivePartitionFilesItem:
    """An archived GeoParquet part

    Attributes:
        name (str): part-{fromSeq}-{toSeq}.parquet
        from_seq (int):
        to_seq (int):
        rows (int):
        bytes_ (int):
        sha256 (str):
        local (bool | Unset):
        uploaded_at (datetime.datetime | None | Unset):
        s_3_key (None | str | Unset):
    """

    name: str
    from_seq: int
    to_seq: int
    rows: int
    bytes_: int
    sha256: str
    local: bool | Unset = UNSET
    uploaded_at: datetime.datetime | None | Unset = UNSET
    s_3_key: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        from_seq = self.from_seq

        to_seq = self.to_seq

        rows = self.rows

        bytes_ = self.bytes_

        sha256 = self.sha256

        local = self.local

        uploaded_at: None | str | Unset
        if isinstance(self.uploaded_at, Unset):
            uploaded_at = UNSET
        elif isinstance(self.uploaded_at, datetime.datetime):
            uploaded_at = self.uploaded_at.isoformat()
        else:
            uploaded_at = self.uploaded_at

        s_3_key: None | str | Unset
        if isinstance(self.s_3_key, Unset):
            s_3_key = UNSET
        else:
            s_3_key = self.s_3_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "fromSeq": from_seq,
                "toSeq": to_seq,
                "rows": rows,
                "bytes": bytes_,
                "sha256": sha256,
            }
        )
        if local is not UNSET:
            field_dict["local"] = local
        if uploaded_at is not UNSET:
            field_dict["uploadedAt"] = uploaded_at
        if s_3_key is not UNSET:
            field_dict["s3Key"] = s_3_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        from_seq = d.pop("fromSeq")

        to_seq = d.pop("toSeq")

        rows = d.pop("rows")

        bytes_ = d.pop("bytes")

        sha256 = d.pop("sha256")

        local = d.pop("local", UNSET)

        def _parse_uploaded_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                uploaded_at_type_0 = datetime.datetime.fromisoformat(data)

                return uploaded_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        uploaded_at = _parse_uploaded_at(d.pop("uploadedAt", UNSET))

        def _parse_s_3_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        s_3_key = _parse_s_3_key(d.pop("s3Key", UNSET))

        archive_partition_files_item = cls(
            name=name,
            from_seq=from_seq,
            to_seq=to_seq,
            rows=rows,
            bytes_=bytes_,
            sha256=sha256,
            local=local,
            uploaded_at=uploaded_at,
            s_3_key=s_3_key,
        )

        archive_partition_files_item.additional_properties = d
        return archive_partition_files_item

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
