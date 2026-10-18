from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="NotificationBadge")


@_attrs_define
class NotificationBadge:
    """Unread counts for the calling device (033); also the `badge` SSE event

    Attributes:
        unread (int):
        unread_critical (int):
        latest_id (int | None | Unset):
    """

    unread: int
    unread_critical: int
    latest_id: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        unread = self.unread

        unread_critical = self.unread_critical

        latest_id: int | None | Unset
        if isinstance(self.latest_id, Unset):
            latest_id = UNSET
        else:
            latest_id = self.latest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "unread": unread,
                "unreadCritical": unread_critical,
            }
        )
        if latest_id is not UNSET:
            field_dict["latestId"] = latest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        unread = d.pop("unread")

        unread_critical = d.pop("unreadCritical")

        def _parse_latest_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        latest_id = _parse_latest_id(d.pop("latestId", UNSET))

        notification_badge = cls(
            unread=unread,
            unread_critical=unread_critical,
            latest_id=latest_id,
        )

        notification_badge.additional_properties = d
        return notification_badge

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
