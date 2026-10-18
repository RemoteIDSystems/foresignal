from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.recipe_backtest_result_events_item_evidence_quality import (
    RecipeBacktestResultEventsItemEvidenceQuality,
)
from ..models.recipe_backtest_result_events_item_type import (
    RecipeBacktestResultEventsItemType,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_event_member import IntelEventMember


T = TypeVar("T", bound="RecipeBacktestResultEventsItem")


@_attrs_define
class RecipeBacktestResultEventsItem:
    """A would-be event

    Attributes:
        type_ (RecipeBacktestResultEventsItemType):
        start_time (datetime.datetime):
        members (list[IntelEventMember]):
        end_time (datetime.datetime | None | Unset):
        confidence (float | Unset):
        evidence_quality (RecipeBacktestResultEventsItemEvidenceQuality | Unset):
    """

    type_: RecipeBacktestResultEventsItemType
    start_time: datetime.datetime
    members: list[IntelEventMember]
    end_time: datetime.datetime | None | Unset = UNSET
    confidence: float | Unset = UNSET
    evidence_quality: RecipeBacktestResultEventsItemEvidenceQuality | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        start_time = self.start_time.isoformat()

        members = []
        for members_item_data in self.members:
            members_item = members_item_data.to_dict()
            members.append(members_item)

        end_time: None | str | Unset
        if isinstance(self.end_time, Unset):
            end_time = UNSET
        elif isinstance(self.end_time, datetime.datetime):
            end_time = self.end_time.isoformat()
        else:
            end_time = self.end_time

        confidence = self.confidence

        evidence_quality: str | Unset = UNSET
        if not isinstance(self.evidence_quality, Unset):
            evidence_quality = self.evidence_quality.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "startTime": start_time,
                "members": members,
            }
        )
        if end_time is not UNSET:
            field_dict["endTime"] = end_time
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence_quality is not UNSET:
            field_dict["evidenceQuality"] = evidence_quality

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_event_member import IntelEventMember

        d = dict(src_dict)
        type_ = RecipeBacktestResultEventsItemType(d.pop("type"))

        start_time = datetime.datetime.fromisoformat(d.pop("startTime"))

        members = []
        _members = d.pop("members")
        for members_item_data in _members:
            members_item = IntelEventMember.from_dict(members_item_data)

            members.append(members_item)

        def _parse_end_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                end_time_type_0 = datetime.datetime.fromisoformat(data)

                return end_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        end_time = _parse_end_time(d.pop("endTime", UNSET))

        confidence = d.pop("confidence", UNSET)

        _evidence_quality = d.pop("evidenceQuality", UNSET)
        evidence_quality: RecipeBacktestResultEventsItemEvidenceQuality | Unset
        if isinstance(_evidence_quality, Unset):
            evidence_quality = UNSET
        else:
            evidence_quality = RecipeBacktestResultEventsItemEvidenceQuality(
                _evidence_quality
            )

        recipe_backtest_result_events_item = cls(
            type_=type_,
            start_time=start_time,
            members=members,
            end_time=end_time,
            confidence=confidence,
            evidence_quality=evidence_quality,
        )

        recipe_backtest_result_events_item.additional_properties = d
        return recipe_backtest_result_events_item

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
