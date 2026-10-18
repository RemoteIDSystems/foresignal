from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.impact_projection_event_hazard_type import ImpactProjectionEventHazardType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ImpactProjectionEvent")


@_attrs_define
class ImpactProjectionEvent:
    """SSE payload for `impact.projected|superseded` (topic `analytics`)

    Attributes:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        hazard_type (ImpactProjectionEventHazardType):
        impact_family (str):
        admin1 (str):
        p_impact (float):
        at (datetime.datetime):
        source_ref (str | Unset):
        question_id (None | str | Unset):
        superseded_by (None | str | Unset):
    """

    finding_id: str
    hazard_type: ImpactProjectionEventHazardType
    impact_family: str
    admin1: str
    p_impact: float
    at: datetime.datetime
    source_ref: str | Unset = UNSET
    question_id: None | str | Unset = UNSET
    superseded_by: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        finding_id = self.finding_id

        hazard_type = self.hazard_type.value

        impact_family = self.impact_family

        admin1 = self.admin1

        p_impact = self.p_impact

        at = self.at.isoformat()

        source_ref = self.source_ref

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        superseded_by: None | str | Unset
        if isinstance(self.superseded_by, Unset):
            superseded_by = UNSET
        else:
            superseded_by = self.superseded_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "findingId": finding_id,
                "hazardType": hazard_type,
                "impactFamily": impact_family,
                "admin1": admin1,
                "pImpact": p_impact,
                "at": at,
            }
        )
        if source_ref is not UNSET:
            field_dict["sourceRef"] = source_ref
        if question_id is not UNSET:
            field_dict["questionId"] = question_id
        if superseded_by is not UNSET:
            field_dict["supersededBy"] = superseded_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        finding_id = d.pop("findingId")

        hazard_type = ImpactProjectionEventHazardType(d.pop("hazardType"))

        impact_family = d.pop("impactFamily")

        admin1 = d.pop("admin1")

        p_impact = d.pop("pImpact")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        source_ref = d.pop("sourceRef", UNSET)

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        def _parse_superseded_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        superseded_by = _parse_superseded_by(d.pop("supersededBy", UNSET))

        impact_projection_event = cls(
            finding_id=finding_id,
            hazard_type=hazard_type,
            impact_family=impact_family,
            admin1=admin1,
            p_impact=p_impact,
            at=at,
            source_ref=source_ref,
            question_id=question_id,
            superseded_by=superseded_by,
        )

        impact_projection_event.additional_properties = d
        return impact_projection_event

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
