from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="AggregatorConfigConfig")


@_attrs_define
class AggregatorConfigConfig:
    """Domain-specific settings; an unknown member is a 400 (R2-019). adsb: `band`, 1090 or 978, default 1090. ais
    (R2-020): `allowSenders`, a list of IPs or CIDRs in private space whose datagrams are read, required with a udp://
    endpoint; `stationName`, the observer of every record, default `local`. datalink (R2-021): the decoders `acars`,
    `vdlm2`, `hfdl` (acars_router's TCP servers) and `aprs` (the igate's KISS TCP port), each a port on the endpoint
    host or a whole tcp://host:port, at least one to enable; `stationId` (ACARS observer), `stationCallsign` (APRS
    observer), `includeMessages` (boolean). satellite (R2-022): `location` (required with an endpoint: lat,lon[,altM] or
    a place id), `inboxPath` (SatDump's output directory under FORESIGNAL_INBOX_DIR), `satdumpVersion` (1.2 or 2.0),
    `predictNorad` (NORAD numbers of the polar satellites to predict), `goes` (east or west: the GOES the dish receives,
    whose ABI imagery pull is superseded). ground-sensors (R2-023): one object per sensor, each with `enabled` and at
    least one enabled to enable: `seismic` and `same` (`source`, the custom push source the bridge posts to),
    `radiosonde` (`endpoint` udp://bindHost:port, `allowSenders`, `stationCallsign`; supersedes sondehub), `airq`
    (`deviceType` purpleair, airrohr or airgradient, `endpoint`, `lat`, `lon`, `indoor`, `name`; supersedes openaq and
    sensor-community), `pws` (`station` davis or ecowitt, `endpoint`, `lat`, `lon`), `mesh` (`meshtastic` mqtt:// broker
    with `root`, supersedes meshtastic-mqtt and meshtastic-map; `meshcore` tcp:// companion; `bridge` boolean)

    """

    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        aggregator_config_config = cls()

        aggregator_config_config.additional_properties = d
        return aggregator_config_config

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
