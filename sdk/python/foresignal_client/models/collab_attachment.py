from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.collab_attachment_image_meta_type_0 import (
        CollabAttachmentImageMetaType0,
    )
    from ..models.collab_attachment_links import CollabAttachmentLinks
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="CollabAttachment")


@_attrs_define
class CollabAttachment:
    """An uploaded document or image stored content-addressed (043) and served only as an attachment; data, never code

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        media_id (str): 043 content-addressed media id
        filename (str):
        content_type (str):  Example: application/pdf.
        size (int):
        sha256 (str):
        uploaded_by (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        tlp (Tlp): Traffic Light Protocol 2.0 label
        created_at (datetime.datetime):
        text (None | str | Unset): Extracted text (PDF via PDFBox, plain text, HTML stripped), ≤ 1 MB
        text_truncated (bool | Unset):
        image_meta (CollabAttachmentImageMetaType0 | None | Unset):
        owner (PrincipalSummary | Unset): Public view of a principal: handle, display name, kind and avatar only
        visibility (Visibility | Unset):
        links (CollabAttachmentLinks | Unset): Related links
    """

    id: str
    media_id: str
    filename: str
    content_type: str
    size: int
    sha256: str
    uploaded_by: PrincipalSummary
    tlp: Tlp
    created_at: datetime.datetime
    text: None | str | Unset = UNSET
    text_truncated: bool | Unset = UNSET
    image_meta: CollabAttachmentImageMetaType0 | None | Unset = UNSET
    owner: PrincipalSummary | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    links: CollabAttachmentLinks | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.collab_attachment_image_meta_type_0 import (
            CollabAttachmentImageMetaType0,
        )

        id = self.id

        media_id = self.media_id

        filename = self.filename

        content_type = self.content_type

        size = self.size

        sha256 = self.sha256

        uploaded_by = self.uploaded_by.to_dict()

        tlp = self.tlp.value

        created_at = self.created_at.isoformat()

        text: None | str | Unset
        if isinstance(self.text, Unset):
            text = UNSET
        else:
            text = self.text

        text_truncated = self.text_truncated

        image_meta: dict[str, Any] | None | Unset
        if isinstance(self.image_meta, Unset):
            image_meta = UNSET
        elif isinstance(self.image_meta, CollabAttachmentImageMetaType0):
            image_meta = self.image_meta.to_dict()
        else:
            image_meta = self.image_meta

        owner: dict[str, Any] | Unset = UNSET
        if not isinstance(self.owner, Unset):
            owner = self.owner.to_dict()

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "mediaId": media_id,
                "filename": filename,
                "contentType": content_type,
                "size": size,
                "sha256": sha256,
                "uploadedBy": uploaded_by,
                "tlp": tlp,
                "createdAt": created_at,
            }
        )
        if text is not UNSET:
            field_dict["text"] = text
        if text_truncated is not UNSET:
            field_dict["textTruncated"] = text_truncated
        if image_meta is not UNSET:
            field_dict["imageMeta"] = image_meta
        if owner is not UNSET:
            field_dict["owner"] = owner
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if links is not UNSET:
            field_dict["links"] = links

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.collab_attachment_image_meta_type_0 import (
            CollabAttachmentImageMetaType0,
        )
        from ..models.collab_attachment_links import CollabAttachmentLinks
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        id = d.pop("id")

        media_id = d.pop("mediaId")

        filename = d.pop("filename")

        content_type = d.pop("contentType")

        size = d.pop("size")

        sha256 = d.pop("sha256")

        uploaded_by = PrincipalSummary.from_dict(d.pop("uploadedBy"))

        tlp = Tlp(d.pop("tlp"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        text = _parse_text(d.pop("text", UNSET))

        text_truncated = d.pop("textTruncated", UNSET)

        def _parse_image_meta(
            data: object,
        ) -> CollabAttachmentImageMetaType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                image_meta_type_0 = CollabAttachmentImageMetaType0.from_dict(data)

                return image_meta_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CollabAttachmentImageMetaType0 | None | Unset, data)

        image_meta = _parse_image_meta(d.pop("imageMeta", UNSET))

        _owner = d.pop("owner", UNSET)
        owner: PrincipalSummary | Unset
        if isinstance(_owner, Unset):
            owner = UNSET
        else:
            owner = PrincipalSummary.from_dict(_owner)

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        _links = d.pop("links", UNSET)
        links: CollabAttachmentLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = CollabAttachmentLinks.from_dict(_links)

        collab_attachment = cls(
            id=id,
            media_id=media_id,
            filename=filename,
            content_type=content_type,
            size=size,
            sha256=sha256,
            uploaded_by=uploaded_by,
            tlp=tlp,
            created_at=created_at,
            text=text,
            text_truncated=text_truncated,
            image_meta=image_meta,
            owner=owner,
            visibility=visibility,
            links=links,
        )

        collab_attachment.additional_properties = d
        return collab_attachment

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
