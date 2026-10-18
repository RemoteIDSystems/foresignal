from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_evidence_member_counts_by_family import (
        SituationEvidenceMemberCountsByFamily,
    )
    from ..models.situation_evidence_member_counts_by_type import (
        SituationEvidenceMemberCountsByType,
    )


T = TypeVar("T", bound="SituationEvidenceMemberCounts")


@_attrs_define
class SituationEvidenceMemberCounts:
    """Member counts

    Attributes:
        by_type (SituationEvidenceMemberCountsByType | Unset): Count per member type
        by_family (SituationEvidenceMemberCountsByFamily | Unset): Count per signal family
    """

    by_type: SituationEvidenceMemberCountsByType | Unset = UNSET
    by_family: SituationEvidenceMemberCountsByFamily | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        by_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_type, Unset):
            by_type = self.by_type.to_dict()

        by_family: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_family, Unset):
            by_family = self.by_family.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if by_type is not UNSET:
            field_dict["byType"] = by_type
        if by_family is not UNSET:
            field_dict["byFamily"] = by_family

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_evidence_member_counts_by_family import (
            SituationEvidenceMemberCountsByFamily,
        )
        from ..models.situation_evidence_member_counts_by_type import (
            SituationEvidenceMemberCountsByType,
        )

        d = dict(src_dict)
        _by_type = d.pop("byType", UNSET)
        by_type: SituationEvidenceMemberCountsByType | Unset
        if isinstance(_by_type, Unset):
            by_type = UNSET
        else:
            by_type = SituationEvidenceMemberCountsByType.from_dict(_by_type)

        _by_family = d.pop("byFamily", UNSET)
        by_family: SituationEvidenceMemberCountsByFamily | Unset
        if isinstance(_by_family, Unset):
            by_family = UNSET
        else:
            by_family = SituationEvidenceMemberCountsByFamily.from_dict(_by_family)

        situation_evidence_member_counts = cls(
            by_type=by_type,
            by_family=by_family,
        )

        situation_evidence_member_counts.additional_properties = d
        return situation_evidence_member_counts

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
