from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="UserPreferencesNotifications")


@_attrs_define
class UserPreferencesNotifications:
    """Per-user notification defaults

    Attributes:
        email_enabled (bool | Unset):  Default: True.
        push_enabled (bool | Unset):  Default: True.
        quiet_hours (None | str | Unset): e.g. `22:00-07:00` in the user's timezone
    """

    email_enabled: bool | Unset = True
    push_enabled: bool | Unset = True
    quiet_hours: None | str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        email_enabled = self.email_enabled

        push_enabled = self.push_enabled

        quiet_hours: None | str | Unset
        if isinstance(self.quiet_hours, Unset):
            quiet_hours = UNSET
        else:
            quiet_hours = self.quiet_hours

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if email_enabled is not UNSET:
            field_dict["emailEnabled"] = email_enabled
        if push_enabled is not UNSET:
            field_dict["pushEnabled"] = push_enabled
        if quiet_hours is not UNSET:
            field_dict["quietHours"] = quiet_hours

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        email_enabled = d.pop("emailEnabled", UNSET)

        push_enabled = d.pop("pushEnabled", UNSET)

        def _parse_quiet_hours(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        quiet_hours = _parse_quiet_hours(d.pop("quietHours", UNSET))

        user_preferences_notifications = cls(
            email_enabled=email_enabled,
            push_enabled=push_enabled,
            quiet_hours=quiet_hours,
        )

        return user_preferences_notifications
