from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.briefing_run_delivery_results_type_0_item_format import (
    BriefingRunDeliveryResultsType0ItemFormat,
)
from ..models.briefing_run_delivery_results_type_0_item_kind import (
    BriefingRunDeliveryResultsType0ItemKind,
)
from ..models.briefing_run_delivery_results_type_0_item_status import (
    BriefingRunDeliveryResultsType0ItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="BriefingRunDeliveryResultsType0Item")


@_attrs_define
class BriefingRunDeliveryResultsType0Item:
    """
    Attributes:
        kind (BriefingRunDeliveryResultsType0ItemKind):
        format_ (BriefingRunDeliveryResultsType0ItemFormat):
        status (BriefingRunDeliveryResultsType0ItemStatus): `queued` handed to the channel (its own delivery is in the
            notification's deliveries); `available` a pulled target (`website`, `podcast`) the run can be read at; `skipped`
            not attempted (`error` says why: a missing, disabled or foreign channel, no audio); `failed` the send failed
            (`error`). One target's failure never stops the others
        channel_id (None | Unset | UUID):
        notification_id (int | None | Unset): The notification published to the channel
        url (None | str | Unset): Where a pulled target is available
        error (None | str | Unset):
    """

    kind: BriefingRunDeliveryResultsType0ItemKind
    format_: BriefingRunDeliveryResultsType0ItemFormat
    status: BriefingRunDeliveryResultsType0ItemStatus
    channel_id: None | Unset | UUID = UNSET
    notification_id: int | None | Unset = UNSET
    url: None | str | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        format_ = self.format_.value

        status = self.status.value

        channel_id: None | str | Unset
        if isinstance(self.channel_id, Unset):
            channel_id = UNSET
        elif isinstance(self.channel_id, UUID):
            channel_id = str(self.channel_id)
        else:
            channel_id = self.channel_id

        notification_id: int | None | Unset
        if isinstance(self.notification_id, Unset):
            notification_id = UNSET
        else:
            notification_id = self.notification_id

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "format": format_,
                "status": status,
            }
        )
        if channel_id is not UNSET:
            field_dict["channelId"] = channel_id
        if notification_id is not UNSET:
            field_dict["notificationId"] = notification_id
        if url is not UNSET:
            field_dict["url"] = url
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = BriefingRunDeliveryResultsType0ItemKind(d.pop("kind"))

        format_ = BriefingRunDeliveryResultsType0ItemFormat(d.pop("format"))

        status = BriefingRunDeliveryResultsType0ItemStatus(d.pop("status"))

        def _parse_channel_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                channel_id_type_0 = UUID(data)

                return channel_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        channel_id = _parse_channel_id(d.pop("channelId", UNSET))

        def _parse_notification_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        notification_id = _parse_notification_id(d.pop("notificationId", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        briefing_run_delivery_results_type_0_item = cls(
            kind=kind,
            format_=format_,
            status=status,
            channel_id=channel_id,
            notification_id=notification_id,
            url=url,
            error=error,
        )

        briefing_run_delivery_results_type_0_item.additional_properties = d
        return briefing_run_delivery_results_type_0_item

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
