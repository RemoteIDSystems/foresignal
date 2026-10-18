from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Attachment")


@_attrs_define
class Attachment:
    """Image attachment of a notification; stored as a media reference so retries resend the bytes

    Attributes:
        filename (str):  Example: map.png.
        content_type (str):  Example: image/png.
        bytes_ (int): Size in bytes (the API never carries the content)
        media_id (None | str | Unset):
        url (None | str | Unset): `{publicBaseUrl}/v1/media/{id}`
    """

    filename: str
    content_type: str
    bytes_: int
    media_id: None | str | Unset = UNSET
    url: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        filename = self.filename

        content_type = self.content_type

        bytes_ = self.bytes_

        media_id: None | str | Unset
        if isinstance(self.media_id, Unset):
            media_id = UNSET
        else:
            media_id = self.media_id

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "filename": filename,
                "contentType": content_type,
                "bytes": bytes_,
            }
        )
        if media_id is not UNSET:
            field_dict["mediaId"] = media_id
        if url is not UNSET:
            field_dict["url"] = url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        filename = d.pop("filename")

        content_type = d.pop("contentType")

        bytes_ = d.pop("bytes")

        def _parse_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        media_id = _parse_media_id(d.pop("mediaId", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        attachment = cls(
            filename=filename,
            content_type=content_type,
            bytes_=bytes_,
            media_id=media_id,
            url=url,
        )

        attachment.additional_properties = d
        return attachment

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
