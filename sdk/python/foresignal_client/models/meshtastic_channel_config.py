from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mesh_channel_common_config_airtime_preset import (
    MeshChannelCommonConfigAirtimePreset,
)
from ..models.mesh_channel_common_config_min_severity import (
    MeshChannelCommonConfigMinSeverity,
)
from ..models.mesh_channel_common_config_profile import MeshChannelCommonConfigProfile
from ..models.mesh_channel_common_config_region import MeshChannelCommonConfigRegion
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mesh_channel_common_config_airtime_custom_type_0 import (
        MeshChannelCommonConfigAirtimeCustomType0,
    )
    from ..models.mesh_channel_common_config_allowed_senders import (
        MeshChannelCommonConfigAllowedSenders,
    )


T = TypeVar("T", bound="MeshtasticChannelConfig")


@_attrs_define
class MeshtasticChannelConfig:
    """`ChannelInput.config` for type `meshtastic`: MQTT JSON downlink to the operator's node on a private channel (084).
    Secrets: `username`, `password`. Public channels are refused with 400 `MESH_PUBLIC_CHANNEL`.

        Attributes:
            region (MeshChannelCommonConfigRegion):
            broker_url (str): LAN_URL broker (never *.meshtastic.org)
            gateway_node_num (str): Unsigned decimal node number used as `from`
            channel_index (int):
            channel_name (str):
            private_channel_confirmed (bool): Must be true
            airtime_preset (MeshChannelCommonConfigAirtimePreset | Unset): Default meshtastic-longfast for `meshtastic`,
                custom for `meshcore`; the operator confirms it matches the node
            airtime_custom (MeshChannelCommonConfigAirtimeCustomType0 | None | Unset): Radio settings when
                `airtimePreset=custom` (required then; the default preset of `meshcore`)
            duty_cycle_percent (float | Unset): Must not exceed the region's regulatory ceiling Default: 1.0.
            min_spacing (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Default: '30s'. Example:
                60s.
            max_messages_per_hour (int | Unset):  Default: 20.
            profile (MeshChannelCommonConfigProfile | Unset):  Default: MeshChannelCommonConfigProfile.TEXT.
            min_severity (MeshChannelCommonConfigMinSeverity | Unset): The channel takes nothing below this severity,
                whatever its top-level minSeverity Default: MeshChannelCommonConfigMinSeverity.WARNING.
            reports_enabled (bool | Unset):  Default: False.
            allowed_senders (MeshChannelCommonConfigAllowedSenders | Unset): Sender to role. A sender is a Meshtastic node
                id (`!xxxxxxxx`), a MeshCore key prefix (`mc:` + 12 hex) or `name:` + the advertised name: MeshCore channel
                messages name their sender only by its advertised name, so a MeshCore channel report is matched by `name:` (084)
            compact_events (bool | Unset): Also send the federation compact messages (fired and resolved alerts,
                intelligence events) within this channel's airtime budget, through its profile (113) Default: False.
            root (str | Unset):  Default: 'msh/local'.
            to_node_num (None | str | Unset): Direct message target
            topic_suffix_node_id (bool | Unset):  Default: False.
            licensed_mode (bool | Unset): Must be false (ham mode disables encryption) Default: False.
    """

    region: MeshChannelCommonConfigRegion
    broker_url: str
    gateway_node_num: str
    channel_index: int
    channel_name: str
    private_channel_confirmed: bool
    airtime_preset: MeshChannelCommonConfigAirtimePreset | Unset = UNSET
    airtime_custom: MeshChannelCommonConfigAirtimeCustomType0 | None | Unset = UNSET
    duty_cycle_percent: float | Unset = 1.0
    min_spacing: str | Unset = "30s"
    max_messages_per_hour: int | Unset = 20
    profile: MeshChannelCommonConfigProfile | Unset = (
        MeshChannelCommonConfigProfile.TEXT
    )
    min_severity: MeshChannelCommonConfigMinSeverity | Unset = (
        MeshChannelCommonConfigMinSeverity.WARNING
    )
    reports_enabled: bool | Unset = False
    allowed_senders: MeshChannelCommonConfigAllowedSenders | Unset = UNSET
    compact_events: bool | Unset = False
    root: str | Unset = "msh/local"
    to_node_num: None | str | Unset = UNSET
    topic_suffix_node_id: bool | Unset = False
    licensed_mode: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.mesh_channel_common_config_airtime_custom_type_0 import (
            MeshChannelCommonConfigAirtimeCustomType0,
        )

        region = self.region.value

        broker_url = self.broker_url

        gateway_node_num = self.gateway_node_num

        channel_index = self.channel_index

        channel_name = self.channel_name

        private_channel_confirmed = self.private_channel_confirmed

        airtime_preset: str | Unset = UNSET
        if not isinstance(self.airtime_preset, Unset):
            airtime_preset = self.airtime_preset.value

        airtime_custom: dict[str, Any] | None | Unset
        if isinstance(self.airtime_custom, Unset):
            airtime_custom = UNSET
        elif isinstance(self.airtime_custom, MeshChannelCommonConfigAirtimeCustomType0):
            airtime_custom = self.airtime_custom.to_dict()
        else:
            airtime_custom = self.airtime_custom

        duty_cycle_percent = self.duty_cycle_percent

        min_spacing = self.min_spacing

        max_messages_per_hour = self.max_messages_per_hour

        profile: str | Unset = UNSET
        if not isinstance(self.profile, Unset):
            profile = self.profile.value

        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        reports_enabled = self.reports_enabled

        allowed_senders: dict[str, Any] | Unset = UNSET
        if not isinstance(self.allowed_senders, Unset):
            allowed_senders = self.allowed_senders.to_dict()

        compact_events = self.compact_events

        root = self.root

        to_node_num: None | str | Unset
        if isinstance(self.to_node_num, Unset):
            to_node_num = UNSET
        else:
            to_node_num = self.to_node_num

        topic_suffix_node_id = self.topic_suffix_node_id

        licensed_mode = self.licensed_mode

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "region": region,
                "brokerUrl": broker_url,
                "gatewayNodeNum": gateway_node_num,
                "channelIndex": channel_index,
                "channelName": channel_name,
                "privateChannelConfirmed": private_channel_confirmed,
            }
        )
        if airtime_preset is not UNSET:
            field_dict["airtimePreset"] = airtime_preset
        if airtime_custom is not UNSET:
            field_dict["airtimeCustom"] = airtime_custom
        if duty_cycle_percent is not UNSET:
            field_dict["dutyCyclePercent"] = duty_cycle_percent
        if min_spacing is not UNSET:
            field_dict["minSpacing"] = min_spacing
        if max_messages_per_hour is not UNSET:
            field_dict["maxMessagesPerHour"] = max_messages_per_hour
        if profile is not UNSET:
            field_dict["profile"] = profile
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if reports_enabled is not UNSET:
            field_dict["reportsEnabled"] = reports_enabled
        if allowed_senders is not UNSET:
            field_dict["allowedSenders"] = allowed_senders
        if compact_events is not UNSET:
            field_dict["compactEvents"] = compact_events
        if root is not UNSET:
            field_dict["root"] = root
        if to_node_num is not UNSET:
            field_dict["toNodeNum"] = to_node_num
        if topic_suffix_node_id is not UNSET:
            field_dict["topicSuffixNodeId"] = topic_suffix_node_id
        if licensed_mode is not UNSET:
            field_dict["licensedMode"] = licensed_mode

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mesh_channel_common_config_airtime_custom_type_0 import (
            MeshChannelCommonConfigAirtimeCustomType0,
        )
        from ..models.mesh_channel_common_config_allowed_senders import (
            MeshChannelCommonConfigAllowedSenders,
        )

        d = dict(src_dict)
        region = MeshChannelCommonConfigRegion(d.pop("region"))

        broker_url = d.pop("brokerUrl")

        gateway_node_num = d.pop("gatewayNodeNum")

        channel_index = d.pop("channelIndex")

        channel_name = d.pop("channelName")

        private_channel_confirmed = d.pop("privateChannelConfirmed")

        _airtime_preset = d.pop("airtimePreset", UNSET)
        airtime_preset: MeshChannelCommonConfigAirtimePreset | Unset
        if isinstance(_airtime_preset, Unset):
            airtime_preset = UNSET
        else:
            airtime_preset = MeshChannelCommonConfigAirtimePreset(_airtime_preset)

        def _parse_airtime_custom(
            data: object,
        ) -> MeshChannelCommonConfigAirtimeCustomType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                airtime_custom_type_0 = (
                    MeshChannelCommonConfigAirtimeCustomType0.from_dict(data)
                )

                return airtime_custom_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MeshChannelCommonConfigAirtimeCustomType0 | None | Unset, data)

        airtime_custom = _parse_airtime_custom(d.pop("airtimeCustom", UNSET))

        duty_cycle_percent = d.pop("dutyCyclePercent", UNSET)

        min_spacing = d.pop("minSpacing", UNSET)

        max_messages_per_hour = d.pop("maxMessagesPerHour", UNSET)

        _profile = d.pop("profile", UNSET)
        profile: MeshChannelCommonConfigProfile | Unset
        if isinstance(_profile, Unset):
            profile = UNSET
        else:
            profile = MeshChannelCommonConfigProfile(_profile)

        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: MeshChannelCommonConfigMinSeverity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = MeshChannelCommonConfigMinSeverity(_min_severity)

        reports_enabled = d.pop("reportsEnabled", UNSET)

        _allowed_senders = d.pop("allowedSenders", UNSET)
        allowed_senders: MeshChannelCommonConfigAllowedSenders | Unset
        if isinstance(_allowed_senders, Unset):
            allowed_senders = UNSET
        else:
            allowed_senders = MeshChannelCommonConfigAllowedSenders.from_dict(
                _allowed_senders
            )

        compact_events = d.pop("compactEvents", UNSET)

        root = d.pop("root", UNSET)

        def _parse_to_node_num(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        to_node_num = _parse_to_node_num(d.pop("toNodeNum", UNSET))

        topic_suffix_node_id = d.pop("topicSuffixNodeId", UNSET)

        licensed_mode = d.pop("licensedMode", UNSET)

        meshtastic_channel_config = cls(
            region=region,
            broker_url=broker_url,
            gateway_node_num=gateway_node_num,
            channel_index=channel_index,
            channel_name=channel_name,
            private_channel_confirmed=private_channel_confirmed,
            airtime_preset=airtime_preset,
            airtime_custom=airtime_custom,
            duty_cycle_percent=duty_cycle_percent,
            min_spacing=min_spacing,
            max_messages_per_hour=max_messages_per_hour,
            profile=profile,
            min_severity=min_severity,
            reports_enabled=reports_enabled,
            allowed_senders=allowed_senders,
            compact_events=compact_events,
            root=root,
            to_node_num=to_node_num,
            topic_suffix_node_id=topic_suffix_node_id,
            licensed_mode=licensed_mode,
        )

        meshtastic_channel_config.additional_properties = d
        return meshtastic_channel_config

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
