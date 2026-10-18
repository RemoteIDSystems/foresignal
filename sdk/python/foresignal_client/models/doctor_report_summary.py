from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_report_summary_status import DoctorReportSummaryStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.doctor_report_summary_top_finding_type_0 import (
        DoctorReportSummaryTopFindingType0,
    )


T = TypeVar("T", bound="DoctorReportSummary")


@_attrs_define
class DoctorReportSummary:
    """Latest doctor report summary on source health

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (DoctorReportSummaryStatus):
        created_at (datetime.datetime):
        top_finding (DoctorReportSummaryTopFindingType0 | None | Unset):
    """

    id: str
    status: DoctorReportSummaryStatus
    created_at: datetime.datetime
    top_finding: DoctorReportSummaryTopFindingType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.doctor_report_summary_top_finding_type_0 import (
            DoctorReportSummaryTopFindingType0,
        )

        id = self.id

        status = self.status.value

        created_at = self.created_at.isoformat()

        top_finding: dict[str, Any] | None | Unset
        if isinstance(self.top_finding, Unset):
            top_finding = UNSET
        elif isinstance(self.top_finding, DoctorReportSummaryTopFindingType0):
            top_finding = self.top_finding.to_dict()
        else:
            top_finding = self.top_finding

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "status": status,
                "createdAt": created_at,
            }
        )
        if top_finding is not UNSET:
            field_dict["topFinding"] = top_finding

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.doctor_report_summary_top_finding_type_0 import (
            DoctorReportSummaryTopFindingType0,
        )

        d = dict(src_dict)
        id = d.pop("id")

        status = DoctorReportSummaryStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_top_finding(
            data: object,
        ) -> DoctorReportSummaryTopFindingType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                top_finding_type_0 = DoctorReportSummaryTopFindingType0.from_dict(data)

                return top_finding_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DoctorReportSummaryTopFindingType0 | None | Unset, data)

        top_finding = _parse_top_finding(d.pop("topFinding", UNSET))

        doctor_report_summary = cls(
            id=id,
            status=status,
            created_at=created_at,
            top_finding=top_finding,
        )

        doctor_report_summary.additional_properties = d
        return doctor_report_summary

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
