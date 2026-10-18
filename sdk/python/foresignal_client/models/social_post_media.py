from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_post_media_type import SocialPostMediaType
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialPostMedia")


@_attrs_define
class SocialPostMedia:
    """A media link on a post (links only; 136 fingerprints images)

    Attributes:
        type_ (SocialPostMediaType):
        url (str):
        thumb_url (None | str | Unset):
        alt_text (None | str | Unset):
        fingerprint_id (None | str | Unset): 136 media fingerprint, once computed
    """

    type_: SocialPostMediaType
    url: str
    thumb_url: None | str | Unset = UNSET
    alt_text: None | str | Unset = UNSET
    fingerprint_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        url = self.url

        thumb_url: None | str | Unset
        if isinstance(self.thumb_url, Unset):
            thumb_url = UNSET
        else:
            thumb_url = self.thumb_url

        alt_text: None | str | Unset
        if isinstance(self.alt_text, Unset):
            alt_text = UNSET
        else:
            alt_text = self.alt_text

        fingerprint_id: None | str | Unset
        if isinstance(self.fingerprint_id, Unset):
            fingerprint_id = UNSET
        else:
            fingerprint_id = self.fingerprint_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "url": url,
            }
        )
        if thumb_url is not UNSET:
            field_dict["thumbUrl"] = thumb_url
        if alt_text is not UNSET:
            field_dict["altText"] = alt_text
        if fingerprint_id is not UNSET:
            field_dict["fingerprintId"] = fingerprint_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = SocialPostMediaType(d.pop("type"))

        url = d.pop("url")

        def _parse_thumb_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        thumb_url = _parse_thumb_url(d.pop("thumbUrl", UNSET))

        def _parse_alt_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        alt_text = _parse_alt_text(d.pop("altText", UNSET))

        def _parse_fingerprint_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        fingerprint_id = _parse_fingerprint_id(d.pop("fingerprintId", UNSET))

        social_post_media = cls(
            type_=type_,
            url=url,
            thumb_url=thumb_url,
            alt_text=alt_text,
            fingerprint_id=fingerprint_id,
        )

        social_post_media.additional_properties = d
        return social_post_media

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
