from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.media_outlet import MediaOutlet
    from ..models.news_article import NewsArticle


T = TypeVar("T", bound="NewsStoryOutletsItem")


@_attrs_define
class NewsStoryOutletsItem:
    """Articles grouped by outlet (detail view)

    Attributes:
        outlet (MediaOutlet): A news outlet in the registry with ratings per provider and consensus
        articles (list[NewsArticle]):
        evidence_group_id (None | str | Unset):
    """

    outlet: MediaOutlet
    articles: list[NewsArticle]
    evidence_group_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        outlet = self.outlet.to_dict()

        articles = []
        for articles_item_data in self.articles:
            articles_item = articles_item_data.to_dict()
            articles.append(articles_item)

        evidence_group_id: None | str | Unset
        if isinstance(self.evidence_group_id, Unset):
            evidence_group_id = UNSET
        else:
            evidence_group_id = self.evidence_group_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "outlet": outlet,
                "articles": articles,
            }
        )
        if evidence_group_id is not UNSET:
            field_dict["evidenceGroupId"] = evidence_group_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.media_outlet import MediaOutlet
        from ..models.news_article import NewsArticle

        d = dict(src_dict)
        outlet = MediaOutlet.from_dict(d.pop("outlet"))

        articles = []
        _articles = d.pop("articles")
        for articles_item_data in _articles:
            articles_item = NewsArticle.from_dict(articles_item_data)

            articles.append(articles_item)

        def _parse_evidence_group_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        evidence_group_id = _parse_evidence_group_id(d.pop("evidenceGroupId", UNSET))

        news_story_outlets_item = cls(
            outlet=outlet,
            articles=articles,
            evidence_group_id=evidence_group_id,
        )

        news_story_outlets_item.additional_properties = d
        return news_story_outlets_item

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
