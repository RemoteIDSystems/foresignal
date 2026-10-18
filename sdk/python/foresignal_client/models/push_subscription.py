from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_subscription_payload_format import PushSubscriptionPayloadFormat
from ..models.push_subscription_status import PushSubscriptionStatus
from ..models.push_subscription_transport_label import PushSubscriptionTransportLabel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_filters import PushFilters


T = TypeVar("T", bound="PushSubscription")


@_attrs_define
class PushSubscription:
    """
    Attributes:
        id (UUID):
        endpoint_host (str):
        created_at (datetime.datetime):
        name (None | str | Unset):
        filters (PushFilters | Unset): Which notifications a push target receives (031; shared by subscriptions and
            native devices)
        last_success_at (datetime.datetime | None | Unset):
        status (PushSubscriptionStatus | Unset): pending until the verification code is echoed; active receives
            notifications; disabled after three consecutive failed send-time address rechecks (URL_NOT_ALLOWED)
        transport_label (PushSubscriptionTransportLabel | Unset):
        payload_format (PushSubscriptionPayloadFormat | Unset):
        lang (None | str | Unset):
        failure_count (int | Unset):
        last_failure_at (datetime.datetime | None | Unset):
        last_status (int | None | Unset): Last push-service HTTP status
    """

    id: UUID
    endpoint_host: str
    created_at: datetime.datetime
    name: None | str | Unset = UNSET
    filters: PushFilters | Unset = UNSET
    last_success_at: datetime.datetime | None | Unset = UNSET
    status: PushSubscriptionStatus | Unset = UNSET
    transport_label: PushSubscriptionTransportLabel | Unset = UNSET
    payload_format: PushSubscriptionPayloadFormat | Unset = UNSET
    lang: None | str | Unset = UNSET
    failure_count: int | Unset = UNSET
    last_failure_at: datetime.datetime | None | Unset = UNSET
    last_status: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = str(self.id)

        endpoint_host = self.endpoint_host

        created_at = self.created_at.isoformat()

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        last_success_at: None | str | Unset
        if isinstance(self.last_success_at, Unset):
            last_success_at = UNSET
        elif isinstance(self.last_success_at, datetime.datetime):
            last_success_at = self.last_success_at.isoformat()
        else:
            last_success_at = self.last_success_at

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

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

        failure_count = self.failure_count

        last_failure_at: None | str | Unset
        if isinstance(self.last_failure_at, Unset):
            last_failure_at = UNSET
        elif isinstance(self.last_failure_at, datetime.datetime):
            last_failure_at = self.last_failure_at.isoformat()
        else:
            last_failure_at = self.last_failure_at

        last_status: int | None | Unset
        if isinstance(self.last_status, Unset):
            last_status = UNSET
        else:
            last_status = self.last_status

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "endpointHost": endpoint_host,
                "createdAt": created_at,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if filters is not UNSET:
            field_dict["filters"] = filters
        if last_success_at is not UNSET:
            field_dict["lastSuccessAt"] = last_success_at
        if status is not UNSET:
            field_dict["status"] = status
        if transport_label is not UNSET:
            field_dict["transportLabel"] = transport_label
        if payload_format is not UNSET:
            field_dict["payloadFormat"] = payload_format
        if lang is not UNSET:
            field_dict["lang"] = lang
        if failure_count is not UNSET:
            field_dict["failureCount"] = failure_count
        if last_failure_at is not UNSET:
            field_dict["lastFailureAt"] = last_failure_at
        if last_status is not UNSET:
            field_dict["lastStatus"] = last_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_filters import PushFilters

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        endpoint_host = d.pop("endpointHost")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        _filters = d.pop("filters", UNSET)
        filters: PushFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = PushFilters.from_dict(_filters)

        def _parse_last_success_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_success_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_success_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_success_at = _parse_last_success_at(d.pop("lastSuccessAt", UNSET))

        _status = d.pop("status", UNSET)
        status: PushSubscriptionStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = PushSubscriptionStatus(_status)

        _transport_label = d.pop("transportLabel", UNSET)
        transport_label: PushSubscriptionTransportLabel | Unset
        if isinstance(_transport_label, Unset):
            transport_label = UNSET
        else:
            transport_label = PushSubscriptionTransportLabel(_transport_label)

        _payload_format = d.pop("payloadFormat", UNSET)
        payload_format: PushSubscriptionPayloadFormat | Unset
        if isinstance(_payload_format, Unset):
            payload_format = UNSET
        else:
            payload_format = PushSubscriptionPayloadFormat(_payload_format)

        def _parse_lang(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lang = _parse_lang(d.pop("lang", UNSET))

        failure_count = d.pop("failureCount", UNSET)

        def _parse_last_failure_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_failure_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_failure_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_failure_at = _parse_last_failure_at(d.pop("lastFailureAt", UNSET))

        def _parse_last_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_status = _parse_last_status(d.pop("lastStatus", UNSET))

        push_subscription = cls(
            id=id,
            endpoint_host=endpoint_host,
            created_at=created_at,
            name=name,
            filters=filters,
            last_success_at=last_success_at,
            status=status,
            transport_label=transport_label,
            payload_format=payload_format,
            lang=lang,
            failure_count=failure_count,
            last_failure_at=last_failure_at,
            last_status=last_status,
        )

        push_subscription.additional_properties = d
        return push_subscription

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
