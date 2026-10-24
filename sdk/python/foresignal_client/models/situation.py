from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..models.situation_origin import SituationOrigin
from ..models.situation_status import SituationStatus
from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.coverage_stats import CoverageStats
    from ..models.geometry import Geometry
    from ..models.situation_evidence import SituationEvidence
    from ..models.situation_inference import SituationInference
    from ..models.situation_social_section import SituationSocialSection
    from ..models.situation_speculation import SituationSpeculation


T = TypeVar("T", bound="Situation")


@_attrs_define
class Situation:
    """A persistent storyline: evidence (members), inference (status, severity, summary) and labeled speculation (brief)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        theme (SituationTheme): Situation theme (118 `ThemeTable`)
        status (SituationStatus): Situation lifecycle status; `closed` is final
        severity (Severity):
        region_codes (list[str]):
        started_at (datetime.datetime):
        last_activity_at (datetime.datetime):
        member_count (int):
        signal_families (list[str]): Signal families of the core members
        summary (str): Deterministic templated summary (inference)
        origin (SituationOrigin):
        private (bool): True when any member is private; follows `EgressPolicy`
        version (int): Optimistic-locking version; send it back on changes
        geometry (Geometry | None): Convex hull of member geometries buffered by 25 km (≤ 500 vertices)
        created_at (datetime.datetime):
        evidence (SituationEvidence): Evidence: members grouped by signal family, with the corroboration roll-up
        inference (SituationInference): Inference by named, versioned components: status, severity and the deterministic
            summary
        speculation (SituationSpeculation): Optional LLM brief, labeled speculation-capable narrative; never changes
            members, status or severity
        severity_overridden (bool | Unset): True when an operator override sets the severity
        bbox (list[float] | None | Unset): minLon, minLat, maxLon, maxLat of the geometry
        centroid (Geometry | None | Unset):
        closed_at (datetime.datetime | None | Unset):
        corroboration_share (float | None | Unset): corroborated ÷ (corroborated + uncorroborated); null when no member
            has a canonical event
        merged_into (None | str | Unset):
        updated_at (datetime.datetime | Unset):
        severity_override (None | Severity | Unset):
        topic_ids (list[str] | Unset): 117 attention topics
        entity_ids (list[str] | Unset): 098 entities
        investigation_ids (list[str] | Unset): 101 investigations
        linker_version (None | str | Unset):
        caveats (list[Caveat] | Unset): e.g. `UNCORROBORATED_ONLY`, `LOW_REPORTING_DENSITY`
        coverage (CoverageStats | None | Unset):
        social (None | SituationSocialSection | Unset): 137: top narratives with status, velocity, origin concentration
            and lead times, social evidence groups (separate from physical corroboration) and attention gap
    """

    id: str
    title: str
    theme: SituationTheme
    status: SituationStatus
    severity: Severity
    region_codes: list[str]
    started_at: datetime.datetime
    last_activity_at: datetime.datetime
    member_count: int
    signal_families: list[str]
    summary: str
    origin: SituationOrigin
    private: bool
    version: int
    geometry: Geometry | None
    created_at: datetime.datetime
    evidence: SituationEvidence
    inference: SituationInference
    speculation: SituationSpeculation
    severity_overridden: bool | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    centroid: Geometry | None | Unset = UNSET
    closed_at: datetime.datetime | None | Unset = UNSET
    corroboration_share: float | None | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    severity_override: None | Severity | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    entity_ids: list[str] | Unset = UNSET
    investigation_ids: list[str] | Unset = UNSET
    linker_version: None | str | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    coverage: CoverageStats | None | Unset = UNSET
    social: None | SituationSocialSection | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.coverage_stats import CoverageStats
        from ..models.geometry import Geometry
        from ..models.situation_social_section import SituationSocialSection

        id = self.id

        title = self.title

        theme = self.theme.value

        status = self.status.value

        severity = self.severity.value

        region_codes = self.region_codes

        started_at = self.started_at.isoformat()

        last_activity_at = self.last_activity_at.isoformat()

        member_count = self.member_count

        signal_families = self.signal_families

        summary = self.summary

        origin = self.origin.value

        private = self.private

        version = self.version

        geometry: dict[str, Any] | None
        if isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        created_at = self.created_at.isoformat()

        evidence = self.evidence.to_dict()

        inference = self.inference.to_dict()

        speculation = self.speculation.to_dict()

        severity_overridden = self.severity_overridden

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        centroid: dict[str, Any] | None | Unset
        if isinstance(self.centroid, Unset):
            centroid = UNSET
        elif isinstance(self.centroid, Geometry):
            centroid = self.centroid.to_dict()
        else:
            centroid = self.centroid

        closed_at: None | str | Unset
        if isinstance(self.closed_at, Unset):
            closed_at = UNSET
        elif isinstance(self.closed_at, datetime.datetime):
            closed_at = self.closed_at.isoformat()
        else:
            closed_at = self.closed_at

        corroboration_share: float | None | Unset
        if isinstance(self.corroboration_share, Unset):
            corroboration_share = UNSET
        else:
            corroboration_share = self.corroboration_share

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        severity_override: None | str | Unset
        if isinstance(self.severity_override, Unset):
            severity_override = UNSET
        elif isinstance(self.severity_override, Severity):
            severity_override = self.severity_override.value
        else:
            severity_override = self.severity_override

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        entity_ids: list[str] | Unset = UNSET
        if not isinstance(self.entity_ids, Unset):
            entity_ids = self.entity_ids

        investigation_ids: list[str] | Unset = UNSET
        if not isinstance(self.investigation_ids, Unset):
            investigation_ids = self.investigation_ids

        linker_version: None | str | Unset
        if isinstance(self.linker_version, Unset):
            linker_version = UNSET
        else:
            linker_version = self.linker_version

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        coverage: dict[str, Any] | None | Unset
        if isinstance(self.coverage, Unset):
            coverage = UNSET
        elif isinstance(self.coverage, CoverageStats):
            coverage = self.coverage.to_dict()
        else:
            coverage = self.coverage

        social: dict[str, Any] | None | Unset
        if isinstance(self.social, Unset):
            social = UNSET
        elif isinstance(self.social, SituationSocialSection):
            social = self.social.to_dict()
        else:
            social = self.social

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "theme": theme,
                "status": status,
                "severity": severity,
                "regionCodes": region_codes,
                "startedAt": started_at,
                "lastActivityAt": last_activity_at,
                "memberCount": member_count,
                "signalFamilies": signal_families,
                "summary": summary,
                "origin": origin,
                "private": private,
                "version": version,
                "geometry": geometry,
                "createdAt": created_at,
                "evidence": evidence,
                "inference": inference,
                "speculation": speculation,
            }
        )
        if severity_overridden is not UNSET:
            field_dict["severityOverridden"] = severity_overridden
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if centroid is not UNSET:
            field_dict["centroid"] = centroid
        if closed_at is not UNSET:
            field_dict["closedAt"] = closed_at
        if corroboration_share is not UNSET:
            field_dict["corroborationShare"] = corroboration_share
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if severity_override is not UNSET:
            field_dict["severityOverride"] = severity_override
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if entity_ids is not UNSET:
            field_dict["entityIds"] = entity_ids
        if investigation_ids is not UNSET:
            field_dict["investigationIds"] = investigation_ids
        if linker_version is not UNSET:
            field_dict["linkerVersion"] = linker_version
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if social is not UNSET:
            field_dict["social"] = social

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.coverage_stats import CoverageStats
        from ..models.geometry import Geometry
        from ..models.situation_evidence import SituationEvidence
        from ..models.situation_inference import SituationInference
        from ..models.situation_social_section import SituationSocialSection
        from ..models.situation_speculation import SituationSpeculation

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        theme = SituationTheme(d.pop("theme"))

        status = SituationStatus(d.pop("status"))

        severity = Severity(d.pop("severity"))

        region_codes = cast(list[str], d.pop("regionCodes"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        last_activity_at = datetime.datetime.fromisoformat(d.pop("lastActivityAt"))

        member_count = d.pop("memberCount")

        signal_families = cast(list[str], d.pop("signalFamilies"))

        summary = d.pop("summary")

        origin = SituationOrigin(d.pop("origin"))

        private = d.pop("private")

        version = d.pop("version")

        def _parse_geometry(data: object) -> Geometry | None:
            if data is None:
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None, data)

        geometry = _parse_geometry(d.pop("geometry"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        evidence = SituationEvidence.from_dict(d.pop("evidence"))

        inference = SituationInference.from_dict(d.pop("inference"))

        speculation = SituationSpeculation.from_dict(d.pop("speculation"))

        severity_overridden = d.pop("severityOverridden", UNSET)

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

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

        def _parse_closed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                closed_at_type_0 = datetime.datetime.fromisoformat(data)

                return closed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        closed_at = _parse_closed_at(d.pop("closedAt", UNSET))

        def _parse_corroboration_share(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        corroboration_share = _parse_corroboration_share(
            d.pop("corroborationShare", UNSET)
        )

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        def _parse_severity_override(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                severity_override_type_0 = Severity(data)

                return severity_override_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        severity_override = _parse_severity_override(d.pop("severityOverride", UNSET))

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        entity_ids = cast(list[str], d.pop("entityIds", UNSET))

        investigation_ids = cast(list[str], d.pop("investigationIds", UNSET))

        def _parse_linker_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        linker_version = _parse_linker_version(d.pop("linkerVersion", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        def _parse_coverage(data: object) -> CoverageStats | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coverage_type_0 = CoverageStats.from_dict(data)

                return coverage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CoverageStats | None | Unset, data)

        coverage = _parse_coverage(d.pop("coverage", UNSET))

        def _parse_social(data: object) -> None | SituationSocialSection | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                social_type_0 = SituationSocialSection.from_dict(data)

                return social_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationSocialSection | Unset, data)

        social = _parse_social(d.pop("social", UNSET))

        situation = cls(
            id=id,
            title=title,
            theme=theme,
            status=status,
            severity=severity,
            region_codes=region_codes,
            started_at=started_at,
            last_activity_at=last_activity_at,
            member_count=member_count,
            signal_families=signal_families,
            summary=summary,
            origin=origin,
            private=private,
            version=version,
            geometry=geometry,
            created_at=created_at,
            evidence=evidence,
            inference=inference,
            speculation=speculation,
            severity_overridden=severity_overridden,
            bbox=bbox,
            centroid=centroid,
            closed_at=closed_at,
            corroboration_share=corroboration_share,
            merged_into=merged_into,
            updated_at=updated_at,
            severity_override=severity_override,
            topic_ids=topic_ids,
            entity_ids=entity_ids,
            investigation_ids=investigation_ids,
            linker_version=linker_version,
            caveats=caveats,
            coverage=coverage,
            social=social,
        )

        situation.additional_properties = d
        return situation

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
