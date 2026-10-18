from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="DoctorRunAccepted")


@_attrs_define
class DoctorRunAccepted:
    """A doctor run still in progress after 90 s; poll the Location

    Attributes:
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (Literal['running']):
        location (str):
    """

    report_id: str
    status: Literal["running"]
    location: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        report_id = self.report_id

        status = self.status

        location = self.location

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reportId": report_id,
                "status": status,
                "location": location,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        report_id = d.pop("reportId")

        status = cast(Literal["running"], d.pop("status"))
        if status != "running":
            raise ValueError(f"status must match const 'running', got '{status}'")

        location = d.pop("location")

        doctor_run_accepted = cls(
            report_id=report_id,
            status=status,
            location=location,
        )

        doctor_run_accepted.additional_properties = d
        return doctor_run_accepted

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
