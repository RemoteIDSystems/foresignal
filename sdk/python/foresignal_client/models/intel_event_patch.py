from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.intel_event_patch_status import IntelEventPatchStatus
from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelEventPatch")


@_attrs_define
class IntelEventPatch:
    """Changes to an intelligence event; severity overrides are logged

    Attributes:
        title (str | Unset):
        status (IntelEventPatchStatus | Unset):
        severity (None | Severity | Unset): Override; null clears the override
        notes (None | str | Unset):
    """

    title: str | Unset = UNSET
    status: IntelEventPatchStatus | Unset = UNSET
    severity: None | Severity | Unset = UNSET
    notes: None | str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        elif isinstance(self.severity, Severity):
            severity = self.severity.value
        else:
            severity = self.severity

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if status is not UNSET:
            field_dict["status"] = status
        if severity is not UNSET:
            field_dict["severity"] = severity
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        title = d.pop("title", UNSET)

        _status = d.pop("status", UNSET)
        status: IntelEventPatchStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = IntelEventPatchStatus(_status)

        def _parse_severity(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                severity_type_0 = Severity(data)

                return severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        intel_event_patch = cls(
            title=title,
            status=status,
            severity=severity,
            notes=notes,
        )

        return intel_event_patch
