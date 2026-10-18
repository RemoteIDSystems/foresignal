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


T = TypeVar("T", bound="MeshChannelCommonConfig")


@_attrs_define
class MeshChannelCommonConfig:
    """Fields shared by the `meshtastic` and `meshcore` channel configs: airtime governor, profile and field-report
    settings (084)

        Attributes:
            region (MeshChannelCommonConfigRegion):
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
    """

    region: MeshChannelCommonConfigRegion
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
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.mesh_channel_common_config_airtime_custom_type_0 import (
            MeshChannelCommonConfigAirtimeCustomType0,
        )

        region = self.region.value

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "region": region,
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

        mesh_channel_common_config = cls(
            region=region,
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
        )

        mesh_channel_common_config.additional_properties = d
        return mesh_channel_common_config

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
