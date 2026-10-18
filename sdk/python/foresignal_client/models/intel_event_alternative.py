from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelEventAlternative")


@_attrs_define
class IntelEventAlternative:
    """An alternative explanation (speculation), e.g. a receiver coverage gap or weather

    Attributes:
        hypothesis (str):  Example: AIS receiver coverage gap.
        basis (str):
        supporting_member_ids (list[str] | Unset):
    """

    hypothesis: str
    basis: str
    supporting_member_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hypothesis = self.hypothesis

        basis = self.basis

        supporting_member_ids: list[str] | Unset = UNSET
        if not isinstance(self.supporting_member_ids, Unset):
            supporting_member_ids = self.supporting_member_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hypothesis": hypothesis,
                "basis": basis,
            }
        )
        if supporting_member_ids is not UNSET:
            field_dict["supportingMemberIds"] = supporting_member_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hypothesis = d.pop("hypothesis")

        basis = d.pop("basis")

        supporting_member_ids = cast(list[str], d.pop("supportingMemberIds", UNSET))

        intel_event_alternative = cls(
            hypothesis=hypothesis,
            basis=basis,
            supporting_member_ids=supporting_member_ids,
        )

        intel_event_alternative.additional_properties = d
        return intel_event_alternative

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
