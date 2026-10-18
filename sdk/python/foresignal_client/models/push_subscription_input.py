from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_subscription_input_payload_format import (
    PushSubscriptionInputPayloadFormat,
)
from ..models.push_subscription_input_transport_label import (
    PushSubscriptionInputTransportLabel,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_filters import PushFilters
    from ..models.push_subscription_input_subscription import (
        PushSubscriptionInputSubscription,
    )


T = TypeVar("T", bound="PushSubscriptionInput")


@_attrs_define
class PushSubscriptionInput:
    """
    Attributes:
        subscription (PushSubscriptionInputSubscription):
        name (str | Unset):
        filters (PushFilters | Unset): Which notifications a push target receives (031; shared by subscriptions and
            native devices)
        transport_label (PushSubscriptionInputTransportLabel | Unset): Display and filter only; the wire protocol is
            identical Default: PushSubscriptionInputTransportLabel.BROWSER.
        payload_format (PushSubscriptionInputPayloadFormat | Unset):  Default:
            PushSubscriptionInputPayloadFormat.FORESIGNAL.
        lang (None | str | Unset):
    """

    subscription: PushSubscriptionInputSubscription
    name: str | Unset = UNSET
    filters: PushFilters | Unset = UNSET
    transport_label: PushSubscriptionInputTransportLabel | Unset = (
        PushSubscriptionInputTransportLabel.BROWSER
    )
    payload_format: PushSubscriptionInputPayloadFormat | Unset = (
        PushSubscriptionInputPayloadFormat.FORESIGNAL
    )
    lang: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subscription = self.subscription.to_dict()

        name = self.name

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        transport_label: str | Unset = UNSET
        if not isinstance(self.transport_label, Unset):
            transport_label = self.transport_label.value

        payload_format: str | Unset = UNSET
        if not isinstance(self.payload_format, Unset):
            payload_format = self.payload_format.value

        lang: None | str | Unset
        if isinstance(self.lang, Unset):
            lang = UNSET
        else:
            lang = self.lang

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subscription": subscription,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if filters is not UNSET:
            field_dict["filters"] = filters
        if transport_label is not UNSET:
            field_dict["transportLabel"] = transport_label
        if payload_format is not UNSET:
            field_dict["payloadFormat"] = payload_format
        if lang is not UNSET:
            field_dict["lang"] = lang

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_filters import PushFilters
        from ..models.push_subscription_input_subscription import (
            PushSubscriptionInputSubscription,
        )

        d = dict(src_dict)
        subscription = PushSubscriptionInputSubscription.from_dict(
            d.pop("subscription")
        )

        name = d.pop("name", UNSET)

        _filters = d.pop("filters", UNSET)
        filters: PushFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = PushFilters.from_dict(_filters)

        _transport_label = d.pop("transportLabel", UNSET)
        transport_label: PushSubscriptionInputTransportLabel | Unset
        if isinstance(_transport_label, Unset):
            transport_label = UNSET
        else:
            transport_label = PushSubscriptionInputTransportLabel(_transport_label)

        _payload_format = d.pop("payloadFormat", UNSET)
        payload_format: PushSubscriptionInputPayloadFormat | Unset
        if isinstance(_payload_format, Unset):
            payload_format = UNSET
        else:
            payload_format = PushSubscriptionInputPayloadFormat(_payload_format)

        def _parse_lang(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lang = _parse_lang(d.pop("lang", UNSET))

        push_subscription_input = cls(
            subscription=subscription,
            name=name,
            filters=filters,
            transport_label=transport_label,
            payload_format=payload_format,
            lang=lang,
        )

        push_subscription_input.additional_properties = d
        return push_subscription_input

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
