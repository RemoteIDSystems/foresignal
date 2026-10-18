from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..models.situation_status import SituationStatus
from ..models.situation_summary_origin import SituationSummaryOrigin
from ..models.situation_theme import SituationTheme
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="SituationSummary")


@_attrs_define
class SituationSummary:
    """Compact situation for lists and SSE events

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
        origin (SituationSummaryOrigin):
        private (bool): True when any member is private; follows `EgressPolicy`
        version (int): Optimistic-locking version; send it back on changes
        severity_overridden (bool | Unset): True when an operator override sets the severity
        bbox (list[float] | None | Unset): minLon, minLat, maxLon, maxLat of the geometry
        centroid (Geometry | None | Unset):
        closed_at (datetime.datetime | None | Unset):
        corroboration_share (float | None | Unset): corroborated ÷ (corroborated + uncorroborated); null when no member
            has a canonical event
        merged_into (None | str | Unset):
        updated_at (datetime.datetime | Unset):
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
    origin: SituationSummaryOrigin
    private: bool
    version: int
    severity_overridden: bool | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    centroid: Geometry | None | Unset = UNSET
    closed_at: datetime.datetime | None | Unset = UNSET
    corroboration_share: float | None | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

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

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

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

        origin = SituationSummaryOrigin(d.pop("origin"))

        private = d.pop("private")

        version = d.pop("version")

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

        situation_summary = cls(
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
            severity_overridden=severity_overridden,
            bbox=bbox,
            centroid=centroid,
            closed_at=closed_at,
            corroboration_share=corroboration_share,
            merged_into=merged_into,
            updated_at=updated_at,
        )

        situation_summary.additional_properties = d
        return situation_summary

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
