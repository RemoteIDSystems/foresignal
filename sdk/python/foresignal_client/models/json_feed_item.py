from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.json_feed_item_authors_item import JsonFeedItemAuthorsItem
    from ..models.json_feed_item_foresignal import JsonFeedItemForesignal


T = TypeVar("T", bound="JsonFeedItem")


@_attrs_define
class JsonFeedItem:
    """One JSON Feed item (alert or briefing run) with the `_foresignal` extension (058)

    Attributes:
        id (str):  Example: tag:foresignal,2026:01JA8Q3V7S/alert/4211.
        title (str):
        url (str | Unset): Client deep link, else `publicBaseUrl/v1/alerts/{id}`
        content_html (str | Unset): Markdown rendered with commonmark-java and sanitised by the allowlist policy
        content_text (str | Unset):
        date_published (datetime.datetime | Unset): `firedAt` (alerts) or run start
        date_modified (datetime.datetime | Unset): Last lifecycle change
        tags (list[str] | Unset): Severity and rule tags
        authors (list[JsonFeedItemAuthorsItem] | Unset):
        field_foresignal (JsonFeedItemForesignal | Unset): Foresignal extension (alert items)
    """

    id: str
    title: str
    url: str | Unset = UNSET
    content_html: str | Unset = UNSET
    content_text: str | Unset = UNSET
    date_published: datetime.datetime | Unset = UNSET
    date_modified: datetime.datetime | Unset = UNSET
    tags: list[str] | Unset = UNSET
    authors: list[JsonFeedItemAuthorsItem] | Unset = UNSET
    field_foresignal: JsonFeedItemForesignal | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        title = self.title

        url = self.url

        content_html = self.content_html

        content_text = self.content_text

        date_published: str | Unset = UNSET
        if not isinstance(self.date_published, Unset):
            date_published = self.date_published.isoformat()

        date_modified: str | Unset = UNSET
        if not isinstance(self.date_modified, Unset):
            date_modified = self.date_modified.isoformat()

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        authors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.authors, Unset):
            authors = []
            for authors_item_data in self.authors:
                authors_item = authors_item_data.to_dict()
                authors.append(authors_item)

        field_foresignal: dict[str, Any] | Unset = UNSET
        if not isinstance(self.field_foresignal, Unset):
            field_foresignal = self.field_foresignal.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
            }
        )
        if url is not UNSET:
            field_dict["url"] = url
        if content_html is not UNSET:
            field_dict["content_html"] = content_html
        if content_text is not UNSET:
            field_dict["content_text"] = content_text
        if date_published is not UNSET:
            field_dict["date_published"] = date_published
        if date_modified is not UNSET:
            field_dict["date_modified"] = date_modified
        if tags is not UNSET:
            field_dict["tags"] = tags
        if authors is not UNSET:
            field_dict["authors"] = authors
        if field_foresignal is not UNSET:
            field_dict["_foresignal"] = field_foresignal

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.json_feed_item_authors_item import JsonFeedItemAuthorsItem
        from ..models.json_feed_item_foresignal import JsonFeedItemForesignal

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        url = d.pop("url", UNSET)

        content_html = d.pop("content_html", UNSET)

        content_text = d.pop("content_text", UNSET)

        _date_published = d.pop("date_published", UNSET)
        date_published: datetime.datetime | Unset
        if isinstance(_date_published, Unset):
            date_published = UNSET
        else:
            date_published = datetime.datetime.fromisoformat(_date_published)

        _date_modified = d.pop("date_modified", UNSET)
        date_modified: datetime.datetime | Unset
        if isinstance(_date_modified, Unset):
            date_modified = UNSET
        else:
            date_modified = datetime.datetime.fromisoformat(_date_modified)

        tags = cast(list[str], d.pop("tags", UNSET))

        _authors = d.pop("authors", UNSET)
        authors: list[JsonFeedItemAuthorsItem] | Unset = UNSET
        if _authors is not UNSET:
            authors = []
            for authors_item_data in _authors:
                authors_item = JsonFeedItemAuthorsItem.from_dict(authors_item_data)

                authors.append(authors_item)

        _field_foresignal = d.pop("_foresignal", UNSET)
        field_foresignal: JsonFeedItemForesignal | Unset
        if isinstance(_field_foresignal, Unset):
            field_foresignal = UNSET
        else:
            field_foresignal = JsonFeedItemForesignal.from_dict(_field_foresignal)

        json_feed_item = cls(
            id=id,
            title=title,
            url=url,
            content_html=content_html,
            content_text=content_text,
            date_published=date_published,
            date_modified=date_modified,
            tags=tags,
            authors=authors,
            field_foresignal=field_foresignal,
        )

        json_feed_item.additional_properties = d
        return json_feed_item

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
