from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_versus_observed_narratives_item_status import (
    SocialVersusObservedNarrativesItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialVersusObservedNarrativesItem")


@_attrs_define
class SocialVersusObservedNarrativesItem:
    """Notable narrative

    Attributes:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        status (SocialVersusObservedNarrativesItemStatus):
        situation_id (None | str | Unset):
    """

    narrative_id: str
    title: str
    status: SocialVersusObservedNarrativesItemStatus
    situation_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        narrative_id = self.narrative_id

        title = self.title

        status = self.status.value

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narrativeId": narrative_id,
                "title": title,
                "status": status,
            }
        )
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        narrative_id = d.pop("narrativeId")

        title = d.pop("title")

        status = SocialVersusObservedNarrativesItemStatus(d.pop("status"))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        social_versus_observed_narratives_item = cls(
            narrative_id=narrative_id,
            title=title,
            status=status,
            situation_id=situation_id,
        )

        social_versus_observed_narratives_item.additional_properties = d
        return social_versus_observed_narratives_item

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
