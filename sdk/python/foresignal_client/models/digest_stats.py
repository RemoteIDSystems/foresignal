from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.digest_active import DigestActive
    from ..models.digest_alerts import DigestAlerts
    from ..models.digest_count import DigestCount
    from ..models.digest_health_item import DigestHealthItem
    from ..models.digest_stats_notable_item import DigestStatsNotableItem
    from ..models.digest_stats_sections import DigestStatsSections


T = TypeVar("T", bound="DigestStats")


@_attrs_define
class DigestStats:
    """Digest statistics shared by briefing runs and GET /v1/digest (029; counts served from 038 stats afterwards)

    Attributes:
        from_seq (int):
        to_seq (int):
        gap (bool): True when fromSeq is below the retention floor of a scope source; counts are incomplete
        counts (list[DigestCount]):
        gap_reason (None | str | Unset):
        active (list[DigestActive] | Unset):
        alerts (DigestAlerts | Unset): Alerts in scope for a digest period, keyed by alert id (029)
        notable (list[DigestStatsNotableItem] | Unset):
        health (list[DigestHealthItem] | None | Unset): null when includeHealth is false
        health_snapshot (bool | Unset): True when health is the current snapshot rather than stored transitions (always
            true until health transitions are stored)
        sections (DigestStatsSections | Unset): Sections from BriefingSectionContributor beans (e.g. 056 weather at
            places)
    """

    from_seq: int
    to_seq: int
    gap: bool
    counts: list[DigestCount]
    gap_reason: None | str | Unset = UNSET
    active: list[DigestActive] | Unset = UNSET
    alerts: DigestAlerts | Unset = UNSET
    notable: list[DigestStatsNotableItem] | Unset = UNSET
    health: list[DigestHealthItem] | None | Unset = UNSET
    health_snapshot: bool | Unset = UNSET
    sections: DigestStatsSections | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_seq = self.from_seq

        to_seq = self.to_seq

        gap = self.gap

        counts = []
        for counts_item_data in self.counts:
            counts_item = counts_item_data.to_dict()
            counts.append(counts_item)

        gap_reason: None | str | Unset
        if isinstance(self.gap_reason, Unset):
            gap_reason = UNSET
        else:
            gap_reason = self.gap_reason

        active: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.active, Unset):
            active = []
            for active_item_data in self.active:
                active_item = active_item_data.to_dict()
                active.append(active_item)

        alerts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.alerts, Unset):
            alerts = self.alerts.to_dict()

        notable: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.notable, Unset):
            notable = []
            for notable_item_data in self.notable:
                notable_item = notable_item_data.to_dict()
                notable.append(notable_item)

        health: list[dict[str, Any]] | None | Unset
        if isinstance(self.health, Unset):
            health = UNSET
        elif isinstance(self.health, list):
            health = []
            for health_type_0_item_data in self.health:
                health_type_0_item = health_type_0_item_data.to_dict()
                health.append(health_type_0_item)

        else:
            health = self.health

        health_snapshot = self.health_snapshot

        sections: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sections, Unset):
            sections = self.sections.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fromSeq": from_seq,
                "toSeq": to_seq,
                "gap": gap,
                "counts": counts,
            }
        )
        if gap_reason is not UNSET:
            field_dict["gapReason"] = gap_reason
        if active is not UNSET:
            field_dict["active"] = active
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if notable is not UNSET:
            field_dict["notable"] = notable
        if health is not UNSET:
            field_dict["health"] = health
        if health_snapshot is not UNSET:
            field_dict["healthSnapshot"] = health_snapshot
        if sections is not UNSET:
            field_dict["sections"] = sections

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.digest_active import DigestActive
        from ..models.digest_alerts import DigestAlerts
        from ..models.digest_count import DigestCount
        from ..models.digest_health_item import DigestHealthItem
        from ..models.digest_stats_notable_item import DigestStatsNotableItem
        from ..models.digest_stats_sections import DigestStatsSections

        d = dict(src_dict)
        from_seq = d.pop("fromSeq")

        to_seq = d.pop("toSeq")

        gap = d.pop("gap")

        counts = []
        _counts = d.pop("counts")
        for counts_item_data in _counts:
            counts_item = DigestCount.from_dict(counts_item_data)

            counts.append(counts_item)

        def _parse_gap_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        gap_reason = _parse_gap_reason(d.pop("gapReason", UNSET))

        _active = d.pop("active", UNSET)
        active: list[DigestActive] | Unset = UNSET
        if _active is not UNSET:
            active = []
            for active_item_data in _active:
                active_item = DigestActive.from_dict(active_item_data)

                active.append(active_item)

        _alerts = d.pop("alerts", UNSET)
        alerts: DigestAlerts | Unset
        if isinstance(_alerts, Unset):
            alerts = UNSET
        else:
            alerts = DigestAlerts.from_dict(_alerts)

        _notable = d.pop("notable", UNSET)
        notable: list[DigestStatsNotableItem] | Unset = UNSET
        if _notable is not UNSET:
            notable = []
            for notable_item_data in _notable:
                notable_item = DigestStatsNotableItem.from_dict(notable_item_data)

                notable.append(notable_item)

        def _parse_health(data: object) -> list[DigestHealthItem] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                health_type_0 = []
                _health_type_0 = data
                for health_type_0_item_data in _health_type_0:
                    health_type_0_item = DigestHealthItem.from_dict(
                        health_type_0_item_data
                    )

                    health_type_0.append(health_type_0_item)

                return health_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[DigestHealthItem] | None | Unset, data)

        health = _parse_health(d.pop("health", UNSET))

        health_snapshot = d.pop("healthSnapshot", UNSET)

        _sections = d.pop("sections", UNSET)
        sections: DigestStatsSections | Unset
        if isinstance(_sections, Unset):
            sections = UNSET
        else:
            sections = DigestStatsSections.from_dict(_sections)

        digest_stats = cls(
            from_seq=from_seq,
            to_seq=to_seq,
            gap=gap,
            counts=counts,
            gap_reason=gap_reason,
            active=active,
            alerts=alerts,
            notable=notable,
            health=health,
            health_snapshot=health_snapshot,
            sections=sections,
        )

        digest_stats.additional_properties = d
        return digest_stats

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
