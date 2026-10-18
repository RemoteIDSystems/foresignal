from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_social_section_attention_gap_type_0 import (
        SituationSocialSectionAttentionGapType0,
    )
    from ..models.situation_social_section_narratives_item import (
        SituationSocialSectionNarrativesItem,
    )


T = TypeVar("T", bound="SituationSocialSection")


@_attrs_define
class SituationSocialSection:
    """What people are saying about a situation, kept apart from physical corroboration (137)

    Attributes:
        narratives (list[SituationSocialSectionNarrativesItem]):
        social_evidence_groups (int): Narrative evidence groups, counted separately from physical corroboration
        social_coverage_ok (bool): 135 social observability for the situation's regions
        attention_gap (None | SituationSocialSectionAttentionGapType0 | Unset):
        updated_at (datetime.datetime | Unset):
    """

    narratives: list[SituationSocialSectionNarrativesItem]
    social_evidence_groups: int
    social_coverage_ok: bool
    attention_gap: None | SituationSocialSectionAttentionGapType0 | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.situation_social_section_attention_gap_type_0 import (
            SituationSocialSectionAttentionGapType0,
        )

        narratives = []
        for narratives_item_data in self.narratives:
            narratives_item = narratives_item_data.to_dict()
            narratives.append(narratives_item)

        social_evidence_groups = self.social_evidence_groups

        social_coverage_ok = self.social_coverage_ok

        attention_gap: dict[str, Any] | None | Unset
        if isinstance(self.attention_gap, Unset):
            attention_gap = UNSET
        elif isinstance(self.attention_gap, SituationSocialSectionAttentionGapType0):
            attention_gap = self.attention_gap.to_dict()
        else:
            attention_gap = self.attention_gap

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narratives": narratives,
                "socialEvidenceGroups": social_evidence_groups,
                "socialCoverageOk": social_coverage_ok,
            }
        )
        if attention_gap is not UNSET:
            field_dict["attentionGap"] = attention_gap
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_social_section_attention_gap_type_0 import (
            SituationSocialSectionAttentionGapType0,
        )
        from ..models.situation_social_section_narratives_item import (
            SituationSocialSectionNarrativesItem,
        )

        d = dict(src_dict)
        narratives = []
        _narratives = d.pop("narratives")
        for narratives_item_data in _narratives:
            narratives_item = SituationSocialSectionNarrativesItem.from_dict(
                narratives_item_data
            )

            narratives.append(narratives_item)

        social_evidence_groups = d.pop("socialEvidenceGroups")

        social_coverage_ok = d.pop("socialCoverageOk")

        def _parse_attention_gap(
            data: object,
        ) -> None | SituationSocialSectionAttentionGapType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                attention_gap_type_0 = (
                    SituationSocialSectionAttentionGapType0.from_dict(data)
                )

                return attention_gap_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationSocialSectionAttentionGapType0 | Unset, data)

        attention_gap = _parse_attention_gap(d.pop("attentionGap", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        situation_social_section = cls(
            narratives=narratives,
            social_evidence_groups=social_evidence_groups,
            social_coverage_ok=social_coverage_ok,
            attention_gap=attention_gap,
            updated_at=updated_at,
        )

        situation_social_section.additional_properties = d
        return situation_social_section

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
