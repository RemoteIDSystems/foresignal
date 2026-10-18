from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DoctorReportFixtureType0")


@_attrs_define
class DoctorReportFixtureType0:
    """Fixture compared against

    Attributes:
        file (str):
        captured_at (None | str | Unset):
        sha256 (str | Unset):
        truncated (bool | Unset): True when the packaged fixture was cut to the 256 KB cap
    """

    file: str
    captured_at: None | str | Unset = UNSET
    sha256: str | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        file = self.file

        captured_at: None | str | Unset
        if isinstance(self.captured_at, Unset):
            captured_at = UNSET
        else:
            captured_at = self.captured_at

        sha256 = self.sha256

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "file": file,
            }
        )
        if captured_at is not UNSET:
            field_dict["capturedAt"] = captured_at
        if sha256 is not UNSET:
            field_dict["sha256"] = sha256
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        file = d.pop("file")

        def _parse_captured_at(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        captured_at = _parse_captured_at(d.pop("capturedAt", UNSET))

        sha256 = d.pop("sha256", UNSET)

        truncated = d.pop("truncated", UNSET)

        doctor_report_fixture_type_0 = cls(
            file=file,
            captured_at=captured_at,
            sha256=sha256,
            truncated=truncated,
        )

        doctor_report_fixture_type_0.additional_properties = d
        return doctor_report_fixture_type_0

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
