from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="NewsArticle")


@_attrs_define
class NewsArticle:
    """A news article (metadata only; no body, no bylines or author names)

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        external_id (str): sha256 of the canonicalised URL
        title (str):
        url (str):
        domain (str):
        seen_at (datetime.datetime):
        source_id (str): `gdelt-articles` or a `news-feed-*` source
        outlet_id (None | str | Unset):
        outlet_name (None | str | Unset):
        language (None | str | Unset):
        source_country (None | str | Unset):
        published_at (datetime.datetime | None | Unset):
        topic_ids (list[str] | Unset):
        summary (None | str | Unset): Feed summary only
        image_url (None | str | Unset): Link only
        story_id (None | str | Unset):
        evidence_group_id (None | str | Unset):
        syndicated (bool | Unset): Reprint of a wire story
    """

    ref: str
    external_id: str
    title: str
    url: str
    domain: str
    seen_at: datetime.datetime
    source_id: str
    outlet_id: None | str | Unset = UNSET
    outlet_name: None | str | Unset = UNSET
    language: None | str | Unset = UNSET
    source_country: None | str | Unset = UNSET
    published_at: datetime.datetime | None | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    summary: None | str | Unset = UNSET
    image_url: None | str | Unset = UNSET
    story_id: None | str | Unset = UNSET
    evidence_group_id: None | str | Unset = UNSET
    syndicated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        external_id = self.external_id

        title = self.title

        url = self.url

        domain = self.domain

        seen_at = self.seen_at.isoformat()

        source_id = self.source_id

        outlet_id: None | str | Unset
        if isinstance(self.outlet_id, Unset):
            outlet_id = UNSET
        else:
            outlet_id = self.outlet_id

        outlet_name: None | str | Unset
        if isinstance(self.outlet_name, Unset):
            outlet_name = UNSET
        else:
            outlet_name = self.outlet_name

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        source_country: None | str | Unset
        if isinstance(self.source_country, Unset):
            source_country = UNSET
        else:
            source_country = self.source_country

        published_at: None | str | Unset
        if isinstance(self.published_at, Unset):
            published_at = UNSET
        elif isinstance(self.published_at, datetime.datetime):
            published_at = self.published_at.isoformat()
        else:
            published_at = self.published_at

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

        image_url: None | str | Unset
        if isinstance(self.image_url, Unset):
            image_url = UNSET
        else:
            image_url = self.image_url

        story_id: None | str | Unset
        if isinstance(self.story_id, Unset):
            story_id = UNSET
        else:
            story_id = self.story_id

        evidence_group_id: None | str | Unset
        if isinstance(self.evidence_group_id, Unset):
            evidence_group_id = UNSET
        else:
            evidence_group_id = self.evidence_group_id

        syndicated = self.syndicated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "externalId": external_id,
                "title": title,
                "url": url,
                "domain": domain,
                "seenAt": seen_at,
                "sourceId": source_id,
            }
        )
        if outlet_id is not UNSET:
            field_dict["outletId"] = outlet_id
        if outlet_name is not UNSET:
            field_dict["outletName"] = outlet_name
        if language is not UNSET:
            field_dict["language"] = language
        if source_country is not UNSET:
            field_dict["sourceCountry"] = source_country
        if published_at is not UNSET:
            field_dict["publishedAt"] = published_at
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if summary is not UNSET:
            field_dict["summary"] = summary
        if image_url is not UNSET:
            field_dict["imageUrl"] = image_url
        if story_id is not UNSET:
            field_dict["storyId"] = story_id
        if evidence_group_id is not UNSET:
            field_dict["evidenceGroupId"] = evidence_group_id
        if syndicated is not UNSET:
            field_dict["syndicated"] = syndicated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        external_id = d.pop("externalId")

        title = d.pop("title")

        url = d.pop("url")

        domain = d.pop("domain")

        seen_at = datetime.datetime.fromisoformat(d.pop("seenAt"))

        source_id = d.pop("sourceId")

        def _parse_outlet_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        outlet_id = _parse_outlet_id(d.pop("outletId", UNSET))

        def _parse_outlet_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        outlet_name = _parse_outlet_name(d.pop("outletName", UNSET))

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        def _parse_source_country(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_country = _parse_source_country(d.pop("sourceCountry", UNSET))

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

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

        def _parse_image_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        image_url = _parse_image_url(d.pop("imageUrl", UNSET))

        def _parse_story_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        story_id = _parse_story_id(d.pop("storyId", UNSET))

        def _parse_evidence_group_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        evidence_group_id = _parse_evidence_group_id(d.pop("evidenceGroupId", UNSET))

        syndicated = d.pop("syndicated", UNSET)

        news_article = cls(
            ref=ref,
            external_id=external_id,
            title=title,
            url=url,
            domain=domain,
            seen_at=seen_at,
            source_id=source_id,
            outlet_id=outlet_id,
            outlet_name=outlet_name,
            language=language,
            source_country=source_country,
            published_at=published_at,
            topic_ids=topic_ids,
            summary=summary,
            image_url=image_url,
            story_id=story_id,
            evidence_group_id=evidence_group_id,
            syndicated=syndicated,
        )

        news_article.additional_properties = d
        return news_article

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
