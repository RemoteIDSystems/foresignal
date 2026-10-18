from __future__ import annotations

from collections.abc import Mapping
from io import BytesIO
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from .. import types
from ..types import UNSET, File, Unset

T = TypeVar("T", bound="MediaMatchUpload")


@_attrs_define
class MediaMatchUpload:
    """Multipart image upload for matching (through 043 ImageGuard: size, type and SSRF limits)

    Attributes:
        file (File):
        max_distance (int | Unset):  Default: 31.
        limit (int | Unset):  Default: 20.
    """

    file: File
    max_distance: int | Unset = 31
    limit: int | Unset = 20
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        file = self.file.to_tuple()

        max_distance = self.max_distance

        limit = self.limit

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "file": file,
            }
        )
        if max_distance is not UNSET:
            field_dict["maxDistance"] = max_distance
        if limit is not UNSET:
            field_dict["limit"] = limit

        return field_dict

    def to_multipart(self) -> types.RequestFiles:
        files: types.RequestFiles = []

        files.append(("file", self.file.to_tuple()))

        if not isinstance(self.max_distance, Unset):
            files.append(
                ("maxDistance", (None, str(self.max_distance).encode(), "text/plain"))
            )

        if not isinstance(self.limit, Unset):
            files.append(("limit", (None, str(self.limit).encode(), "text/plain")))

        for prop_name, prop in self.additional_properties.items():
            files.append((prop_name, (None, str(prop).encode(), "text/plain")))

        return files

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        file = File(payload=BytesIO(d.pop("file")))

        max_distance = d.pop("maxDistance", UNSET)

        limit = d.pop("limit", UNSET)

        media_match_upload = cls(
            file=file,
            max_distance=max_distance,
            limit=limit,
        )

        media_match_upload.additional_properties = d
        return media_match_upload

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
