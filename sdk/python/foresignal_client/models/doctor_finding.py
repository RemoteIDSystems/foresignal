from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_finding_code import DoctorFindingCode
from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="DoctorFinding")


@_attrs_define
class DoctorFinding:
    """A deterministic diagnosis finding

    Attributes:
        code (DoctorFindingCode):
        severity (Severity):
        evidence (list[str]): Facts behind the finding; heuristic evidence (parser path scan) is marked as such
        suggested_action (str):
        path (None | str | Unset): Structure path concerned, e.g. `features[].properties.mag`
        heuristic (bool | Unset):
    """

    code: DoctorFindingCode
    severity: Severity
    evidence: list[str]
    suggested_action: str
    path: None | str | Unset = UNSET
    heuristic: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        code = self.code.value

        severity = self.severity.value

        evidence = self.evidence

        suggested_action = self.suggested_action

        path: None | str | Unset
        if isinstance(self.path, Unset):
            path = UNSET
        else:
            path = self.path

        heuristic = self.heuristic

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "code": code,
                "severity": severity,
                "evidence": evidence,
                "suggestedAction": suggested_action,
            }
        )
        if path is not UNSET:
            field_dict["path"] = path
        if heuristic is not UNSET:
            field_dict["heuristic"] = heuristic

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        code = DoctorFindingCode(d.pop("code"))

        severity = Severity(d.pop("severity"))

        evidence = cast(list[str], d.pop("evidence"))

        suggested_action = d.pop("suggestedAction")

        def _parse_path(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        path = _parse_path(d.pop("path", UNSET))

        heuristic = d.pop("heuristic", UNSET)

        doctor_finding = cls(
            code=code,
            severity=severity,
            evidence=evidence,
            suggested_action=suggested_action,
            path=path,
            heuristic=heuristic,
        )

        doctor_finding.additional_properties = d
        return doctor_finding

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
