from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.situation_evidence_corroboration import SituationEvidenceCorroboration
    from ..models.situation_evidence_families_item import SituationEvidenceFamiliesItem
    from ..models.situation_evidence_member_counts import SituationEvidenceMemberCounts


T = TypeVar("T", bound="SituationEvidence")


@_attrs_define
class SituationEvidence:
    """Evidence: members grouped by signal family, with the corroboration roll-up

    Attributes:
        member_counts (SituationEvidenceMemberCounts): Member counts
        families (list[SituationEvidenceFamiliesItem]):
        corroboration (SituationEvidenceCorroboration): 116 corroboration roll-up; echoed reports never count twice
    """

    member_counts: SituationEvidenceMemberCounts
    families: list[SituationEvidenceFamiliesItem]
    corroboration: SituationEvidenceCorroboration
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        member_counts = self.member_counts.to_dict()

        families = []
        for families_item_data in self.families:
            families_item = families_item_data.to_dict()
            families.append(families_item)

        corroboration = self.corroboration.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "memberCounts": member_counts,
                "families": families,
                "corroboration": corroboration,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_evidence_corroboration import (
            SituationEvidenceCorroboration,
        )
        from ..models.situation_evidence_families_item import (
            SituationEvidenceFamiliesItem,
        )
        from ..models.situation_evidence_member_counts import (
            SituationEvidenceMemberCounts,
        )

        d = dict(src_dict)
        member_counts = SituationEvidenceMemberCounts.from_dict(d.pop("memberCounts"))

        families = []
        _families = d.pop("families")
        for families_item_data in _families:
            families_item = SituationEvidenceFamiliesItem.from_dict(families_item_data)

            families.append(families_item)

        corroboration = SituationEvidenceCorroboration.from_dict(d.pop("corroboration"))

        situation_evidence = cls(
            member_counts=member_counts,
            families=families,
            corroboration=corroboration,
        )

        situation_evidence.additional_properties = d
        return situation_evidence

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
