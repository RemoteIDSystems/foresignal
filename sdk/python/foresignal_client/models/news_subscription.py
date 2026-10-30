from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.news_subscription_type import NewsSubscriptionType

T = TypeVar("T", bound="NewsSubscription")


@_attrs_define
class NewsSubscription:
    """One entry of the caller's news subscriptions: an outlet (133) or a language the caller follows

    Attributes:
        type_ (NewsSubscriptionType):
        id (str): A media outlet id (`listMediaOutlets`) for `outlet`; a BCP 47 language tag for `language`
    """

    type_: NewsSubscriptionType
    id: str

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        id = self.id

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "type": type_,
                "id": id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = NewsSubscriptionType(d.pop("type"))

        id = d.pop("id")

        news_subscription = cls(
            type_=type_,
            id=id,
        )

        return news_subscription
