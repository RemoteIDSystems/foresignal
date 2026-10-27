from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.channel_input_events_item import ChannelInputEventsItem
from ..models.channel_input_min_severity import ChannelInputMinSeverity
from ..models.channel_input_type import ChannelInputType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.channel_input_secrets import ChannelInputSecrets
    from ..models.map_attachment_config import MapAttachmentConfig
    from ..models.quiet_hours import QuietHours


T = TypeVar("T", bound="ChannelInput")


@_attrs_define
class ChannelInput:
    """
    Attributes:
        name (str):
        type_ (ChannelInputType): teams accepts only Power Automate Workflows URLs (Office 365 connector URLs → 400);
            webpush is the built-in Push channel (031): created at startup with a fixed id, updatable but not creatable
            (400); its config takes quietHoursMode (defer|downgrade|drop); mqtt (060) publishes on the operator's broker
            (config MqttChannelConfig, secrets username, password, caCertPem): events on {prefix}/alerts/{severity},
            {prefix}/alerts/updates, {prefix}/briefings/{briefingId} and {prefix}/system/events, plus retained state and
            optional Home Assistant discovery; a public broker without TLS or credentials is saved with the warning
            MQTT_INSECURE_PUBLIC_BROKER; meshtastic (084) sends compact alert lines through the node's MQTT JSON downlink
            (config MeshtasticChannelConfig, secrets username, password) and meshcore (084) through the MeshCore companion
            protocol (config MeshcoreChannelConfig); both refuse public or default channels with MESH_PUBLIC_CHANNEL, pass
            every transmission through a LoRa airtime governor and take only alert.resolved among lifecycle updates
        config (MapAttachmentConfig | Unset): Map-attachment keys accepted in every channel's `config` (055)
        secrets (ChannelInputSecrets | Unset): Write-only secret settings per the type's secretFields. A secretFields
            entry named `prefix.<Name>` (webhook `header.<Name>`) accepts any secret whose name starts with the prefix.
        events (list[ChannelInputEventsItem] | Unset): Default ["alert"]. The personal-notice events (`mention`,
            `share`, `report.received`, `package.received`, `chronicle.proposed`, `chronicle.cap_warning`, `chronicle.gap`,
            `chronicle.access.requested`, `saved-search.matches`) deliver a copy of each such notice, once per addressee,
            with its TLP label at the head
        rule_ids (list[UUID] | None | Unset): null = all rules
        min_severity (ChannelInputMinSeverity | Unset):  Default: ChannelInputMinSeverity.INFO.
        title_template (None | str | Unset):
        body_template (None | str | Unset):
        wait_for_enrichment_seconds (int | Unset):  Default: 0.
        enabled (bool | Unset):  Default: True.
        timezone (None | str | Unset): IANA timezone for quiet hours and `|local` template filters (default: the
            `server` setting's timezone, else UTC) Example: America/Chicago.
        quiet_hours (None | QuietHours | Unset): Quiet hours in the channel timezone; null for none
        group_window_seconds (int | Unset): > 0 holds the first alert per (channel, rule) for the window and sends one
            grouped message Default: 0.
        backlog_cap (int | Unset): Pending sends above this are folded into one digest ("+N more alerts") Default: 20.
        edit_in_place (bool | Unset): Edit the original message on lifecycle updates where the type supports it
            (Discord, Telegram, Matrix) Default: True.
        defer_while_active_seconds (int | Unset): Defer non-critical alerts while a desktop client is active (033); for
            phone-type channels Default: 0.
        accept_exercise (bool | Unset): Receive exercise alerts Default: False.
        language (None | str | Unset): BCP 47 language for notifications on this channel (141), stored canonicalized
            (`pt-br` → `pt-BR`); null uses `notify.defaultLanguage`. When the primary language or script differs from it,
            title and body are translated through the 026 chat role (budget `translate`, low effort), code spans, URLs and
            placeholders untouched, cached 24 h; after 8 s, or on any failure, the original is sent with the title prefixed
            `[untranslated]`.
        global_ (bool | None | Unset): On create only (R2-009): true for a global channel (needs `channels.global`),
            false for a personal one (needs `self.channels`). Absent, a user holding `self.channels` gets a personal channel
            and anyone else a global one; an API token always creates global channels. Ignored on update.
    """

    name: str
    type_: ChannelInputType
    config: MapAttachmentConfig | Unset = UNSET
    secrets: ChannelInputSecrets | Unset = UNSET
    events: list[ChannelInputEventsItem] | Unset = UNSET
    rule_ids: list[UUID] | None | Unset = UNSET
    min_severity: ChannelInputMinSeverity | Unset = ChannelInputMinSeverity.INFO
    title_template: None | str | Unset = UNSET
    body_template: None | str | Unset = UNSET
    wait_for_enrichment_seconds: int | Unset = 0
    enabled: bool | Unset = True
    timezone: None | str | Unset = UNSET
    quiet_hours: None | QuietHours | Unset = UNSET
    group_window_seconds: int | Unset = 0
    backlog_cap: int | Unset = 20
    edit_in_place: bool | Unset = True
    defer_while_active_seconds: int | Unset = 0
    accept_exercise: bool | Unset = False
    language: None | str | Unset = UNSET
    global_: bool | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.quiet_hours import QuietHours

        name = self.name

        type_ = self.type_.value

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

        events: list[str] | Unset = UNSET
        if not isinstance(self.events, Unset):
            events = []
            for events_item_data in self.events:
                events_item = events_item_data.value
                events.append(events_item)

        rule_ids: list[str] | None | Unset
        if isinstance(self.rule_ids, Unset):
            rule_ids = UNSET
        elif isinstance(self.rule_ids, list):
            rule_ids = []
            for rule_ids_type_0_item_data in self.rule_ids:
                rule_ids_type_0_item = str(rule_ids_type_0_item_data)
                rule_ids.append(rule_ids_type_0_item)

        else:
            rule_ids = self.rule_ids

        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        title_template: None | str | Unset
        if isinstance(self.title_template, Unset):
            title_template = UNSET
        else:
            title_template = self.title_template

        body_template: None | str | Unset
        if isinstance(self.body_template, Unset):
            body_template = UNSET
        else:
            body_template = self.body_template

        wait_for_enrichment_seconds = self.wait_for_enrichment_seconds

        enabled = self.enabled

        timezone: None | str | Unset
        if isinstance(self.timezone, Unset):
            timezone = UNSET
        else:
            timezone = self.timezone

        quiet_hours: dict[str, Any] | None | Unset
        if isinstance(self.quiet_hours, Unset):
            quiet_hours = UNSET
        elif isinstance(self.quiet_hours, QuietHours):
            quiet_hours = self.quiet_hours.to_dict()
        else:
            quiet_hours = self.quiet_hours

        group_window_seconds = self.group_window_seconds

        backlog_cap = self.backlog_cap

        edit_in_place = self.edit_in_place

        defer_while_active_seconds = self.defer_while_active_seconds

        accept_exercise = self.accept_exercise

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        global_: bool | None | Unset
        if isinstance(self.global_, Unset):
            global_ = UNSET
        else:
            global_ = self.global_

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "type": type_,
            }
        )
        if config is not UNSET:
            field_dict["config"] = config
        if secrets is not UNSET:
            field_dict["secrets"] = secrets
        if events is not UNSET:
            field_dict["events"] = events
        if rule_ids is not UNSET:
            field_dict["ruleIds"] = rule_ids
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if title_template is not UNSET:
            field_dict["titleTemplate"] = title_template
        if body_template is not UNSET:
            field_dict["bodyTemplate"] = body_template
        if wait_for_enrichment_seconds is not UNSET:
            field_dict["waitForEnrichmentSeconds"] = wait_for_enrichment_seconds
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if quiet_hours is not UNSET:
            field_dict["quietHours"] = quiet_hours
        if group_window_seconds is not UNSET:
            field_dict["groupWindowSeconds"] = group_window_seconds
        if backlog_cap is not UNSET:
            field_dict["backlogCap"] = backlog_cap
        if edit_in_place is not UNSET:
            field_dict["editInPlace"] = edit_in_place
        if defer_while_active_seconds is not UNSET:
            field_dict["deferWhileActiveSeconds"] = defer_while_active_seconds
        if accept_exercise is not UNSET:
            field_dict["acceptExercise"] = accept_exercise
        if language is not UNSET:
            field_dict["language"] = language
        if global_ is not UNSET:
            field_dict["global"] = global_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.channel_input_secrets import ChannelInputSecrets
        from ..models.map_attachment_config import MapAttachmentConfig
        from ..models.quiet_hours import QuietHours

        d = dict(src_dict)
        name = d.pop("name")

        type_ = ChannelInputType(d.pop("type"))

        _config = d.pop("config", UNSET)
        config: MapAttachmentConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = MapAttachmentConfig.from_dict(_config)

        _secrets = d.pop("secrets", UNSET)
        secrets: ChannelInputSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = ChannelInputSecrets.from_dict(_secrets)

        _events = d.pop("events", UNSET)
        events: list[ChannelInputEventsItem] | Unset = UNSET
        if _events is not UNSET:
            events = []
            for events_item_data in _events:
                events_item = ChannelInputEventsItem(events_item_data)

                events.append(events_item)

        def _parse_rule_ids(data: object) -> list[UUID] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                rule_ids_type_0 = []
                _rule_ids_type_0 = data
                for rule_ids_type_0_item_data in _rule_ids_type_0:
                    rule_ids_type_0_item = UUID(rule_ids_type_0_item_data)

                    rule_ids_type_0.append(rule_ids_type_0_item)

                return rule_ids_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[UUID] | None | Unset, data)

        rule_ids = _parse_rule_ids(d.pop("ruleIds", UNSET))

        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: ChannelInputMinSeverity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = ChannelInputMinSeverity(_min_severity)

        def _parse_title_template(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title_template = _parse_title_template(d.pop("titleTemplate", UNSET))

        def _parse_body_template(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        body_template = _parse_body_template(d.pop("bodyTemplate", UNSET))

        wait_for_enrichment_seconds = d.pop("waitForEnrichmentSeconds", UNSET)

        enabled = d.pop("enabled", UNSET)

        def _parse_timezone(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        timezone = _parse_timezone(d.pop("timezone", UNSET))

        def _parse_quiet_hours(data: object) -> None | QuietHours | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quiet_hours_type_0 = QuietHours.from_dict(data)

                return quiet_hours_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | QuietHours | Unset, data)

        quiet_hours = _parse_quiet_hours(d.pop("quietHours", UNSET))

        group_window_seconds = d.pop("groupWindowSeconds", UNSET)

        backlog_cap = d.pop("backlogCap", UNSET)

        edit_in_place = d.pop("editInPlace", UNSET)

        defer_while_active_seconds = d.pop("deferWhileActiveSeconds", UNSET)

        accept_exercise = d.pop("acceptExercise", UNSET)

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        def _parse_global_(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        global_ = _parse_global_(d.pop("global", UNSET))

        channel_input = cls(
            name=name,
            type_=type_,
            config=config,
            secrets=secrets,
            events=events,
            rule_ids=rule_ids,
            min_severity=min_severity,
            title_template=title_template,
            body_template=body_template,
            wait_for_enrichment_seconds=wait_for_enrichment_seconds,
            enabled=enabled,
            timezone=timezone,
            quiet_hours=quiet_hours,
            group_window_seconds=group_window_seconds,
            backlog_cap=backlog_cap,
            edit_in_place=edit_in_place,
            defer_while_active_seconds=defer_while_active_seconds,
            accept_exercise=accept_exercise,
            language=language,
            global_=global_,
        )

        channel_input.additional_properties = d
        return channel_input

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
