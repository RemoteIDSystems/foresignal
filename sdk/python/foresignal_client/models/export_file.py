from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExportFile")


@_attrs_define
class ExportFile:
    """A downloadable export file

    Attributes:
        name (str):
        url (str):
        bytes_ (int):
        sha256 (str):
        expires_at (datetime.datetime):
        media_type (str | Unset):  Example: application/vnd.pmtiles.
    """

    name: str
    url: str
    bytes_: int
    sha256: str
    expires_at: datetime.datetime
    media_type: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        url = self.url

        bytes_ = self.bytes_

        sha256 = self.sha256

        expires_at = self.expires_at.isoformat()

        media_type = self.media_type

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "url": url,
                "bytes": bytes_,
                "sha256": sha256,
                "expiresAt": expires_at,
            }
        )
        if media_type is not UNSET:
            field_dict["mediaType"] = media_type

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        url = d.pop("url")

        bytes_ = d.pop("bytes")

        sha256 = d.pop("sha256")

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        media_type = d.pop("mediaType", UNSET)

        export_file = cls(
            name=name,
            url=url,
            bytes_=bytes_,
            sha256=sha256,
            expires_at=expires_at,
            media_type=media_type,
        )

        export_file.additional_properties = d
        return export_file

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
