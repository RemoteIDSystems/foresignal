from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_filter import SinkFilter
    from ..models.sink_update_config import SinkUpdateConfig
    from ..models.sink_update_secrets_type_0 import SinkUpdateSecretsType0


T = TypeVar("T", bound="SinkUpdate")


@_attrs_define
class SinkUpdate:
    """Replace a sink's settings. Secrets are merged, not replaced: a field with a value sets it, a field set to null
    removes it, an omitted field keeps its stored value, and `"secrets": null` removes every secret (110)

        Attributes:
            name (str):
            config (SinkUpdateConfig): Per-type settings validated against SinkType.configSchema. Common keys: `mode`
                (PUSH|SERVE where the type supports both), `bind` (default 127.0.0.1; loopback, RFC 1918, link-local, ULA, or
                100.64.0.0/10 with `allowTailnet`), `port` (required for SERVE, no default), `allowAllInterfaces`, `maxClients`
                (default 8), `maxPerSecond`; broker and target URLs pass the outbound URL policy (035).
            enabled (bool | Unset):
            secrets (None | SinkUpdateSecretsType0 | Unset):
            filter_ (SinkFilter | Unset): Which records a sink receives (110)
    """

    name: str
    config: SinkUpdateConfig
    enabled: bool | Unset = UNSET
    secrets: None | SinkUpdateSecretsType0 | Unset = UNSET
    filter_: SinkFilter | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.sink_update_secrets_type_0 import SinkUpdateSecretsType0

        name = self.name

        config = self.config.to_dict()

        enabled = self.enabled

        secrets: dict[str, Any] | None | Unset
        if isinstance(self.secrets, Unset):
            secrets = UNSET
        elif isinstance(self.secrets, SinkUpdateSecretsType0):
            secrets = self.secrets.to_dict()
        else:
            secrets = self.secrets

        filter_: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filter_, Unset):
            filter_ = self.filter_.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
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
        from ..models.sink_filter import SinkFilter
        from ..models.sink_update_config import SinkUpdateConfig
        from ..models.sink_update_secrets_type_0 import SinkUpdateSecretsType0

        d = dict(src_dict)
        name = d.pop("name")

        config = SinkUpdateConfig.from_dict(d.pop("config"))

        enabled = d.pop("enabled", UNSET)

        def _parse_secrets(data: object) -> None | SinkUpdateSecretsType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                secrets_type_0 = SinkUpdateSecretsType0.from_dict(data)

                return secrets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SinkUpdateSecretsType0 | Unset, data)

        secrets = _parse_secrets(d.pop("secrets", UNSET))

        _filter_ = d.pop("filter", UNSET)
        filter_: SinkFilter | Unset
        if isinstance(_filter_, Unset):
            filter_ = UNSET
        else:
            filter_ = SinkFilter.from_dict(_filter_)

        sink_update = cls(
            name=name,
            config=config,
            enabled=enabled,
            secrets=secrets,
            filter_=filter_,
        )

        sink_update.additional_properties = d
        return sink_update

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
