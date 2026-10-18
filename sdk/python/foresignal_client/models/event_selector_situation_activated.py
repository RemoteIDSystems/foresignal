from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

T = TypeVar("T", bound="EventSelectorSituationActivated")


@_attrs_define
class EventSelectorSituationActivated:
    """Situations turning active (118), optionally by theme

    Attributes:
        theme (None | SituationTheme | Unset):
    """

    theme: None | SituationTheme | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        theme: None | str | Unset
        if isinstance(self.theme, Unset):
            theme = UNSET
        elif isinstance(self.theme, SituationTheme):
            theme = self.theme.value
        else:
            theme = self.theme

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if theme is not UNSET:
            field_dict["theme"] = theme

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_theme(data: object) -> None | SituationTheme | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                theme_type_0 = SituationTheme(data)

                return theme_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationTheme | Unset, data)

        theme = _parse_theme(d.pop("theme", UNSET))

        event_selector_situation_activated = cls(
            theme=theme,
        )

        event_selector_situation_activated.additional_properties = d
        return event_selector_situation_activated

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
