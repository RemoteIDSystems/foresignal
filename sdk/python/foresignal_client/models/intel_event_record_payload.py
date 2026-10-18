from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_event_record_payload_evidence_quality import (
    IntelEventRecordPayloadEvidenceQuality,
)
from ..models.intel_event_record_payload_label import IntelEventRecordPayloadLabel
from ..models.intel_event_record_payload_status import IntelEventRecordPayloadStatus
from ..models.intel_event_record_payload_type import IntelEventRecordPayloadType
from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelEventRecordPayload")


@_attrs_define
class IntelEventRecordPayload:
    """Payload of `foresignal-intel` records of kind `intelligence-event` (externalId = event id); emitted only on change,
    tombstoned on dismiss or merge

        Attributes:
            type_ (IntelEventRecordPayloadType):
            severity (Severity):
            status (IntelEventRecordPayloadStatus):
            confidence (float):
            title (str):
            automated (bool):
            label (IntelEventRecordPayloadLabel | Unset):
            evidence_quality (IntelEventRecordPayloadEvidenceQuality | Unset):
            member_count (int | Unset):
            signal_families (list[str] | Unset):
    """

    type_: IntelEventRecordPayloadType
    severity: Severity
    status: IntelEventRecordPayloadStatus
    confidence: float
    title: str
    automated: bool
    label: IntelEventRecordPayloadLabel | Unset = UNSET
    evidence_quality: IntelEventRecordPayloadEvidenceQuality | Unset = UNSET
    member_count: int | Unset = UNSET
    signal_families: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        severity = self.severity.value

        status = self.status.value

        confidence = self.confidence

        title = self.title

        automated = self.automated

        label: str | Unset = UNSET
        if not isinstance(self.label, Unset):
            label = self.label.value

        evidence_quality: str | Unset = UNSET
        if not isinstance(self.evidence_quality, Unset):
            evidence_quality = self.evidence_quality.value

        member_count = self.member_count

        signal_families: list[str] | Unset = UNSET
        if not isinstance(self.signal_families, Unset):
            signal_families = self.signal_families

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "severity": severity,
                "status": status,
                "confidence": confidence,
                "title": title,
                "automated": automated,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label
        if evidence_quality is not UNSET:
            field_dict["evidenceQuality"] = evidence_quality
        if member_count is not UNSET:
            field_dict["memberCount"] = member_count
        if signal_families is not UNSET:
            field_dict["signalFamilies"] = signal_families

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = IntelEventRecordPayloadType(d.pop("type"))

        severity = Severity(d.pop("severity"))

        status = IntelEventRecordPayloadStatus(d.pop("status"))

        confidence = d.pop("confidence")

        title = d.pop("title")

        automated = d.pop("automated")

        _label = d.pop("label", UNSET)
        label: IntelEventRecordPayloadLabel | Unset
        if isinstance(_label, Unset):
            label = UNSET
        else:
            label = IntelEventRecordPayloadLabel(_label)

        _evidence_quality = d.pop("evidenceQuality", UNSET)
        evidence_quality: IntelEventRecordPayloadEvidenceQuality | Unset
        if isinstance(_evidence_quality, Unset):
            evidence_quality = UNSET
        else:
            evidence_quality = IntelEventRecordPayloadEvidenceQuality(_evidence_quality)

        member_count = d.pop("memberCount", UNSET)

        signal_families = cast(list[str], d.pop("signalFamilies", UNSET))

        intel_event_record_payload = cls(
            type_=type_,
            severity=severity,
            status=status,
            confidence=confidence,
            title=title,
            automated=automated,
            label=label,
            evidence_quality=evidence_quality,
            member_count=member_count,
            signal_families=signal_families,
        )

        intel_event_record_payload.additional_properties = d
        return intel_event_record_payload

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
