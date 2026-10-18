from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.canonical_event_quality_flags_item import CanonicalEventQualityFlagsItem
from ..models.canonical_event_status import CanonicalEventStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.evidence_group import EvidenceGroup
    from ..models.geometry import Geometry
    from ..models.observation_link import ObservationLink


T = TypeVar("T", bound="CanonicalEvent")


@_attrs_define
class CanonicalEvent:
    """A real-world event distinct from the observations that describe it (116)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str): Signal family plus subtype, e.g. seismic, fire, conflict, outage, disaster, weather-alert
        event_time (datetime.datetime):
        observation_count (int):
        evidence_group_count (int):
        quality_flags (list[CanonicalEventQualityFlagsItem]):
        status (CanonicalEventStatus):
        resolver_version (str):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        geometry (Geometry | None | Unset):
        region_codes (list[str] | Unset):
        first_observed_at (datetime.datetime | Unset):
        first_observation_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2).
            Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132)
            are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        merged_into (None | str | Unset):
        observations (list[ObservationLink] | Unset): Detail only
        evidence_groups (list[EvidenceGroup] | Unset): Detail only
    """

    id: str
    ref: str
    family: str
    event_time: datetime.datetime
    observation_count: int
    evidence_group_count: int
    quality_flags: list[CanonicalEventQualityFlagsItem]
    status: CanonicalEventStatus
    resolver_version: str
    created_at: datetime.datetime
    updated_at: datetime.datetime
    geometry: Geometry | None | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    first_observed_at: datetime.datetime | Unset = UNSET
    first_observation_ref: str | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    observations: list[ObservationLink] | Unset = UNSET
    evidence_groups: list[EvidenceGroup] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        id = self.id

        ref = self.ref

        family = self.family

        event_time = self.event_time.isoformat()

        observation_count = self.observation_count

        evidence_group_count = self.evidence_group_count

        quality_flags = []
        for quality_flags_item_data in self.quality_flags:
            quality_flags_item = quality_flags_item_data.value
            quality_flags.append(quality_flags_item)

        status = self.status.value

        resolver_version = self.resolver_version

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        first_observed_at: str | Unset = UNSET
        if not isinstance(self.first_observed_at, Unset):
            first_observed_at = self.first_observed_at.isoformat()

        first_observation_ref = self.first_observation_ref

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        observations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.observations, Unset):
            observations = []
            for observations_item_data in self.observations:
                observations_item = observations_item_data.to_dict()
                observations.append(observations_item)

        evidence_groups: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence_groups, Unset):
            evidence_groups = []
            for evidence_groups_item_data in self.evidence_groups:
                evidence_groups_item = evidence_groups_item_data.to_dict()
                evidence_groups.append(evidence_groups_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "family": family,
                "eventTime": event_time,
                "observationCount": observation_count,
                "evidenceGroupCount": evidence_group_count,
                "qualityFlags": quality_flags,
                "status": status,
                "resolverVersion": resolver_version,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if first_observed_at is not UNSET:
            field_dict["firstObservedAt"] = first_observed_at
        if first_observation_ref is not UNSET:
            field_dict["firstObservationRef"] = first_observation_ref
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if observations is not UNSET:
            field_dict["observations"] = observations
        if evidence_groups is not UNSET:
            field_dict["evidenceGroups"] = evidence_groups

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.evidence_group import EvidenceGroup
        from ..models.geometry import Geometry
        from ..models.observation_link import ObservationLink

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        family = d.pop("family")

        event_time = datetime.datetime.fromisoformat(d.pop("eventTime"))

        observation_count = d.pop("observationCount")

        evidence_group_count = d.pop("evidenceGroupCount")

        quality_flags = []
        _quality_flags = d.pop("qualityFlags")
        for quality_flags_item_data in _quality_flags:
            quality_flags_item = CanonicalEventQualityFlagsItem(quality_flags_item_data)

            quality_flags.append(quality_flags_item)

        status = CanonicalEventStatus(d.pop("status"))

        resolver_version = d.pop("resolverVersion")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        _first_observed_at = d.pop("firstObservedAt", UNSET)
        first_observed_at: datetime.datetime | Unset
        if isinstance(_first_observed_at, Unset):
            first_observed_at = UNSET
        else:
            first_observed_at = datetime.datetime.fromisoformat(_first_observed_at)

        first_observation_ref = d.pop("firstObservationRef", UNSET)

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        _observations = d.pop("observations", UNSET)
        observations: list[ObservationLink] | Unset = UNSET
        if _observations is not UNSET:
            observations = []
            for observations_item_data in _observations:
                observations_item = ObservationLink.from_dict(observations_item_data)

                observations.append(observations_item)

        _evidence_groups = d.pop("evidenceGroups", UNSET)
        evidence_groups: list[EvidenceGroup] | Unset = UNSET
        if _evidence_groups is not UNSET:
            evidence_groups = []
            for evidence_groups_item_data in _evidence_groups:
                evidence_groups_item = EvidenceGroup.from_dict(
                    evidence_groups_item_data
                )

                evidence_groups.append(evidence_groups_item)

        canonical_event = cls(
            id=id,
            ref=ref,
            family=family,
            event_time=event_time,
            observation_count=observation_count,
            evidence_group_count=evidence_group_count,
            quality_flags=quality_flags,
            status=status,
            resolver_version=resolver_version,
            created_at=created_at,
            updated_at=updated_at,
            geometry=geometry,
            region_codes=region_codes,
            first_observed_at=first_observed_at,
            first_observation_ref=first_observation_ref,
            merged_into=merged_into,
            observations=observations,
            evidence_groups=evidence_groups,
        )

        canonical_event.additional_properties = d
        return canonical_event

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
