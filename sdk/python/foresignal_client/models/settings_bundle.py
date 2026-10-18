from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.briefing import Briefing
    from ..models.cors_settings import CorsSettings
    from ..models.custom_source_input import CustomSourceInput
    from ..models.llm_provider_input import LlmProviderInput
    from ..models.llm_settings import LlmSettings
    from ..models.notification_settings import NotificationSettings
    from ..models.place import Place
    from ..models.push_provider import PushProvider
    from ..models.retention_settings import RetentionSettings
    from ..models.saved_view import SavedView
    from ..models.security_settings import SecuritySettings
    from ..models.server_settings import ServerSettings
    from ..models.settings_bundle_channels_item import SettingsBundleChannelsItem
    from ..models.settings_bundle_sources import SettingsBundleSources
    from ..models.settings_bundle_watchlists_item import SettingsBundleWatchlistsItem
    from ..models.trigger_rule import TriggerRule
    from ..models.watchdog_settings import WatchdogSettings


T = TypeVar("T", bound="SettingsBundle")


@_attrs_define
class SettingsBundle:
    """
    Attributes:
        sources (SettingsBundleSources):
        exported_at (datetime.datetime | Unset):
        foresignal_version (str | Unset):
        retention (RetentionSettings | Unset): Retention windows (Core-Data-Model §13). PUT replaces the whole object;
            every duration ≥ 1h. STANDARD windows resolve in order: kinds.{kind}, sources.{id} (a view over
            source_settings.retention), the descriptor's kindRetention, its defaultRetention, then default; TRACK always
            uses trackWindow. Members owned by later prompts (see x-foresignal-property-prompts) are accepted and not stored
            until those prompts. Example: {'default': '7d', 'alerts': '90d', 'usage': '30d', 'sources': {'usgs-earthquakes':
            '3650d'}}.
        cors (CorsSettings | Unset):
        security (SecuritySettings | Unset):
        server (ServerSettings | Unset):
        triggers (list[TriggerRule] | Unset): Trigger rules with their ids; import upserts by id
        channels (list[SettingsBundleChannelsItem] | Unset): Channels without secrets: ChannelInput plus id. Import
            upserts by id; a channel imported without secrets keeps the ones it has.
        briefings (list[Briefing] | Unset):
        custom_sources (list[CustomSourceInput] | Unset):
        llm_providers (list[LlmProviderInput] | Unset):
        llm_settings (LlmSettings | Unset):
        notifications (NotificationSettings | Unset): Global notification pause (`app_setting['notifications']`)
        watchdog (WatchdogSettings | Unset): Off-box dead-man watchdog (`app_setting['watchdog']`) and optional ingest
            canary
        push_providers (list[PushProvider] | Unset): Without secrets (032). Import upserts by id: an existing provider
            keeps its key; a new one is stored disabled until an admin sets its key.
        places (list[Place] | Unset): Only with includePlaces=true (036)
        views (list[SavedView] | Unset): 036
        watchlists (list[SettingsBundleWatchlistsItem] | Unset): Only with includeWatchlists=true (036)
        warnings (list[str] | Unset): Export warnings (036): places reveal locations; watchlists identify what is
            followed
    """

    sources: SettingsBundleSources
    exported_at: datetime.datetime | Unset = UNSET
    foresignal_version: str | Unset = UNSET
    retention: RetentionSettings | Unset = UNSET
    cors: CorsSettings | Unset = UNSET
    security: SecuritySettings | Unset = UNSET
    server: ServerSettings | Unset = UNSET
    triggers: list[TriggerRule] | Unset = UNSET
    channels: list[SettingsBundleChannelsItem] | Unset = UNSET
    briefings: list[Briefing] | Unset = UNSET
    custom_sources: list[CustomSourceInput] | Unset = UNSET
    llm_providers: list[LlmProviderInput] | Unset = UNSET
    llm_settings: LlmSettings | Unset = UNSET
    notifications: NotificationSettings | Unset = UNSET
    watchdog: WatchdogSettings | Unset = UNSET
    push_providers: list[PushProvider] | Unset = UNSET
    places: list[Place] | Unset = UNSET
    views: list[SavedView] | Unset = UNSET
    watchlists: list[SettingsBundleWatchlistsItem] | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sources = self.sources.to_dict()

        exported_at: str | Unset = UNSET
        if not isinstance(self.exported_at, Unset):
            exported_at = self.exported_at.isoformat()

        foresignal_version = self.foresignal_version

        retention: dict[str, Any] | Unset = UNSET
        if not isinstance(self.retention, Unset):
            retention = self.retention.to_dict()

        cors: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cors, Unset):
            cors = self.cors.to_dict()

        security: dict[str, Any] | Unset = UNSET
        if not isinstance(self.security, Unset):
            security = self.security.to_dict()

        server: dict[str, Any] | Unset = UNSET
        if not isinstance(self.server, Unset):
            server = self.server.to_dict()

        triggers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.triggers, Unset):
            triggers = []
            for triggers_item_data in self.triggers:
                triggers_item = triggers_item_data.to_dict()
                triggers.append(triggers_item)

        channels: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.channels, Unset):
            channels = []
            for channels_item_data in self.channels:
                channels_item = channels_item_data.to_dict()
                channels.append(channels_item)

        briefings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.briefings, Unset):
            briefings = []
            for briefings_item_data in self.briefings:
                briefings_item = briefings_item_data.to_dict()
                briefings.append(briefings_item)

        custom_sources: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.custom_sources, Unset):
            custom_sources = []
            for custom_sources_item_data in self.custom_sources:
                custom_sources_item = custom_sources_item_data.to_dict()
                custom_sources.append(custom_sources_item)

        llm_providers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.llm_providers, Unset):
            llm_providers = []
            for llm_providers_item_data in self.llm_providers:
                llm_providers_item = llm_providers_item_data.to_dict()
                llm_providers.append(llm_providers_item)

        llm_settings: dict[str, Any] | Unset = UNSET
        if not isinstance(self.llm_settings, Unset):
            llm_settings = self.llm_settings.to_dict()

        notifications: dict[str, Any] | Unset = UNSET
        if not isinstance(self.notifications, Unset):
            notifications = self.notifications.to_dict()

        watchdog: dict[str, Any] | Unset = UNSET
        if not isinstance(self.watchdog, Unset):
            watchdog = self.watchdog.to_dict()

        push_providers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.push_providers, Unset):
            push_providers = []
            for push_providers_item_data in self.push_providers:
                push_providers_item = push_providers_item_data.to_dict()
                push_providers.append(push_providers_item)

        places: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.places, Unset):
            places = []
            for places_item_data in self.places:
                places_item = places_item_data.to_dict()
                places.append(places_item)

        views: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.views, Unset):
            views = []
            for views_item_data in self.views:
                views_item = views_item_data.to_dict()
                views.append(views_item)

        watchlists: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.watchlists, Unset):
            watchlists = []
            for watchlists_item_data in self.watchlists:
                watchlists_item = watchlists_item_data.to_dict()
                watchlists.append(watchlists_item)

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sources": sources,
            }
        )
        if exported_at is not UNSET:
            field_dict["exportedAt"] = exported_at
        if foresignal_version is not UNSET:
            field_dict["foresignalVersion"] = foresignal_version
        if retention is not UNSET:
            field_dict["retention"] = retention
        if cors is not UNSET:
            field_dict["cors"] = cors
        if security is not UNSET:
            field_dict["security"] = security
        if server is not UNSET:
            field_dict["server"] = server
        if triggers is not UNSET:
            field_dict["triggers"] = triggers
        if channels is not UNSET:
            field_dict["channels"] = channels
        if briefings is not UNSET:
            field_dict["briefings"] = briefings
        if custom_sources is not UNSET:
            field_dict["customSources"] = custom_sources
        if llm_providers is not UNSET:
            field_dict["llmProviders"] = llm_providers
        if llm_settings is not UNSET:
            field_dict["llmSettings"] = llm_settings
        if notifications is not UNSET:
            field_dict["notifications"] = notifications
        if watchdog is not UNSET:
            field_dict["watchdog"] = watchdog
        if push_providers is not UNSET:
            field_dict["pushProviders"] = push_providers
        if places is not UNSET:
            field_dict["places"] = places
        if views is not UNSET:
            field_dict["views"] = views
        if watchlists is not UNSET:
            field_dict["watchlists"] = watchlists
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.briefing import Briefing
        from ..models.cors_settings import CorsSettings
        from ..models.custom_source_input import CustomSourceInput
        from ..models.llm_provider_input import LlmProviderInput
        from ..models.llm_settings import LlmSettings
        from ..models.notification_settings import NotificationSettings
        from ..models.place import Place
        from ..models.push_provider import PushProvider
        from ..models.retention_settings import RetentionSettings
        from ..models.saved_view import SavedView
        from ..models.security_settings import SecuritySettings
        from ..models.server_settings import ServerSettings
        from ..models.settings_bundle_channels_item import SettingsBundleChannelsItem
        from ..models.settings_bundle_sources import SettingsBundleSources
        from ..models.settings_bundle_watchlists_item import (
            SettingsBundleWatchlistsItem,
        )
        from ..models.trigger_rule import TriggerRule
        from ..models.watchdog_settings import WatchdogSettings

        d = dict(src_dict)
        sources = SettingsBundleSources.from_dict(d.pop("sources"))

        _exported_at = d.pop("exportedAt", UNSET)
        exported_at: datetime.datetime | Unset
        if isinstance(_exported_at, Unset):
            exported_at = UNSET
        else:
            exported_at = datetime.datetime.fromisoformat(_exported_at)

        foresignal_version = d.pop("foresignalVersion", UNSET)

        _retention = d.pop("retention", UNSET)
        retention: RetentionSettings | Unset
        if isinstance(_retention, Unset):
            retention = UNSET
        else:
            retention = RetentionSettings.from_dict(_retention)

        _cors = d.pop("cors", UNSET)
        cors: CorsSettings | Unset
        if isinstance(_cors, Unset):
            cors = UNSET
        else:
            cors = CorsSettings.from_dict(_cors)

        _security = d.pop("security", UNSET)
        security: SecuritySettings | Unset
        if isinstance(_security, Unset):
            security = UNSET
        else:
            security = SecuritySettings.from_dict(_security)

        _server = d.pop("server", UNSET)
        server: ServerSettings | Unset
        if isinstance(_server, Unset):
            server = UNSET
        else:
            server = ServerSettings.from_dict(_server)

        _triggers = d.pop("triggers", UNSET)
        triggers: list[TriggerRule] | Unset = UNSET
        if _triggers is not UNSET:
            triggers = []
            for triggers_item_data in _triggers:
                triggers_item = TriggerRule.from_dict(triggers_item_data)

                triggers.append(triggers_item)

        _channels = d.pop("channels", UNSET)
        channels: list[SettingsBundleChannelsItem] | Unset = UNSET
        if _channels is not UNSET:
            channels = []
            for channels_item_data in _channels:
                channels_item = SettingsBundleChannelsItem.from_dict(channels_item_data)

                channels.append(channels_item)

        _briefings = d.pop("briefings", UNSET)
        briefings: list[Briefing] | Unset = UNSET
        if _briefings is not UNSET:
            briefings = []
            for briefings_item_data in _briefings:
                briefings_item = Briefing.from_dict(briefings_item_data)

                briefings.append(briefings_item)

        _custom_sources = d.pop("customSources", UNSET)
        custom_sources: list[CustomSourceInput] | Unset = UNSET
        if _custom_sources is not UNSET:
            custom_sources = []
            for custom_sources_item_data in _custom_sources:
                custom_sources_item = CustomSourceInput.from_dict(
                    custom_sources_item_data
                )

                custom_sources.append(custom_sources_item)

        _llm_providers = d.pop("llmProviders", UNSET)
        llm_providers: list[LlmProviderInput] | Unset = UNSET
        if _llm_providers is not UNSET:
            llm_providers = []
            for llm_providers_item_data in _llm_providers:
                llm_providers_item = LlmProviderInput.from_dict(llm_providers_item_data)

                llm_providers.append(llm_providers_item)

        _llm_settings = d.pop("llmSettings", UNSET)
        llm_settings: LlmSettings | Unset
        if isinstance(_llm_settings, Unset):
            llm_settings = UNSET
        else:
            llm_settings = LlmSettings.from_dict(_llm_settings)

        _notifications = d.pop("notifications", UNSET)
        notifications: NotificationSettings | Unset
        if isinstance(_notifications, Unset):
            notifications = UNSET
        else:
            notifications = NotificationSettings.from_dict(_notifications)

        _watchdog = d.pop("watchdog", UNSET)
        watchdog: WatchdogSettings | Unset
        if isinstance(_watchdog, Unset):
            watchdog = UNSET
        else:
            watchdog = WatchdogSettings.from_dict(_watchdog)

        _push_providers = d.pop("pushProviders", UNSET)
        push_providers: list[PushProvider] | Unset = UNSET
        if _push_providers is not UNSET:
            push_providers = []
            for push_providers_item_data in _push_providers:
                push_providers_item = PushProvider.from_dict(push_providers_item_data)

                push_providers.append(push_providers_item)

        _places = d.pop("places", UNSET)
        places: list[Place] | Unset = UNSET
        if _places is not UNSET:
            places = []
            for places_item_data in _places:
                places_item = Place.from_dict(places_item_data)

                places.append(places_item)

        _views = d.pop("views", UNSET)
        views: list[SavedView] | Unset = UNSET
        if _views is not UNSET:
            views = []
            for views_item_data in _views:
                views_item = SavedView.from_dict(views_item_data)

                views.append(views_item)

        _watchlists = d.pop("watchlists", UNSET)
        watchlists: list[SettingsBundleWatchlistsItem] | Unset = UNSET
        if _watchlists is not UNSET:
            watchlists = []
            for watchlists_item_data in _watchlists:
                watchlists_item = SettingsBundleWatchlistsItem.from_dict(
                    watchlists_item_data
                )

                watchlists.append(watchlists_item)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        settings_bundle = cls(
            sources=sources,
            exported_at=exported_at,
            foresignal_version=foresignal_version,
            retention=retention,
            cors=cors,
            security=security,
            server=server,
            triggers=triggers,
            channels=channels,
            briefings=briefings,
            custom_sources=custom_sources,
            llm_providers=llm_providers,
            llm_settings=llm_settings,
            notifications=notifications,
            watchdog=watchdog,
            push_providers=push_providers,
            places=places,
            views=views,
            watchlists=watchlists,
            warnings=warnings,
        )

        settings_bundle.additional_properties = d
        return settings_bundle

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
