from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_changes_new_members_by_family import (
        SituationChangesNewMembersByFamily,
    )
    from ..models.situation_changes_new_members_by_type import (
        SituationChangesNewMembersByType,
    )


T = TypeVar("T", bound="SituationChangesNewMembers")


@_attrs_define
class SituationChangesNewMembers:
    """Members added since the baseline

    Attributes:
        total (int):
        by_family (SituationChangesNewMembersByFamily): Count per signal family
        by_type (SituationChangesNewMembersByType | Unset): Count per member type
    """

    total: int
    by_family: SituationChangesNewMembersByFamily
    by_type: SituationChangesNewMembersByType | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        total = self.total

        by_family = self.by_family.to_dict()

        by_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_type, Unset):
            by_type = self.by_type.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "total": total,
                "byFamily": by_family,
            }
        )
        if by_type is not UNSET:
            field_dict["byType"] = by_type

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_changes_new_members_by_family import (
            SituationChangesNewMembersByFamily,
        )
        from ..models.situation_changes_new_members_by_type import (
            SituationChangesNewMembersByType,
        )

        d = dict(src_dict)
        total = d.pop("total")

        by_family = SituationChangesNewMembersByFamily.from_dict(d.pop("byFamily"))

        _by_type = d.pop("byType", UNSET)
        by_type: SituationChangesNewMembersByType | Unset
        if isinstance(_by_type, Unset):
            by_type = UNSET
        else:
            by_type = SituationChangesNewMembersByType.from_dict(_by_type)

        situation_changes_new_members = cls(
            total=total,
            by_family=by_family,
            by_type=by_type,
        )

        situation_changes_new_members.additional_properties = d
        return situation_changes_new_members

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
