from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.watchlist_item_origin import WatchlistItemOrigin
from ..types import UNSET, Unset

T = TypeVar("T", bound="WatchlistItem")


@_attrs_define
class WatchlistItem:
    """One watchlist item (036)

    Attributes:
        value (str): Normalised
        added_at (datetime.datetime):
        origin (WatchlistItemOrigin):
        label (None | str | Unset):
        notes (None | str | Unset):
        tags (list[str] | Unset):
    """

    value: str
    added_at: datetime.datetime
    origin: WatchlistItemOrigin
    label: None | str | Unset = UNSET
    notes: None | str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        value = self.value

        added_at = self.added_at.isoformat()

        origin = self.origin.value

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "value": value,
                "addedAt": added_at,
                "origin": origin,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label
        if notes is not UNSET:
            field_dict["notes"] = notes
        if tags is not UNSET:
            field_dict["tags"] = tags

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        value = d.pop("value")

        added_at = datetime.datetime.fromisoformat(d.pop("addedAt"))

        origin = WatchlistItemOrigin(d.pop("origin"))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        watchlist_item = cls(
            value=value,
            added_at=added_at,
            origin=origin,
            label=label,
            notes=notes,
            tags=tags,
        )

        watchlist_item.additional_properties = d
        return watchlist_item

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
