from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mqtt_channel_config_protocol_version import (
    MqttChannelConfigProtocolVersion,
)
from ..models.mqtt_channel_config_qos import MqttChannelConfigQos
from ..types import UNSET, Unset

T = TypeVar("T", bound="MqttChannelConfig")


@_attrs_define
class MqttChannelConfig:
    """`ChannelInput.config` for type `mqtt` (060). Secrets (in `secrets`): `username`, `password`, `caCertPem`. A public
    broker without TLS or credentials yields the warning `MQTT_INSECURE_PUBLIC_BROKER` on the channel.

        Attributes:
            broker_url (str): mqtt://, mqtts://, ws:// or wss:// (WebSocket URLs may carry a path); no credentials, query or
                fragment. The host passes the outbound URL policy (profile LAN_ALLOWED) Example: mqtts://broker.lan:8883.
            topic_prefix (str | Unset): Topic prefix; leading and trailing slashes are dropped. No + or # wildcards Default:
                'foresignal'.
            qos (MqttChannelConfigQos | Unset): QoS of event messages; a delivery succeeds on PUBACK (1) or a completed
                write (0). Retained state and discovery always use QoS 1 Default: MqttChannelConfigQos.VALUE_1.
            protocol_version (MqttChannelConfigProtocolVersion | Unset):  Default: MqttChannelConfigProtocolVersion.VALUE_0.
            message_expiry (str | Unset): MQTT 5 message expiry of non-retained event messages (ignored on 3.1.1). ISO-8601
                duration or shorthand Default: '24h'. Example: 60s.
            payload_template (None | str | Unset): Optional JSON with JSON-escaped {{…}} placeholders, like webhook
                `bodyTemplate`; replaces every event payload except the channel test
            publish_health (bool | Unset):  Default: True.
            publish_summary (bool | Unset):  Default: True.
            summary_interval (str | Unset): How often `{prefix}/summary` is checked (at least 10s); it is published only
                when the glance changed. ISO-8601 duration (PT1M) or shorthand (60s, 15m) Default: '60s'. Example: 60s.
            ha_discovery (bool | Unset):  Default: False.
            ha_prefix (str | Unset): Home Assistant discovery prefix; Foresignal subscribes to `{haPrefix}/status` for the
                birth message Default: 'homeassistant'.
            ha_max_rules (int | Unset): Most enabled rules given a `problem` binary_sensor, critical first, then by name
                Default: 50.
            ha_max_sources (int | Unset): Most non-private, non-disabled sources given a `problem` binary_sensor, by id
                Default: 100.
            ha_retain_discovery (bool | Unset): Retain the discovery config. Off by default: Home Assistant prefers
                discovery re-published on its birth message, because retained configs can leave ghost entities Default: False.
    """

    broker_url: str
    topic_prefix: str | Unset = "foresignal"
    qos: MqttChannelConfigQos | Unset = MqttChannelConfigQos.VALUE_1
    protocol_version: MqttChannelConfigProtocolVersion | Unset = (
        MqttChannelConfigProtocolVersion.VALUE_0
    )
    message_expiry: str | Unset = "24h"
    payload_template: None | str | Unset = UNSET
    publish_health: bool | Unset = True
    publish_summary: bool | Unset = True
    summary_interval: str | Unset = "60s"
    ha_discovery: bool | Unset = False
    ha_prefix: str | Unset = "homeassistant"
    ha_max_rules: int | Unset = 50
    ha_max_sources: int | Unset = 100
    ha_retain_discovery: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        broker_url = self.broker_url

        topic_prefix = self.topic_prefix

        qos: int | Unset = UNSET
        if not isinstance(self.qos, Unset):
            qos = self.qos.value

        protocol_version: str | Unset = UNSET
        if not isinstance(self.protocol_version, Unset):
            protocol_version = self.protocol_version.value

        message_expiry = self.message_expiry

        payload_template: None | str | Unset
        if isinstance(self.payload_template, Unset):
            payload_template = UNSET
        else:
            payload_template = self.payload_template

        publish_health = self.publish_health

        publish_summary = self.publish_summary

        summary_interval = self.summary_interval

        ha_discovery = self.ha_discovery

        ha_prefix = self.ha_prefix

        ha_max_rules = self.ha_max_rules

        ha_max_sources = self.ha_max_sources

        ha_retain_discovery = self.ha_retain_discovery

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "brokerUrl": broker_url,
            }
        )
        if topic_prefix is not UNSET:
            field_dict["topicPrefix"] = topic_prefix
        if qos is not UNSET:
            field_dict["qos"] = qos
        if protocol_version is not UNSET:
            field_dict["protocolVersion"] = protocol_version
        if message_expiry is not UNSET:
            field_dict["messageExpiry"] = message_expiry
        if payload_template is not UNSET:
            field_dict["payloadTemplate"] = payload_template
        if publish_health is not UNSET:
            field_dict["publishHealth"] = publish_health
        if publish_summary is not UNSET:
            field_dict["publishSummary"] = publish_summary
        if summary_interval is not UNSET:
            field_dict["summaryInterval"] = summary_interval
        if ha_discovery is not UNSET:
            field_dict["haDiscovery"] = ha_discovery
        if ha_prefix is not UNSET:
            field_dict["haPrefix"] = ha_prefix
        if ha_max_rules is not UNSET:
            field_dict["haMaxRules"] = ha_max_rules
        if ha_max_sources is not UNSET:
            field_dict["haMaxSources"] = ha_max_sources
        if ha_retain_discovery is not UNSET:
            field_dict["haRetainDiscovery"] = ha_retain_discovery

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        broker_url = d.pop("brokerUrl")

        topic_prefix = d.pop("topicPrefix", UNSET)

        _qos = d.pop("qos", UNSET)
        qos: MqttChannelConfigQos | Unset
        if isinstance(_qos, Unset):
            qos = UNSET
        else:
            qos = MqttChannelConfigQos(_qos)

        _protocol_version = d.pop("protocolVersion", UNSET)
        protocol_version: MqttChannelConfigProtocolVersion | Unset
        if isinstance(_protocol_version, Unset):
            protocol_version = UNSET
        else:
            protocol_version = MqttChannelConfigProtocolVersion(_protocol_version)

        message_expiry = d.pop("messageExpiry", UNSET)

        def _parse_payload_template(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        payload_template = _parse_payload_template(d.pop("payloadTemplate", UNSET))

        publish_health = d.pop("publishHealth", UNSET)

        publish_summary = d.pop("publishSummary", UNSET)

        summary_interval = d.pop("summaryInterval", UNSET)

        ha_discovery = d.pop("haDiscovery", UNSET)

        ha_prefix = d.pop("haPrefix", UNSET)

        ha_max_rules = d.pop("haMaxRules", UNSET)

        ha_max_sources = d.pop("haMaxSources", UNSET)

        ha_retain_discovery = d.pop("haRetainDiscovery", UNSET)

        mqtt_channel_config = cls(
            broker_url=broker_url,
            topic_prefix=topic_prefix,
            qos=qos,
            protocol_version=protocol_version,
            message_expiry=message_expiry,
            payload_template=payload_template,
            publish_health=publish_health,
            publish_summary=publish_summary,
            summary_interval=summary_interval,
            ha_discovery=ha_discovery,
            ha_prefix=ha_prefix,
            ha_max_rules=ha_max_rules,
            ha_max_sources=ha_max_sources,
            ha_retain_discovery=ha_retain_discovery,
        )

        mqtt_channel_config.additional_properties = d
        return mqtt_channel_config

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
