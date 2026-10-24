from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intelligence_event_origin import IntelligenceEventOrigin
from ..models.intelligence_event_status import IntelligenceEventStatus
from ..models.intelligence_event_type import IntelligenceEventType
from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef
    from ..models.exposure import Exposure
    from ..models.intel_event_log_entry import IntelEventLogEntry
    from ..models.intelligence_event_context import IntelligenceEventContext
    from ..models.intelligence_event_evidence import IntelligenceEventEvidence
    from ..models.intelligence_event_inference import IntelligenceEventInference
    from ..models.intelligence_event_speculation import IntelligenceEventSpeculation


T = TypeVar("T", bound="IntelligenceEvent")


@_attrs_define
class IntelligenceEvent:
    """A typed, located, time-bounded finding from deterministic recipes, with evidence, inference and speculation in
    separate sections

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            type_ (IntelligenceEventType):
            title (str):
            status (IntelligenceEventStatus):
            severity (Severity):
            origin (IntelligenceEventOrigin):
            detector (DetectorRef):
            version (int): Optimistic-lock version; send it as If-Match on PATCH
            created_at (datetime.datetime):
            updated_at (datetime.datetime):
            evidence (IntelligenceEventEvidence): Observed members
            inference (IntelligenceEventInference): What the detector derived
            speculation (IntelligenceEventSpeculation): Alternatives and any LLM narrative, always labeled
            severity_overridden (bool | Unset): True when an operator override sets the severity; `severity: null` in
                `IntelEventPatch` clears it and restores the computed severity
            private (bool | Unset): True when any member comes from a private source
            recipe_id (None | str | Unset):
            recipe_version (int | None | Unset):
            merged_into (None | str | Unset):
            notes (None | str | Unset): Operator or agent notes (markdown)
            context (IntelligenceEventContext | Unset): Context added by annotators
            exposure (Exposure | None | Unset):
            log (list[IntelEventLogEntry] | Unset):
            manifest_id (None | str | Unset): The analysis manifest of this output (prompt 116); null for outputs written
                before it
    """

    id: str
    type_: IntelligenceEventType
    title: str
    status: IntelligenceEventStatus
    severity: Severity
    origin: IntelligenceEventOrigin
    detector: DetectorRef
    version: int
    created_at: datetime.datetime
    updated_at: datetime.datetime
    evidence: IntelligenceEventEvidence
    inference: IntelligenceEventInference
    speculation: IntelligenceEventSpeculation
    severity_overridden: bool | Unset = UNSET
    private: bool | Unset = UNSET
    recipe_id: None | str | Unset = UNSET
    recipe_version: int | None | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    notes: None | str | Unset = UNSET
    context: IntelligenceEventContext | Unset = UNSET
    exposure: Exposure | None | Unset = UNSET
    log: list[IntelEventLogEntry] | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.exposure import Exposure

        id = self.id

        type_ = self.type_.value

        title = self.title

        status = self.status.value

        severity = self.severity.value

        origin = self.origin.value

        detector = self.detector.to_dict()

        version = self.version

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        evidence = self.evidence.to_dict()

        inference = self.inference.to_dict()

        speculation = self.speculation.to_dict()

        severity_overridden = self.severity_overridden

        private = self.private

        recipe_id: None | str | Unset
        if isinstance(self.recipe_id, Unset):
            recipe_id = UNSET
        else:
            recipe_id = self.recipe_id

        recipe_version: int | None | Unset
        if isinstance(self.recipe_version, Unset):
            recipe_version = UNSET
        else:
            recipe_version = self.recipe_version

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        context: dict[str, Any] | Unset = UNSET
        if not isinstance(self.context, Unset):
            context = self.context.to_dict()

        exposure: dict[str, Any] | None | Unset
        if isinstance(self.exposure, Unset):
            exposure = UNSET
        elif isinstance(self.exposure, Exposure):
            exposure = self.exposure.to_dict()
        else:
            exposure = self.exposure

        log: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.log, Unset):
            log = []
            for log_item_data in self.log:
                log_item = log_item_data.to_dict()
                log.append(log_item)

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "title": title,
                "status": status,
                "severity": severity,
                "origin": origin,
                "detector": detector,
                "version": version,
                "createdAt": created_at,
                "updatedAt": updated_at,
                "evidence": evidence,
                "inference": inference,
                "speculation": speculation,
            }
        )
        if severity_overridden is not UNSET:
            field_dict["severityOverridden"] = severity_overridden
        if private is not UNSET:
            field_dict["private"] = private
        if recipe_id is not UNSET:
            field_dict["recipeId"] = recipe_id
        if recipe_version is not UNSET:
            field_dict["recipeVersion"] = recipe_version
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if notes is not UNSET:
            field_dict["notes"] = notes
        if context is not UNSET:
            field_dict["context"] = context
        if exposure is not UNSET:
            field_dict["exposure"] = exposure
        if log is not UNSET:
            field_dict["log"] = log
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref import DetectorRef
        from ..models.exposure import Exposure
        from ..models.intel_event_log_entry import IntelEventLogEntry
        from ..models.intelligence_event_context import IntelligenceEventContext
        from ..models.intelligence_event_evidence import IntelligenceEventEvidence
        from ..models.intelligence_event_inference import IntelligenceEventInference
        from ..models.intelligence_event_speculation import IntelligenceEventSpeculation

        d = dict(src_dict)
        id = d.pop("id")

        type_ = IntelligenceEventType(d.pop("type"))

        title = d.pop("title")

        status = IntelligenceEventStatus(d.pop("status"))

        severity = Severity(d.pop("severity"))

        origin = IntelligenceEventOrigin(d.pop("origin"))

        detector = DetectorRef.from_dict(d.pop("detector"))

        version = d.pop("version")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        evidence = IntelligenceEventEvidence.from_dict(d.pop("evidence"))

        inference = IntelligenceEventInference.from_dict(d.pop("inference"))

        speculation = IntelligenceEventSpeculation.from_dict(d.pop("speculation"))

        severity_overridden = d.pop("severityOverridden", UNSET)

        private = d.pop("private", UNSET)

        def _parse_recipe_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        recipe_id = _parse_recipe_id(d.pop("recipeId", UNSET))

        def _parse_recipe_version(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        recipe_version = _parse_recipe_version(d.pop("recipeVersion", UNSET))

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        _context = d.pop("context", UNSET)
        context: IntelligenceEventContext | Unset
        if isinstance(_context, Unset):
            context = UNSET
        else:
            context = IntelligenceEventContext.from_dict(_context)

        def _parse_exposure(data: object) -> Exposure | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                exposure_type_0 = Exposure.from_dict(data)

                return exposure_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Exposure | None | Unset, data)

        exposure = _parse_exposure(d.pop("exposure", UNSET))

        _log = d.pop("log", UNSET)
        log: list[IntelEventLogEntry] | Unset = UNSET
        if _log is not UNSET:
            log = []
            for log_item_data in _log:
                log_item = IntelEventLogEntry.from_dict(log_item_data)

                log.append(log_item)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        intelligence_event = cls(
            id=id,
            type_=type_,
            title=title,
            status=status,
            severity=severity,
            origin=origin,
            detector=detector,
            version=version,
            created_at=created_at,
            updated_at=updated_at,
            evidence=evidence,
            inference=inference,
            speculation=speculation,
            severity_overridden=severity_overridden,
            private=private,
            recipe_id=recipe_id,
            recipe_version=recipe_version,
            merged_into=merged_into,
            notes=notes,
            context=context,
            exposure=exposure,
            log=log,
            manifest_id=manifest_id,
        )

        intelligence_event.additional_properties = d
        return intelligence_event

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
