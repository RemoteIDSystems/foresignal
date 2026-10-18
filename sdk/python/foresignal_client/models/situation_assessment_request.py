from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_assessment_request_effort import (
    SituationAssessmentRequestEffort,
)
from ..models.situation_assessment_request_roles_item import (
    SituationAssessmentRequestRolesItem,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationAssessmentRequest")


@_attrs_define
class SituationAssessmentRequest:
    """Options for an on-demand situation assessment

    Attributes:
        roles (list[SituationAssessmentRequestRolesItem] | Unset): Default analyst and red-team
        effort (SituationAssessmentRequestEffort | Unset): LLM effort
    """

    roles: list[SituationAssessmentRequestRolesItem] | Unset = UNSET
    effort: SituationAssessmentRequestEffort | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        roles: list[str] | Unset = UNSET
        if not isinstance(self.roles, Unset):
            roles = []
            for roles_item_data in self.roles:
                roles_item = roles_item_data.value
                roles.append(roles_item)

        effort: str | Unset = UNSET
        if not isinstance(self.effort, Unset):
            effort = self.effort.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if roles is not UNSET:
            field_dict["roles"] = roles
        if effort is not UNSET:
            field_dict["effort"] = effort

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _roles = d.pop("roles", UNSET)
        roles: list[SituationAssessmentRequestRolesItem] | Unset = UNSET
        if _roles is not UNSET:
            roles = []
            for roles_item_data in _roles:
                roles_item = SituationAssessmentRequestRolesItem(roles_item_data)

                roles.append(roles_item)

        _effort = d.pop("effort", UNSET)
        effort: SituationAssessmentRequestEffort | Unset
        if isinstance(_effort, Unset):
            effort = UNSET
        else:
            effort = SituationAssessmentRequestEffort(_effort)

        situation_assessment_request = cls(
            roles=roles,
            effort=effort,
        )

        situation_assessment_request.additional_properties = d
        return situation_assessment_request

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
