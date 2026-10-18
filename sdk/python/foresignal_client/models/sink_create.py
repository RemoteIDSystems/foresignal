from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sink_create_type import SinkCreateType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_create_config import SinkCreateConfig
    from ..models.sink_create_secrets import SinkCreateSecrets
    from ..models.sink_filter import SinkFilter


T = TypeVar("T", bound="SinkCreate")


@_attrs_define
class SinkCreate:
    """Create an outbound sink; sinks are created disabled unless enabled=true (110)

    Attributes:
        id (str):
        type_ (SinkCreateType):
        name (str):
        config (SinkCreateConfig): Per-type settings validated against SinkType.configSchema. Common keys: `mode`
            (PUSH|SERVE where the type supports both), `bind` (default 127.0.0.1; loopback, RFC 1918, link-local, ULA, or
            100.64.0.0/10 with `allowTailnet`), `port` (required for SERVE, no default), `allowAllInterfaces`, `maxClients`
            (default 8), `maxPerSecond`; broker and target URLs pass the outbound URL policy (035).
        enabled (bool | Unset): Defaults to false Default: False.
        secrets (SinkCreateSecrets | Unset): Write-only secrets per SinkType.secretFields (encrypted at rest, never
            returned)
        filter_ (SinkFilter | Unset): Which records a sink receives (110)
    """

    id: str
    type_: SinkCreateType
    name: str
    config: SinkCreateConfig
    enabled: bool | Unset = False
    secrets: SinkCreateSecrets | Unset = UNSET
    filter_: SinkFilter | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        type_ = self.type_.value

        name = self.name

        config = self.config.to_dict()

        enabled = self.enabled

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

        filter_: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filter_, Unset):
            filter_ = self.filter_.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "name": name,
                "config": config,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if secrets is not UNSET:
            field_dict["secrets"] = secrets
        if filter_ is not UNSET:
            field_dict["filter"] = filter_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_create_config import SinkCreateConfig
        from ..models.sink_create_secrets import SinkCreateSecrets
        from ..models.sink_filter import SinkFilter

        d = dict(src_dict)
        id = d.pop("id")

        type_ = SinkCreateType(d.pop("type"))

        name = d.pop("name")

        config = SinkCreateConfig.from_dict(d.pop("config"))

        enabled = d.pop("enabled", UNSET)

        _secrets = d.pop("secrets", UNSET)
        secrets: SinkCreateSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = SinkCreateSecrets.from_dict(_secrets)

        _filter_ = d.pop("filter", UNSET)
        filter_: SinkFilter | Unset
        if isinstance(_filter_, Unset):
            filter_ = UNSET
        else:
            filter_ = SinkFilter.from_dict(_filter_)

        sink_create = cls(
            id=id,
            type_=type_,
            name=name,
            config=config,
            enabled=enabled,
            secrets=secrets,
            filter_=filter_,
        )

        sink_create.additional_properties = d
        return sink_create

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
