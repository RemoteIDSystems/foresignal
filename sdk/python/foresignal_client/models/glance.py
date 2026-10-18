from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.glance_status import GlanceStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.glance_activity import GlanceActivity
    from ..models.glance_alerts import GlanceAlerts
    from ..models.glance_briefing_type_0 import GlanceBriefingType0
    from ..models.glance_links import GlanceLinks
    from ..models.glance_sections import GlanceSections
    from ..models.glance_sources import GlanceSources


T = TypeVar("T", bound="Glance")


@_attrs_define
class Glance:
    """≤ 2,048-byte status document for widgets, watches and e-ink (038). Anonymous callers get status, serverTime,
    alerts.latest and lastSeq only (counts omitted unless glance.exposeCountsAnonymously).

        Attributes:
            server_time (datetime.datetime):
            status (GlanceStatus):
            last_seq (int):
            alerts (GlanceAlerts | Unset): Open alerts
            sources (GlanceSources | Unset): Source health tallies
            briefing (GlanceBriefingType0 | None | Unset): Latest briefing run
            activity (GlanceActivity | Unset): Recent activity
            links (GlanceLinks | Unset): Deep links from clientLinks
            sections (GlanceSections | Unset): GlanceContributor sections (e.g. weather at place, 056), each within a byte
                budget
    """

    server_time: datetime.datetime
    status: GlanceStatus
    last_seq: int
    alerts: GlanceAlerts | Unset = UNSET
    sources: GlanceSources | Unset = UNSET
    briefing: GlanceBriefingType0 | None | Unset = UNSET
    activity: GlanceActivity | Unset = UNSET
    links: GlanceLinks | Unset = UNSET
    sections: GlanceSections | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.glance_briefing_type_0 import GlanceBriefingType0

        server_time = self.server_time.isoformat()

        status = self.status.value

        last_seq = self.last_seq

        alerts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.alerts, Unset):
            alerts = self.alerts.to_dict()

        sources: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources.to_dict()

        briefing: dict[str, Any] | None | Unset
        if isinstance(self.briefing, Unset):
            briefing = UNSET
        elif isinstance(self.briefing, GlanceBriefingType0):
            briefing = self.briefing.to_dict()
        else:
            briefing = self.briefing

        activity: dict[str, Any] | Unset = UNSET
        if not isinstance(self.activity, Unset):
            activity = self.activity.to_dict()

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        sections: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sections, Unset):
            sections = self.sections.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "serverTime": server_time,
                "status": status,
                "lastSeq": last_seq,
            }
        )
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if sources is not UNSET:
            field_dict["sources"] = sources
        if briefing is not UNSET:
            field_dict["briefing"] = briefing
        if activity is not UNSET:
            field_dict["activity"] = activity
        if links is not UNSET:
            field_dict["links"] = links
        if sections is not UNSET:
            field_dict["sections"] = sections

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.glance_activity import GlanceActivity
        from ..models.glance_alerts import GlanceAlerts
        from ..models.glance_briefing_type_0 import GlanceBriefingType0
        from ..models.glance_links import GlanceLinks
        from ..models.glance_sections import GlanceSections
        from ..models.glance_sources import GlanceSources

        d = dict(src_dict)
        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        status = GlanceStatus(d.pop("status"))

        last_seq = d.pop("lastSeq")

        _alerts = d.pop("alerts", UNSET)
        alerts: GlanceAlerts | Unset
        if isinstance(_alerts, Unset):
            alerts = UNSET
        else:
            alerts = GlanceAlerts.from_dict(_alerts)

        _sources = d.pop("sources", UNSET)
        sources: GlanceSources | Unset
        if isinstance(_sources, Unset):
            sources = UNSET
        else:
            sources = GlanceSources.from_dict(_sources)

        def _parse_briefing(data: object) -> GlanceBriefingType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                briefing_type_0 = GlanceBriefingType0.from_dict(data)

                return briefing_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(GlanceBriefingType0 | None | Unset, data)

        briefing = _parse_briefing(d.pop("briefing", UNSET))

        _activity = d.pop("activity", UNSET)
        activity: GlanceActivity | Unset
        if isinstance(_activity, Unset):
            activity = UNSET
        else:
            activity = GlanceActivity.from_dict(_activity)

        _links = d.pop("links", UNSET)
        links: GlanceLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = GlanceLinks.from_dict(_links)

        _sections = d.pop("sections", UNSET)
        sections: GlanceSections | Unset
        if isinstance(_sections, Unset):
            sections = UNSET
        else:
            sections = GlanceSections.from_dict(_sections)

        glance = cls(
            server_time=server_time,
            status=status,
            last_seq=last_seq,
            alerts=alerts,
            sources=sources,
            briefing=briefing,
            activity=activity,
            links=links,
            sections=sections,
        )

        glance.additional_properties = d
        return glance

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
