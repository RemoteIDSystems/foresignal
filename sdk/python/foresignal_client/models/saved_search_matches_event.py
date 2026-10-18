from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SavedSearchMatchesEvent")


@_attrs_define
class SavedSearchMatchesEvent:
    """SSE `saved-search.matches` on the `me` topic (at most hourly per saved search)

    Attributes:
        saved_search_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        new_matches (int):
        at (datetime.datetime):
        refs (list[str] | Unset):
    """

    saved_search_id: str
    name: str
    new_matches: int
    at: datetime.datetime
    refs: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        saved_search_id = self.saved_search_id

        name = self.name

        new_matches = self.new_matches

        at = self.at.isoformat()

        refs: list[str] | Unset = UNSET
        if not isinstance(self.refs, Unset):
            refs = self.refs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "savedSearchId": saved_search_id,
                "name": name,
                "newMatches": new_matches,
                "at": at,
            }
        )
        if refs is not UNSET:
            field_dict["refs"] = refs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        saved_search_id = d.pop("savedSearchId")

        name = d.pop("name")

        new_matches = d.pop("newMatches")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        refs = cast(list[str], d.pop("refs", UNSET))

        saved_search_matches_event = cls(
            saved_search_id=saved_search_id,
            name=name,
            new_matches=new_matches,
            at=at,
            refs=refs,
        )

        saved_search_matches_event.additional_properties = d
        return saved_search_matches_event

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
