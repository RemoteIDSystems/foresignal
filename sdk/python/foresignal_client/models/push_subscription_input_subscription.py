from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.push_subscription_input_subscription_keys import (
        PushSubscriptionInputSubscriptionKeys,
    )


T = TypeVar("T", bound="PushSubscriptionInputSubscription")


@_attrs_define
class PushSubscriptionInputSubscription:
    """
    Attributes:
        endpoint (str):
        keys (PushSubscriptionInputSubscriptionKeys):
    """

    endpoint: str
    keys: PushSubscriptionInputSubscriptionKeys
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        endpoint = self.endpoint

        keys = self.keys.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "endpoint": endpoint,
                "keys": keys,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_subscription_input_subscription_keys import (
            PushSubscriptionInputSubscriptionKeys,
        )

        d = dict(src_dict)
        endpoint = d.pop("endpoint")

        keys = PushSubscriptionInputSubscriptionKeys.from_dict(d.pop("keys"))

        push_subscription_input_subscription = cls(
            endpoint=endpoint,
            keys=keys,
        )

        push_subscription_input_subscription.additional_properties = d
        return push_subscription_input_subscription

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
