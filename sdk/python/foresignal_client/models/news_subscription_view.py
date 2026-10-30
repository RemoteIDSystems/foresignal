from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.news_subscription import NewsSubscription


T = TypeVar("T", bound="NewsSubscriptionView")


@_attrs_define
class NewsSubscriptionView:
    """The caller's news subscriptions (NP-003). With no entry of a type there is no restriction of that type; with at
    least one, the caller's editions keep only news matching one of them (an outlet entry matches its outlet, a language
    entry an article in that language or translated into it, NP-002).

        Attributes:
            subscriptions (list[NewsSubscription]):
            updated_at (datetime.datetime | None | Unset): When the caller last replaced the subscriptions, or null when
                never
    """

    subscriptions: list[NewsSubscription]
    updated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subscriptions = []
        for subscriptions_item_data in self.subscriptions:
            subscriptions_item = subscriptions_item_data.to_dict()
            subscriptions.append(subscriptions_item)

        updated_at: None | str | Unset
        if isinstance(self.updated_at, Unset):
            updated_at = UNSET
        elif isinstance(self.updated_at, datetime.datetime):
            updated_at = self.updated_at.isoformat()
        else:
            updated_at = self.updated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subscriptions": subscriptions,
            }
        )
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

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

        def _parse_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        updated_at = _parse_updated_at(d.pop("updatedAt", UNSET))

        news_subscription_view = cls(
            subscriptions=subscriptions,
            updated_at=updated_at,
        )

        news_subscription_view.additional_properties = d
        return news_subscription_view

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
