from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_event_summary_confidence_label import (
    IntelEventSummaryConfidenceLabel,
)
from ..models.intel_event_summary_evidence_quality import (
    IntelEventSummaryEvidenceQuality,
)
from ..models.intel_event_summary_origin import IntelEventSummaryOrigin
from ..models.intel_event_summary_status import IntelEventSummaryStatus
from ..models.intel_event_summary_type import IntelEventSummaryType
from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="IntelEventSummary")


@_attrs_define
class IntelEventSummary:
    """Compact intelligence event for lists and SSE

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (IntelEventSummaryType):
        title (str):
        status (IntelEventSummaryStatus):
        severity (Severity):
        confidence (float): Score from EventScorer; not a calibrated probability unless `calibrated`
        confidence_label (IntelEventSummaryConfidenceLabel): low < 0.4 ≤ moderate < 0.7 ≤ high
        evidence_quality (IntelEventSummaryEvidenceQuality):
        member_count (int):
        start_time (datetime.datetime):
        version (int):
        updated_at (datetime.datetime):
        confidence_method (str | Unset):  Example: event-scorer-1.0.0.
        calibrated (bool | Unset): Set only by 103 validation
        signal_families (list[str] | Unset):
        end_time (datetime.datetime | None | Unset):
        last_evidence_at (datetime.datetime | None | Unset):
        centroid (Geometry | None | Unset):
        region_tags (list[str] | Unset):
        private (bool | Unset):
        origin (IntelEventSummaryOrigin | Unset):
        merged_into (None | str | Unset):
    """

    id: str
    type_: IntelEventSummaryType
    title: str
    status: IntelEventSummaryStatus
    severity: Severity
    confidence: float
    confidence_label: IntelEventSummaryConfidenceLabel
    evidence_quality: IntelEventSummaryEvidenceQuality
    member_count: int
    start_time: datetime.datetime
    version: int
    updated_at: datetime.datetime
    confidence_method: str | Unset = UNSET
    calibrated: bool | Unset = UNSET
    signal_families: list[str] | Unset = UNSET
    end_time: datetime.datetime | None | Unset = UNSET
    last_evidence_at: datetime.datetime | None | Unset = UNSET
    centroid: Geometry | None | Unset = UNSET
    region_tags: list[str] | Unset = UNSET
    private: bool | Unset = UNSET
    origin: IntelEventSummaryOrigin | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        id = self.id

        type_ = self.type_.value

        title = self.title

        status = self.status.value

        severity = self.severity.value

        confidence = self.confidence

        confidence_label = self.confidence_label.value

        evidence_quality = self.evidence_quality.value

        member_count = self.member_count

        start_time = self.start_time.isoformat()

        version = self.version

        updated_at = self.updated_at.isoformat()

        confidence_method = self.confidence_method

        calibrated = self.calibrated

        signal_families: list[str] | Unset = UNSET
        if not isinstance(self.signal_families, Unset):
            signal_families = self.signal_families

        end_time: None | str | Unset
        if isinstance(self.end_time, Unset):
            end_time = UNSET
        elif isinstance(self.end_time, datetime.datetime):
            end_time = self.end_time.isoformat()
        else:
            end_time = self.end_time

        last_evidence_at: None | str | Unset
        if isinstance(self.last_evidence_at, Unset):
            last_evidence_at = UNSET
        elif isinstance(self.last_evidence_at, datetime.datetime):
            last_evidence_at = self.last_evidence_at.isoformat()
        else:
            last_evidence_at = self.last_evidence_at

        centroid: dict[str, Any] | None | Unset
        if isinstance(self.centroid, Unset):
            centroid = UNSET
        elif isinstance(self.centroid, Geometry):
            centroid = self.centroid.to_dict()
        else:
            centroid = self.centroid

        region_tags: list[str] | Unset = UNSET
        if not isinstance(self.region_tags, Unset):
            region_tags = self.region_tags

        private = self.private

        origin: str | Unset = UNSET
        if not isinstance(self.origin, Unset):
            origin = self.origin.value

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "title": title,
                "status": status,
                "severity": severity,
                "confidence": confidence,
                "confidenceLabel": confidence_label,
                "evidenceQuality": evidence_quality,
                "memberCount": member_count,
                "startTime": start_time,
                "version": version,
                "updatedAt": updated_at,
            }
        )
        if confidence_method is not UNSET:
            field_dict["confidenceMethod"] = confidence_method
        if calibrated is not UNSET:
            field_dict["calibrated"] = calibrated
        if signal_families is not UNSET:
            field_dict["signalFamilies"] = signal_families
        if end_time is not UNSET:
            field_dict["endTime"] = end_time
        if last_evidence_at is not UNSET:
            field_dict["lastEvidenceAt"] = last_evidence_at
        if centroid is not UNSET:
            field_dict["centroid"] = centroid
        if region_tags is not UNSET:
            field_dict["regionTags"] = region_tags
        if private is not UNSET:
            field_dict["private"] = private
        if origin is not UNSET:
            field_dict["origin"] = origin
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        id = d.pop("id")

        type_ = IntelEventSummaryType(d.pop("type"))

        title = d.pop("title")

        status = IntelEventSummaryStatus(d.pop("status"))

        severity = Severity(d.pop("severity"))

        confidence = d.pop("confidence")

        confidence_label = IntelEventSummaryConfidenceLabel(d.pop("confidenceLabel"))

        evidence_quality = IntelEventSummaryEvidenceQuality(d.pop("evidenceQuality"))

        member_count = d.pop("memberCount")

        start_time = datetime.datetime.fromisoformat(d.pop("startTime"))

        version = d.pop("version")

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        confidence_method = d.pop("confidenceMethod", UNSET)

        calibrated = d.pop("calibrated", UNSET)

        signal_families = cast(list[str], d.pop("signalFamilies", UNSET))

        def _parse_end_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                end_time_type_0 = datetime.datetime.fromisoformat(data)

                return end_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        end_time = _parse_end_time(d.pop("endTime", UNSET))

        def _parse_last_evidence_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_evidence_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_evidence_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_evidence_at = _parse_last_evidence_at(d.pop("lastEvidenceAt", UNSET))

        def _parse_centroid(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                centroid_type_0 = Geometry.from_dict(data)

                return centroid_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        centroid = _parse_centroid(d.pop("centroid", UNSET))

        region_tags = cast(list[str], d.pop("regionTags", UNSET))

        private = d.pop("private", UNSET)

        _origin = d.pop("origin", UNSET)
        origin: IntelEventSummaryOrigin | Unset
        if isinstance(_origin, Unset):
            origin = UNSET
        else:
            origin = IntelEventSummaryOrigin(_origin)

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        intel_event_summary = cls(
            id=id,
            type_=type_,
            title=title,
            status=status,
            severity=severity,
            confidence=confidence,
            confidence_label=confidence_label,
            evidence_quality=evidence_quality,
            member_count=member_count,
            start_time=start_time,
            version=version,
            updated_at=updated_at,
            confidence_method=confidence_method,
            calibrated=calibrated,
            signal_families=signal_families,
            end_time=end_time,
            last_evidence_at=last_evidence_at,
            centroid=centroid,
            region_tags=region_tags,
            private=private,
            origin=origin,
            merged_into=merged_into,
        )

        intel_event_summary.additional_properties = d
        return intel_event_summary

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
