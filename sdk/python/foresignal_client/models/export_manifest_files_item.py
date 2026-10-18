from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExportManifestFilesItem")


@_attrs_define
class ExportManifestFilesItem:
    """A data or sidecar file

    Attributes:
        name (str):
        bytes_ (int):
        sha256 (str):
        media_type (str | Unset):
        rows (int | None | Unset):
    """

    name: str
    bytes_: int
    sha256: str
    media_type: str | Unset = UNSET
    rows: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        bytes_ = self.bytes_

        sha256 = self.sha256

        media_type = self.media_type

        rows: int | None | Unset
        if isinstance(self.rows, Unset):
            rows = UNSET
        else:
            rows = self.rows

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "bytes": bytes_,
                "sha256": sha256,
            }
        )
        if media_type is not UNSET:
            field_dict["mediaType"] = media_type
        if rows is not UNSET:
            field_dict["rows"] = rows

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        bytes_ = d.pop("bytes")

        sha256 = d.pop("sha256")

        media_type = d.pop("mediaType", UNSET)

        def _parse_rows(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        rows = _parse_rows(d.pop("rows", UNSET))

        export_manifest_files_item = cls(
            name=name,
            bytes_=bytes_,
            sha256=sha256,
            media_type=media_type,
            rows=rows,
        )

        export_manifest_files_item.additional_properties = d
        return export_manifest_files_item

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
