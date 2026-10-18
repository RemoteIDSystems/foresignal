from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_board_latest_assessments_item_cadence import (
    IntelBoardLatestAssessmentsItemCadence,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelBoardLatestAssessmentsItem")


@_attrs_define
class IntelBoardLatestAssessmentsItem:
    """Latest assessment of a cadence

    Attributes:
        cadence (IntelBoardLatestAssessmentsItemCadence):
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        created_at (datetime.datetime):
        headline (None | str | Unset):
    """

    cadence: IntelBoardLatestAssessmentsItemCadence
    assessment_id: str
    created_at: datetime.datetime
    headline: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cadence = self.cadence.value

        assessment_id = self.assessment_id

        created_at = self.created_at.isoformat()

        headline: None | str | Unset
        if isinstance(self.headline, Unset):
            headline = UNSET
        else:
            headline = self.headline

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "cadence": cadence,
                "assessmentId": assessment_id,
                "createdAt": created_at,
            }
        )
        if headline is not UNSET:
            field_dict["headline"] = headline

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        cadence = IntelBoardLatestAssessmentsItemCadence(d.pop("cadence"))

        assessment_id = d.pop("assessmentId")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_headline(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        headline = _parse_headline(d.pop("headline", UNSET))

        intel_board_latest_assessments_item = cls(
            cadence=cadence,
            assessment_id=assessment_id,
            created_at=created_at,
            headline=headline,
        )

        intel_board_latest_assessments_item.additional_properties = d
        return intel_board_latest_assessments_item

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
