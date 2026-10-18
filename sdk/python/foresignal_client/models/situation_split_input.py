from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationSplitInput")


@_attrs_define
class SituationSplitInput:
    """Move a chosen member set into a new situation

    Attributes:
        member_ids (list[str]):
        title (str):
        theme (SituationTheme | Unset): Situation theme (118 `ThemeTable`)
        version (int | Unset): Expected version of the source situation
    """

    member_ids: list[str]
    title: str
    theme: SituationTheme | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        member_ids = self.member_ids

        title = self.title

        theme: str | Unset = UNSET
        if not isinstance(self.theme, Unset):
            theme = self.theme.value

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "memberIds": member_ids,
                "title": title,
            }
        )
        if theme is not UNSET:
            field_dict["theme"] = theme
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        member_ids = cast(list[str], d.pop("memberIds"))

        title = d.pop("title")

        _theme = d.pop("theme", UNSET)
        theme: SituationTheme | Unset
        if isinstance(_theme, Unset):
            theme = UNSET
        else:
            theme = SituationTheme(_theme)

        version = d.pop("version", UNSET)

        situation_split_input = cls(
            member_ids=member_ids,
            title=title,
            theme=theme,
            version=version,
        )

        situation_split_input.additional_properties = d
        return situation_split_input

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
