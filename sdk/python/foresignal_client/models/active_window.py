from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.active_window_windows_item import ActiveWindowWindowsItem


T = TypeVar("T", bound="ActiveWindow")


@_attrs_define
class ActiveWindow:
    """Recurring local-time windows in which a rule is active (DST-safe, evaluated on the version's observedAt)

    Attributes:
        timezone (str): IANA timezone Example: America/Chicago.
        windows (list[ActiveWindowWindowsItem]):
    """

    timezone: str
    windows: list[ActiveWindowWindowsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        timezone = self.timezone

        windows = []
        for windows_item_data in self.windows:
            windows_item = windows_item_data.to_dict()
            windows.append(windows_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "timezone": timezone,
                "windows": windows,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.active_window_windows_item import ActiveWindowWindowsItem

        d = dict(src_dict)
        timezone = d.pop("timezone")

        windows = []
        _windows = d.pop("windows")
        for windows_item_data in _windows:
            windows_item = ActiveWindowWindowsItem.from_dict(windows_item_data)

            windows.append(windows_item)

        active_window = cls(
            timezone=timezone,
            windows=windows,
        )

        active_window.additional_properties = d
        return active_window

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
