from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attention_topic_input_wiki_articles_item import (
        AttentionTopicInputWikiArticlesItem,
    )
    from ..models.attention_topic_social import AttentionTopicSocial


T = TypeVar("T", bound="AttentionTopicInput")


@_attrs_define
class AttentionTopicInput:
    """Create or replace an attention topic (117)

    Attributes:
        id (str):
        name (str):
        description (None | str | Unset):
        gdelt_query (None | str | Unset): GDELT DOC 2.0 query
        wiki_articles (list[AttentionTopicInputWikiArticlesItem] | Unset):
        keywords (list[str] | Unset): Used by 118's linker
        region_codes (list[str] | Unset): 040 region codes
        themes (list[str] | Unset):
        enabled (bool | Unset):
        social (AttentionTopicSocial | None | Unset): Social tracking block (135): terms, hashtags, accounts, channels,
            excludes, platforms, places, regions, entities, mode
    """

    id: str
    name: str
    description: None | str | Unset = UNSET
    gdelt_query: None | str | Unset = UNSET
    wiki_articles: list[AttentionTopicInputWikiArticlesItem] | Unset = UNSET
    keywords: list[str] | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    themes: list[str] | Unset = UNSET
    enabled: bool | Unset = UNSET
    social: AttentionTopicSocial | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.attention_topic_social import AttentionTopicSocial

        id = self.id

        name = self.name

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        gdelt_query: None | str | Unset
        if isinstance(self.gdelt_query, Unset):
            gdelt_query = UNSET
        else:
            gdelt_query = self.gdelt_query

        wiki_articles: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.wiki_articles, Unset):
            wiki_articles = []
            for wiki_articles_item_data in self.wiki_articles:
                wiki_articles_item = wiki_articles_item_data.to_dict()
                wiki_articles.append(wiki_articles_item)

        keywords: list[str] | Unset = UNSET
        if not isinstance(self.keywords, Unset):
            keywords = self.keywords

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        themes: list[str] | Unset = UNSET
        if not isinstance(self.themes, Unset):
            themes = self.themes

        enabled = self.enabled

        social: dict[str, Any] | None | Unset
        if isinstance(self.social, Unset):
            social = UNSET
        elif isinstance(self.social, AttentionTopicSocial):
            social = self.social.to_dict()
        else:
            social = self.social

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if gdelt_query is not UNSET:
            field_dict["gdeltQuery"] = gdelt_query
        if wiki_articles is not UNSET:
            field_dict["wikiArticles"] = wiki_articles
        if keywords is not UNSET:
            field_dict["keywords"] = keywords
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if themes is not UNSET:
            field_dict["themes"] = themes
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if social is not UNSET:
            field_dict["social"] = social

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attention_topic_input_wiki_articles_item import (
            AttentionTopicInputWikiArticlesItem,
        )
        from ..models.attention_topic_social import AttentionTopicSocial

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_gdelt_query(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        gdelt_query = _parse_gdelt_query(d.pop("gdeltQuery", UNSET))

        _wiki_articles = d.pop("wikiArticles", UNSET)
        wiki_articles: list[AttentionTopicInputWikiArticlesItem] | Unset = UNSET
        if _wiki_articles is not UNSET:
            wiki_articles = []
            for wiki_articles_item_data in _wiki_articles:
                wiki_articles_item = AttentionTopicInputWikiArticlesItem.from_dict(
                    wiki_articles_item_data
                )

                wiki_articles.append(wiki_articles_item)

        keywords = cast(list[str], d.pop("keywords", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        themes = cast(list[str], d.pop("themes", UNSET))

        enabled = d.pop("enabled", UNSET)

        def _parse_social(data: object) -> AttentionTopicSocial | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                social_type_0 = AttentionTopicSocial.from_dict(data)

                return social_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AttentionTopicSocial | None | Unset, data)

        social = _parse_social(d.pop("social", UNSET))

        attention_topic_input = cls(
            id=id,
            name=name,
            description=description,
            gdelt_query=gdelt_query,
            wiki_articles=wiki_articles,
            keywords=keywords,
            region_codes=region_codes,
            themes=themes,
            enabled=enabled,
            social=social,
        )

        attention_topic_input.additional_properties = d
        return attention_topic_input

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
