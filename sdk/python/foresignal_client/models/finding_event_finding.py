from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.finding_event_finding_finding_type import FindingEventFindingFindingType
from ..models.finding_event_finding_status import FindingEventFindingStatus
from ..models.finding_severity import FindingSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="FindingEventFinding")


@_attrs_define
class FindingEventFinding:
    """Finding summary

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        finding_type (FindingEventFindingFindingType):
        title (str):
        severity (FindingSeverity):
        status (FindingEventFindingStatus):
        detected_at (datetime.datetime):
        region_codes (list[str] | Unset):
        situation_id (None | str | Unset):
        private (bool | Unset):
    """

    id: str
    finding_type: FindingEventFindingFindingType
    title: str
    severity: FindingSeverity
    status: FindingEventFindingStatus
    detected_at: datetime.datetime
    region_codes: list[str] | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        finding_type = self.finding_type.value

        title = self.title

        severity = self.severity.value

        status = self.status.value

        detected_at = self.detected_at.isoformat()

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "findingType": finding_type,
                "title": title,
                "severity": severity,
                "status": status,
                "detectedAt": detected_at,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        finding_type = FindingEventFindingFindingType(d.pop("findingType"))

        title = d.pop("title")

        severity = FindingSeverity(d.pop("severity"))

        status = FindingEventFindingStatus(d.pop("status"))

        detected_at = datetime.datetime.fromisoformat(d.pop("detectedAt"))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        private = d.pop("private", UNSET)

        finding_event_finding = cls(
            id=id,
            finding_type=finding_type,
            title=title,
            severity=severity,
            status=status,
            detected_at=detected_at,
            region_codes=region_codes,
            situation_id=situation_id,
            private=private,
        )

        finding_event_finding.additional_properties = d
        return finding_event_finding

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
