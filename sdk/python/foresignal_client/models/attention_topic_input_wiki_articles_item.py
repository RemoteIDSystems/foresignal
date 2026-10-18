from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="AttentionTopicInputWikiArticlesItem")


@_attrs_define
class AttentionTopicInputWikiArticlesItem:
    """A Wikipedia article

    Attributes:
        project (str):  Example: en.wikipedia.
        article (str):
    """

    project: str
    article: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        project = self.project

        article = self.article

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "project": project,
                "article": article,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        project = d.pop("project")

        article = d.pop("article")

        attention_topic_input_wiki_articles_item = cls(
            project=project,
            article=article,
        )

        attention_topic_input_wiki_articles_item.additional_properties = d
        return attention_topic_input_wiki_articles_item

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
