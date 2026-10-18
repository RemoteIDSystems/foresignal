from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.analyst_evaluation_profiles_item import AnalystEvaluationProfilesItem


T = TypeVar("T", bound="AnalystEvaluation")


@_attrs_define
class AnalystEvaluation:
    """Per-profile evaluation aggregates since a time

    Attributes:
        since (datetime.datetime):
        profiles (list[AnalystEvaluationProfilesItem]):
    """

    since: datetime.datetime
    profiles: list[AnalystEvaluationProfilesItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        since = self.since.isoformat()

        profiles = []
        for profiles_item_data in self.profiles:
            profiles_item = profiles_item_data.to_dict()
            profiles.append(profiles_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "since": since,
                "profiles": profiles,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_evaluation_profiles_item import (
            AnalystEvaluationProfilesItem,
        )

        d = dict(src_dict)
        since = datetime.datetime.fromisoformat(d.pop("since"))

        profiles = []
        _profiles = d.pop("profiles")
        for profiles_item_data in _profiles:
            profiles_item = AnalystEvaluationProfilesItem.from_dict(profiles_item_data)

            profiles.append(profiles_item)

        analyst_evaluation = cls(
            since=since,
            profiles=profiles,
        )

        analyst_evaluation.additional_properties = d
        return analyst_evaluation

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
