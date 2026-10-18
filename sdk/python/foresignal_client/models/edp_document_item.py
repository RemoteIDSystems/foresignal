from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_document_item_document_type import EdpDocumentItemDocumentType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_document_item_author_type_0 import EdpDocumentItemAuthorType0
    from ..models.edp_document_item_engagement_type_0 import (
        EdpDocumentItemEngagementType0,
    )
    from ..models.edp_document_item_media_item import EdpDocumentItemMediaItem
    from ..models.edp_document_item_properties import EdpDocumentItemProperties
    from ..models.edp_geometry import EdpGeometry
    from ..models.edp_origin import EdpOrigin


T = TypeVar("T", bound="EdpDocumentItem")


@_attrs_define
class EdpDocumentItem:
    """A text item: post, article, report, transcript or note

    Attributes:
        stream (str):
        type_ (Literal['document']):
        id (str):
        document_type (EdpDocumentItemDocumentType):
        text (str):
        observed_at (datetime.datetime | Unset):
        properties (EdpDocumentItemProperties | Unset): Item properties, validated against the stream's payloadSchema
        title (None | str | Unset):
        url (None | str | Unset):
        author (EdpDocumentItemAuthorType0 | None | Unset):
        language (None | str | Unset):
        published_at (datetime.datetime | None | Unset):
        about (list[str] | Unset): Foresignal object references or URLs
        media (list[EdpDocumentItemMediaItem] | Unset):
        geometry (EdpGeometry | None | Unset):
        engagement (EdpDocumentItemEngagementType0 | None | Unset):
        origin (EdpOrigin | None | Unset):
    """

    stream: str
    type_: Literal["document"]
    id: str
    document_type: EdpDocumentItemDocumentType
    text: str
    observed_at: datetime.datetime | Unset = UNSET
    properties: EdpDocumentItemProperties | Unset = UNSET
    title: None | str | Unset = UNSET
    url: None | str | Unset = UNSET
    author: EdpDocumentItemAuthorType0 | None | Unset = UNSET
    language: None | str | Unset = UNSET
    published_at: datetime.datetime | None | Unset = UNSET
    about: list[str] | Unset = UNSET
    media: list[EdpDocumentItemMediaItem] | Unset = UNSET
    geometry: EdpGeometry | None | Unset = UNSET
    engagement: EdpDocumentItemEngagementType0 | None | Unset = UNSET
    origin: EdpOrigin | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_document_item_author_type_0 import EdpDocumentItemAuthorType0
        from ..models.edp_document_item_engagement_type_0 import (
            EdpDocumentItemEngagementType0,
        )
        from ..models.edp_geometry import EdpGeometry
        from ..models.edp_origin import EdpOrigin

        stream = self.stream

        type_ = self.type_

        id = self.id

        document_type = self.document_type.value

        text = self.text

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        author: dict[str, Any] | None | Unset
        if isinstance(self.author, Unset):
            author = UNSET
        elif isinstance(self.author, EdpDocumentItemAuthorType0):
            author = self.author.to_dict()
        else:
            author = self.author

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        published_at: None | str | Unset
        if isinstance(self.published_at, Unset):
            published_at = UNSET
        elif isinstance(self.published_at, datetime.datetime):
            published_at = self.published_at.isoformat()
        else:
            published_at = self.published_at

        about: list[str] | Unset = UNSET
        if not isinstance(self.about, Unset):
            about = self.about

        media: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.media, Unset):
            media = []
            for media_item_data in self.media:
                media_item = media_item_data.to_dict()
                media.append(media_item)

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, EdpGeometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        engagement: dict[str, Any] | None | Unset
        if isinstance(self.engagement, Unset):
            engagement = UNSET
        elif isinstance(self.engagement, EdpDocumentItemEngagementType0):
            engagement = self.engagement.to_dict()
        else:
            engagement = self.engagement

        origin: dict[str, Any] | None | Unset
        if isinstance(self.origin, Unset):
            origin = UNSET
        elif isinstance(self.origin, EdpOrigin):
            origin = self.origin.to_dict()
        else:
            origin = self.origin

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "stream": stream,
                "type": type_,
                "id": id,
                "documentType": document_type,
                "text": text,
            }
        )
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if properties is not UNSET:
            field_dict["properties"] = properties
        if title is not UNSET:
            field_dict["title"] = title
        if url is not UNSET:
            field_dict["url"] = url
        if author is not UNSET:
            field_dict["author"] = author
        if language is not UNSET:
            field_dict["language"] = language
        if published_at is not UNSET:
            field_dict["publishedAt"] = published_at
        if about is not UNSET:
            field_dict["about"] = about
        if media is not UNSET:
            field_dict["media"] = media
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if engagement is not UNSET:
            field_dict["engagement"] = engagement
        if origin is not UNSET:
            field_dict["origin"] = origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_document_item_author_type_0 import EdpDocumentItemAuthorType0
        from ..models.edp_document_item_engagement_type_0 import (
            EdpDocumentItemEngagementType0,
        )
        from ..models.edp_document_item_media_item import EdpDocumentItemMediaItem
        from ..models.edp_document_item_properties import EdpDocumentItemProperties
        from ..models.edp_geometry import EdpGeometry
        from ..models.edp_origin import EdpOrigin

        d = dict(src_dict)
        stream = d.pop("stream")

        type_ = cast(Literal["document"], d.pop("type"))
        if type_ != "document":
            raise ValueError(f"type must match const 'document', got '{type_}'")

        id = d.pop("id")

        document_type = EdpDocumentItemDocumentType(d.pop("documentType"))

        text = d.pop("text")

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _properties = d.pop("properties", UNSET)
        properties: EdpDocumentItemProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = EdpDocumentItemProperties.from_dict(_properties)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_author(data: object) -> EdpDocumentItemAuthorType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                author_type_0 = EdpDocumentItemAuthorType0.from_dict(data)

                return author_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpDocumentItemAuthorType0 | None | Unset, data)

        author = _parse_author(d.pop("author", UNSET))

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        def _parse_published_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                published_at_type_0 = datetime.datetime.fromisoformat(data)

                return published_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        published_at = _parse_published_at(d.pop("publishedAt", UNSET))

        about = cast(list[str], d.pop("about", UNSET))

        _media = d.pop("media", UNSET)
        media: list[EdpDocumentItemMediaItem] | Unset = UNSET
        if _media is not UNSET:
            media = []
            for media_item_data in _media:
                media_item = EdpDocumentItemMediaItem.from_dict(media_item_data)

                media.append(media_item)

        def _parse_geometry(data: object) -> EdpGeometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = EdpGeometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpGeometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_engagement(
            data: object,
        ) -> EdpDocumentItemEngagementType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                engagement_type_0 = EdpDocumentItemEngagementType0.from_dict(data)

                return engagement_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpDocumentItemEngagementType0 | None | Unset, data)

        engagement = _parse_engagement(d.pop("engagement", UNSET))

        def _parse_origin(data: object) -> EdpOrigin | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                origin_type_0 = EdpOrigin.from_dict(data)

                return origin_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpOrigin | None | Unset, data)

        origin = _parse_origin(d.pop("origin", UNSET))

        edp_document_item = cls(
            stream=stream,
            type_=type_,
            id=id,
            document_type=document_type,
            text=text,
            observed_at=observed_at,
            properties=properties,
            title=title,
            url=url,
            author=author,
            language=language,
            published_at=published_at,
            about=about,
            media=media,
            geometry=geometry,
            engagement=engagement,
            origin=origin,
        )

        edp_document_item.additional_properties = d
        return edp_document_item

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
