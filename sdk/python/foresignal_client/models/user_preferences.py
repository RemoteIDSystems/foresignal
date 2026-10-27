from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.user_preferences_notifications import UserPreferencesNotifications
    from ..models.user_preferences_ui import UserPreferencesUi


T = TypeVar("T", bound="UserPreferences")


@_attrs_define
class UserPreferences:
    """The calling user's preferences (timezone, UI and notification defaults, 175)

    Attributes:
        timezone (None | str | Unset): IANA zone; falls back to the server setting when null
        locale (None | str | Unset):
        theme (None | str | Unset):
        notifications (UserPreferencesNotifications | Unset): Per-user notification defaults
        ui (UserPreferencesUi | Unset): Free-form UI preferences
    """

    timezone: None | str | Unset = UNSET
    locale: None | str | Unset = UNSET
    theme: None | str | Unset = UNSET
    notifications: UserPreferencesNotifications | Unset = UNSET
    ui: UserPreferencesUi | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        timezone: None | str | Unset
        if isinstance(self.timezone, Unset):
            timezone = UNSET
        else:
            timezone = self.timezone

        locale: None | str | Unset
        if isinstance(self.locale, Unset):
            locale = UNSET
        else:
            locale = self.locale

        theme: None | str | Unset
        if isinstance(self.theme, Unset):
            theme = UNSET
        else:
            theme = self.theme

        notifications: dict[str, Any] | Unset = UNSET
        if not isinstance(self.notifications, Unset):
            notifications = self.notifications.to_dict()

        ui: dict[str, Any] | Unset = UNSET
        if not isinstance(self.ui, Unset):
            ui = self.ui.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if locale is not UNSET:
            field_dict["locale"] = locale
        if theme is not UNSET:
            field_dict["theme"] = theme
        if notifications is not UNSET:
            field_dict["notifications"] = notifications
        if ui is not UNSET:
            field_dict["ui"] = ui

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.user_preferences_notifications import UserPreferencesNotifications
        from ..models.user_preferences_ui import UserPreferencesUi

        d = dict(src_dict)

        def _parse_timezone(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        timezone = _parse_timezone(d.pop("timezone", UNSET))

        def _parse_locale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        locale = _parse_locale(d.pop("locale", UNSET))

        def _parse_theme(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        theme = _parse_theme(d.pop("theme", UNSET))

        _notifications = d.pop("notifications", UNSET)
        notifications: UserPreferencesNotifications | Unset
        if isinstance(_notifications, Unset):
            notifications = UNSET
        else:
            notifications = UserPreferencesNotifications.from_dict(_notifications)

        _ui = d.pop("ui", UNSET)
        ui: UserPreferencesUi | Unset
        if isinstance(_ui, Unset):
            ui = UNSET
        else:
            ui = UserPreferencesUi.from_dict(_ui)

        user_preferences = cls(
            timezone=timezone,
            locale=locale,
            theme=theme,
            notifications=notifications,
            ui=ui,
        )

        user_preferences.additional_properties = d
        return user_preferences

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
