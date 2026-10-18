from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.social_versus_observed_lead_times_item import (
        SocialVersusObservedLeadTimesItem,
    )
    from ..models.social_versus_observed_narratives_by_status import (
        SocialVersusObservedNarrativesByStatus,
    )
    from ..models.social_versus_observed_narratives_item import (
        SocialVersusObservedNarrativesItem,
    )


T = TypeVar("T", bound="SocialVersusObserved")


@_attrs_define
class SocialVersusObserved:
    """Packet and assessment section "What people are saying versus what we observe" (137; ICD 203 wording rules apply)

    Attributes:
        narratives_by_status (SocialVersusObservedNarrativesByStatus): Status → narrative count
        narratives (list[SocialVersusObservedNarrativesItem]):
        attention_gap_finding_ids (list[str] | Unset):
        lead_times (list[SocialVersusObservedLeadTimesItem] | Unset): Out-of-sample statistics only
        media_reuse_finding_ids (list[str] | Unset):
        coordination_finding_ids (list[str] | Unset):
        caveats (list[Caveat] | Unset):
    """

    narratives_by_status: SocialVersusObservedNarrativesByStatus
    narratives: list[SocialVersusObservedNarrativesItem]
    attention_gap_finding_ids: list[str] | Unset = UNSET
    lead_times: list[SocialVersusObservedLeadTimesItem] | Unset = UNSET
    media_reuse_finding_ids: list[str] | Unset = UNSET
    coordination_finding_ids: list[str] | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        narratives_by_status = self.narratives_by_status.to_dict()

        narratives = []
        for narratives_item_data in self.narratives:
            narratives_item = narratives_item_data.to_dict()
            narratives.append(narratives_item)

        attention_gap_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.attention_gap_finding_ids, Unset):
            attention_gap_finding_ids = self.attention_gap_finding_ids

        lead_times: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.lead_times, Unset):
            lead_times = []
            for lead_times_item_data in self.lead_times:
                lead_times_item = lead_times_item_data.to_dict()
                lead_times.append(lead_times_item)

        media_reuse_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.media_reuse_finding_ids, Unset):
            media_reuse_finding_ids = self.media_reuse_finding_ids

        coordination_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.coordination_finding_ids, Unset):
            coordination_finding_ids = self.coordination_finding_ids

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narrativesByStatus": narratives_by_status,
                "narratives": narratives,
            }
        )
        if attention_gap_finding_ids is not UNSET:
            field_dict["attentionGapFindingIds"] = attention_gap_finding_ids
        if lead_times is not UNSET:
            field_dict["leadTimes"] = lead_times
        if media_reuse_finding_ids is not UNSET:
            field_dict["mediaReuseFindingIds"] = media_reuse_finding_ids
        if coordination_finding_ids is not UNSET:
            field_dict["coordinationFindingIds"] = coordination_finding_ids
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.social_versus_observed_lead_times_item import (
            SocialVersusObservedLeadTimesItem,
        )
        from ..models.social_versus_observed_narratives_by_status import (
            SocialVersusObservedNarrativesByStatus,
        )
        from ..models.social_versus_observed_narratives_item import (
            SocialVersusObservedNarrativesItem,
        )

        d = dict(src_dict)
        narratives_by_status = SocialVersusObservedNarrativesByStatus.from_dict(
            d.pop("narrativesByStatus")
        )

        narratives = []
        _narratives = d.pop("narratives")
        for narratives_item_data in _narratives:
            narratives_item = SocialVersusObservedNarrativesItem.from_dict(
                narratives_item_data
            )

            narratives.append(narratives_item)

        attention_gap_finding_ids = cast(
            list[str], d.pop("attentionGapFindingIds", UNSET)
        )

        _lead_times = d.pop("leadTimes", UNSET)
        lead_times: list[SocialVersusObservedLeadTimesItem] | Unset = UNSET
        if _lead_times is not UNSET:
            lead_times = []
            for lead_times_item_data in _lead_times:
                lead_times_item = SocialVersusObservedLeadTimesItem.from_dict(
                    lead_times_item_data
                )

                lead_times.append(lead_times_item)

        media_reuse_finding_ids = cast(list[str], d.pop("mediaReuseFindingIds", UNSET))

        coordination_finding_ids = cast(
            list[str], d.pop("coordinationFindingIds", UNSET)
        )

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        social_versus_observed = cls(
            narratives_by_status=narratives_by_status,
            narratives=narratives,
            attention_gap_finding_ids=attention_gap_finding_ids,
            lead_times=lead_times,
            media_reuse_finding_ids=media_reuse_finding_ids,
            coordination_finding_ids=coordination_finding_ids,
            caveats=caveats,
        )

        social_versus_observed.additional_properties = d
        return social_versus_observed

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
