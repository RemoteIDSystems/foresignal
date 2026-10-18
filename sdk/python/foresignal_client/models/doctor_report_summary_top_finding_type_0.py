from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_report_summary_top_finding_type_0_code import (
    DoctorReportSummaryTopFindingType0Code,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="DoctorReportSummaryTopFindingType0")


@_attrs_define
class DoctorReportSummaryTopFindingType0:
    """Top finding

    Attributes:
        code (DoctorReportSummaryTopFindingType0Code):
        suggested_action (str | Unset):
    """

    code: DoctorReportSummaryTopFindingType0Code
    suggested_action: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        code = self.code.value

        suggested_action = self.suggested_action

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "code": code,
            }
        )
        if suggested_action is not UNSET:
            field_dict["suggestedAction"] = suggested_action

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        code = DoctorReportSummaryTopFindingType0Code(d.pop("code"))

        suggested_action = d.pop("suggestedAction", UNSET)

        doctor_report_summary_top_finding_type_0 = cls(
            code=code,
            suggested_action=suggested_action,
        )

        doctor_report_summary_top_finding_type_0.additional_properties = d
        return doctor_report_summary_top_finding_type_0

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
