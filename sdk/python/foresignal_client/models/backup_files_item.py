from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="BackupFilesItem")


@_attrs_define
class BackupFilesItem:
    """
    Attributes:
        name (str):
        location (str): Absolute path or `s3://` URL
        bytes_ (int):
        sha256 (str):
    """

    name: str
    location: str
    bytes_: int
    sha256: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        location = self.location

        bytes_ = self.bytes_

        sha256 = self.sha256

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "location": location,
                "bytes": bytes_,
                "sha256": sha256,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        location = d.pop("location")

        bytes_ = d.pop("bytes")

        sha256 = d.pop("sha256")

        backup_files_item = cls(
            name=name,
            location=location,
            bytes_=bytes_,
            sha256=sha256,
        )

        backup_files_item.additional_properties = d
        return backup_files_item

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
