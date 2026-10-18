from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.media_content_type import MediaContentType
from ..models.media_role import MediaRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.media_links import MediaLinks


T = TypeVar("T", bound="Media")


@_attrs_define
class Media:
    """A content-addressed media row (SatDump composite, thumbnail, static map, briefing audio, or a generated document
    such as the CZML linked by MCP `get_track`, prompt 141)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            sha256 (str):
            content_type (MediaContentType):
            width (int): Pixels; 0 for audio
            height (int): Pixels; 0 for audio
            bytes_ (int):
            role (MediaRole): `document` (141): a generated non-image document such as CZML, stored with an expiry; `audio`
                (141): synthesized speech of a briefing run, expiring with the briefing-run retention. Neither has thumbnails
            created_at (datetime.datetime):
            source_id (None | str | Unset):
            record_source_id (None | str | Unset):
            record_external_id (None | str | Unset):
            record_ref (None | str | Unset):
            licence (None | str | Unset):
            expires_at (datetime.datetime | None | Unset): Null: follows the owning record's retention
            variant_of (None | str | Unset):
            variant_width (int | None | Unset):
            links (MediaLinks | Unset): Expanded links (documents have no thumbnail links)
    """

    id: str
    sha256: str
    content_type: MediaContentType
    width: int
    height: int
    bytes_: int
    role: MediaRole
    created_at: datetime.datetime
    source_id: None | str | Unset = UNSET
    record_source_id: None | str | Unset = UNSET
    record_external_id: None | str | Unset = UNSET
    record_ref: None | str | Unset = UNSET
    licence: None | str | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    variant_of: None | str | Unset = UNSET
    variant_width: int | None | Unset = UNSET
    links: MediaLinks | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        sha256 = self.sha256

        content_type = self.content_type.value

        width = self.width

        height = self.height

        bytes_ = self.bytes_

        role = self.role.value

        created_at = self.created_at.isoformat()

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        record_source_id: None | str | Unset
        if isinstance(self.record_source_id, Unset):
            record_source_id = UNSET
        else:
            record_source_id = self.record_source_id

        record_external_id: None | str | Unset
        if isinstance(self.record_external_id, Unset):
            record_external_id = UNSET
        else:
            record_external_id = self.record_external_id

        record_ref: None | str | Unset
        if isinstance(self.record_ref, Unset):
            record_ref = UNSET
        else:
            record_ref = self.record_ref

        licence: None | str | Unset
        if isinstance(self.licence, Unset):
            licence = UNSET
        else:
            licence = self.licence

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        variant_of: None | str | Unset
        if isinstance(self.variant_of, Unset):
            variant_of = UNSET
        else:
            variant_of = self.variant_of

        variant_width: int | None | Unset
        if isinstance(self.variant_width, Unset):
            variant_width = UNSET
        else:
            variant_width = self.variant_width

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "sha256": sha256,
                "contentType": content_type,
                "width": width,
                "height": height,
                "bytes": bytes_,
                "role": role,
                "createdAt": created_at,
            }
        )
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if record_source_id is not UNSET:
            field_dict["recordSourceId"] = record_source_id
        if record_external_id is not UNSET:
            field_dict["recordExternalId"] = record_external_id
        if record_ref is not UNSET:
            field_dict["recordRef"] = record_ref
        if licence is not UNSET:
            field_dict["licence"] = licence
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if variant_of is not UNSET:
            field_dict["variantOf"] = variant_of
        if variant_width is not UNSET:
            field_dict["variantWidth"] = variant_width
        if links is not UNSET:
            field_dict["links"] = links

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.media_links import MediaLinks

        d = dict(src_dict)
        id = d.pop("id")

        sha256 = d.pop("sha256")

        content_type = MediaContentType(d.pop("contentType"))

        width = d.pop("width")

        height = d.pop("height")

        bytes_ = d.pop("bytes")

        role = MediaRole(d.pop("role"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        def _parse_record_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_source_id = _parse_record_source_id(d.pop("recordSourceId", UNSET))

        def _parse_record_external_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_external_id = _parse_record_external_id(d.pop("recordExternalId", UNSET))

        def _parse_record_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ref = _parse_record_ref(d.pop("recordRef", UNSET))

        def _parse_licence(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence = _parse_licence(d.pop("licence", UNSET))

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        def _parse_variant_of(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        variant_of = _parse_variant_of(d.pop("variantOf", UNSET))

        def _parse_variant_width(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        variant_width = _parse_variant_width(d.pop("variantWidth", UNSET))

        _links = d.pop("links", UNSET)
        links: MediaLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = MediaLinks.from_dict(_links)

        media = cls(
            id=id,
            sha256=sha256,
            content_type=content_type,
            width=width,
            height=height,
            bytes_=bytes_,
            role=role,
            created_at=created_at,
            source_id=source_id,
            record_source_id=record_source_id,
            record_external_id=record_external_id,
            record_ref=record_ref,
            licence=licence,
            expires_at=expires_at,
            variant_of=variant_of,
            variant_width=variant_width,
            links=links,
        )

        media.additional_properties = d
        return media

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
