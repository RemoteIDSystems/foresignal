from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.json_feed_version import JsonFeedVersion
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.json_feed_authors_item import JsonFeedAuthorsItem
    from ..models.json_feed_item import JsonFeedItem


T = TypeVar("T", bound="JsonFeed")


@_attrs_define
class JsonFeed:
    """JSON Feed 1.1 document for alerts or briefing runs (058). Property names follow the JSON Feed spec (snake_case).

    Attributes:
        version (JsonFeedVersion):
        title (str):
        items (list[JsonFeedItem]):
        home_page_url (str | Unset):
        feed_url (str | Unset):
        description (str | Unset):
        language (str | Unset):
        authors (list[JsonFeedAuthorsItem] | Unset):
    """

    version: JsonFeedVersion
    title: str
    items: list[JsonFeedItem]
    home_page_url: str | Unset = UNSET
    feed_url: str | Unset = UNSET
    description: str | Unset = UNSET
    language: str | Unset = UNSET
    authors: list[JsonFeedAuthorsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        version = self.version.value

        title = self.title

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        home_page_url = self.home_page_url

        feed_url = self.feed_url

        description = self.description

        language = self.language

        authors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.authors, Unset):
            authors = []
            for authors_item_data in self.authors:
                authors_item = authors_item_data.to_dict()
                authors.append(authors_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "title": title,
                "items": items,
            }
        )
        if home_page_url is not UNSET:
            field_dict["home_page_url"] = home_page_url
        if feed_url is not UNSET:
            field_dict["feed_url"] = feed_url
        if description is not UNSET:
            field_dict["description"] = description
        if language is not UNSET:
            field_dict["language"] = language
        if authors is not UNSET:
            field_dict["authors"] = authors

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.json_feed_authors_item import JsonFeedAuthorsItem
        from ..models.json_feed_item import JsonFeedItem

        d = dict(src_dict)
        version = JsonFeedVersion(d.pop("version"))

        title = d.pop("title")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = JsonFeedItem.from_dict(items_item_data)

            items.append(items_item)

        home_page_url = d.pop("home_page_url", UNSET)

        feed_url = d.pop("feed_url", UNSET)

        description = d.pop("description", UNSET)

        language = d.pop("language", UNSET)

        _authors = d.pop("authors", UNSET)
        authors: list[JsonFeedAuthorsItem] | Unset = UNSET
        if _authors is not UNSET:
            authors = []
            for authors_item_data in _authors:
                authors_item = JsonFeedAuthorsItem.from_dict(authors_item_data)

                authors.append(authors_item)

        json_feed = cls(
            version=version,
            title=title,
            items=items,
            home_page_url=home_page_url,
            feed_url=feed_url,
            description=description,
            language=language,
            authors=authors,
        )

        json_feed.additional_properties = d
        return json_feed

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
