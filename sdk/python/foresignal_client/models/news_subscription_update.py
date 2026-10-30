from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.news_subscription import NewsSubscription


T = TypeVar("T", bound="NewsSubscriptionUpdate")


@_attrs_define
class NewsSubscriptionUpdate:
    """Replace the caller's news subscriptions. With no entry of a type there is no restriction of that type; with at least
    one, the caller's editions keep only news matching one of them (an outlet entry matches its outlet, a language entry
    an article in that language or translated into it, NP-002).

        Attributes:
            subscriptions (list[NewsSubscription]):
    """

    subscriptions: list[NewsSubscription]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subscriptions = []
        for subscriptions_item_data in self.subscriptions:
            subscriptions_item = subscriptions_item_data.to_dict()
            subscriptions.append(subscriptions_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subscriptions": subscriptions,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.news_subscription import NewsSubscription

        d = dict(src_dict)
        subscriptions = []
        _subscriptions = d.pop("subscriptions")
        for subscriptions_item_data in _subscriptions:
            subscriptions_item = NewsSubscription.from_dict(subscriptions_item_data)

            subscriptions.append(subscriptions_item)

        news_subscription_update = cls(
            subscriptions=subscriptions,
        )

        news_subscription_update.additional_properties = d
        return news_subscription_update

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
