from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.retention_settings_kinds import RetentionSettingsKinds
    from ..models.retention_settings_social import RetentionSettingsSocial
    from ..models.retention_settings_sources import RetentionSettingsSources
    from ..models.retention_settings_stats import RetentionSettingsStats


T = TypeVar("T", bound="RetentionSettings")


@_attrs_define
class RetentionSettings:
    """Retention windows (Core-Data-Model §13). PUT replaces the whole object; every duration ≥ 1h. STANDARD windows
    resolve in order: kinds.{kind}, sources.{id} (a view over source_settings.retention), the descriptor's
    kindRetention, its defaultRetention, then default; TRACK always uses trackWindow. Members owned by later prompts
    (see x-foresignal-property-prompts) are accepted and not stored until those prompts.

        Example:
            {'default': '7d', 'alerts': '90d', 'usage': '30d', 'sources': {'usgs-earthquakes': '3650d'}}

        Attributes:
            default (str): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            alerts (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            usage (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            briefings (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            ask (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            sources (RetentionSettingsSources | Unset): Per-source overrides (view over source_settings.retention); TRACK
                sources → 400 RETENTION_TIER_FIXED Example: {'usgs-earthquakes': '3650d'}.
            track_window (str | Unset): Single window of the TRACK tier. ISO-8601 duration (P7D, PT15M) or shorthand (7d,
                15m, 12h) Default: '3d'. Example: 3d.
            provenance (str | Unset): fetch_log rows with versions. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m,
                12h) Default: '30d'. Example: 30d.
            notifications (str | Unset): notification rows. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
                Default: '90d'. Example: 90d.
            kinds (RetentionSettingsKinds | Unset): Per-kind windows (STANDARD only), first in the resolution order Example:
                {'satellite-pass': '365d'}.
            audit (str | Unset): Audit log retention (default 90d) Example: 90d.
            stats (RetentionSettingsStats | Unset): Retention of 038 activity rollups and 119 region rollups
            inbox (str | Unset): Notification inbox retention (033). Separate from `notifications` (delivery rows, 019).
                Default: '30d'. Example: 30d.
            track_raw_window (None | str | Unset): TRACK history older than this is downsampled; null disables (must be <
                trackWindow). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 24h.
            track_downsample_interval (str | Unset): One sample per entity per interval in downsampled leaves (30s ≤
                interval ≤ trackRawWindow). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 5m.
            entity_links (str | Unset): How long closed entity links are kept (098; default 365d). ISO-8601 duration (P7D,
                PT15M) or shorthand (7d, 15m, 12h) Example: 365d.
            social (RetentionSettingsSocial | Unset): Analytic class `social` (135): `social-post` retention per platform
                (view over `social.postRetention`); narratives, claims, comparisons and volume series are kept as intel and
                series
    """

    default: str
    alerts: str | Unset = UNSET
    usage: str | Unset = UNSET
    briefings: str | Unset = UNSET
    ask: str | Unset = UNSET
    sources: RetentionSettingsSources | Unset = UNSET
    track_window: str | Unset = "3d"
    provenance: str | Unset = "30d"
    notifications: str | Unset = "90d"
    kinds: RetentionSettingsKinds | Unset = UNSET
    audit: str | Unset = UNSET
    stats: RetentionSettingsStats | Unset = UNSET
    inbox: str | Unset = "30d"
    track_raw_window: None | str | Unset = UNSET
    track_downsample_interval: str | Unset = UNSET
    entity_links: str | Unset = UNSET
    social: RetentionSettingsSocial | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        default = self.default

        alerts = self.alerts

        usage = self.usage

        briefings = self.briefings

        ask = self.ask

        sources: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources.to_dict()

        track_window = self.track_window

        provenance = self.provenance

        notifications = self.notifications

        kinds: dict[str, Any] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds.to_dict()

        audit = self.audit

        stats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stats, Unset):
            stats = self.stats.to_dict()

        inbox = self.inbox

        track_raw_window: None | str | Unset
        if isinstance(self.track_raw_window, Unset):
            track_raw_window = UNSET
        else:
            track_raw_window = self.track_raw_window

        track_downsample_interval = self.track_downsample_interval

        entity_links = self.entity_links

        social: dict[str, Any] | Unset = UNSET
        if not isinstance(self.social, Unset):
            social = self.social.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "default": default,
            }
        )
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if usage is not UNSET:
            field_dict["usage"] = usage
        if briefings is not UNSET:
            field_dict["briefings"] = briefings
        if ask is not UNSET:
            field_dict["ask"] = ask
        if sources is not UNSET:
            field_dict["sources"] = sources
        if track_window is not UNSET:
            field_dict["trackWindow"] = track_window
        if provenance is not UNSET:
            field_dict["provenance"] = provenance
        if notifications is not UNSET:
            field_dict["notifications"] = notifications
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if audit is not UNSET:
            field_dict["audit"] = audit
        if stats is not UNSET:
            field_dict["stats"] = stats
        if inbox is not UNSET:
            field_dict["inbox"] = inbox
        if track_raw_window is not UNSET:
            field_dict["trackRawWindow"] = track_raw_window
        if track_downsample_interval is not UNSET:
            field_dict["trackDownsampleInterval"] = track_downsample_interval
        if entity_links is not UNSET:
            field_dict["entityLinks"] = entity_links
        if social is not UNSET:
            field_dict["social"] = social

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.retention_settings_kinds import RetentionSettingsKinds
        from ..models.retention_settings_social import RetentionSettingsSocial
        from ..models.retention_settings_sources import RetentionSettingsSources
        from ..models.retention_settings_stats import RetentionSettingsStats

        d = dict(src_dict)
        default = d.pop("default")

        alerts = d.pop("alerts", UNSET)

        usage = d.pop("usage", UNSET)

        briefings = d.pop("briefings", UNSET)

        ask = d.pop("ask", UNSET)

        _sources = d.pop("sources", UNSET)
        sources: RetentionSettingsSources | Unset
        if isinstance(_sources, Unset):
            sources = UNSET
        else:
            sources = RetentionSettingsSources.from_dict(_sources)

        track_window = d.pop("trackWindow", UNSET)

        provenance = d.pop("provenance", UNSET)

        notifications = d.pop("notifications", UNSET)

        _kinds = d.pop("kinds", UNSET)
        kinds: RetentionSettingsKinds | Unset
        if isinstance(_kinds, Unset):
            kinds = UNSET
        else:
            kinds = RetentionSettingsKinds.from_dict(_kinds)

        audit = d.pop("audit", UNSET)

        _stats = d.pop("stats", UNSET)
        stats: RetentionSettingsStats | Unset
        if isinstance(_stats, Unset):
            stats = UNSET
        else:
            stats = RetentionSettingsStats.from_dict(_stats)

        inbox = d.pop("inbox", UNSET)

        def _parse_track_raw_window(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        track_raw_window = _parse_track_raw_window(d.pop("trackRawWindow", UNSET))

        track_downsample_interval = d.pop("trackDownsampleInterval", UNSET)

        entity_links = d.pop("entityLinks", UNSET)

        _social = d.pop("social", UNSET)
        social: RetentionSettingsSocial | Unset
        if isinstance(_social, Unset):
            social = UNSET
        else:
            social = RetentionSettingsSocial.from_dict(_social)

        retention_settings = cls(
            default=default,
            alerts=alerts,
            usage=usage,
            briefings=briefings,
            ask=ask,
            sources=sources,
            track_window=track_window,
            provenance=provenance,
            notifications=notifications,
            kinds=kinds,
            audit=audit,
            stats=stats,
            inbox=inbox,
            track_raw_window=track_raw_window,
            track_downsample_interval=track_downsample_interval,
            entity_links=entity_links,
            social=social,
        )

        retention_settings.additional_properties = d
        return retention_settings

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
