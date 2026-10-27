from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.egress_health import EgressHealth


T = TypeVar("T", bound="EgressRoute")


@_attrs_define
class EgressRoute:
    """An egress route: a proxy, VPN profile or bound interface the collector can present as an exit (C1/181); secrets are
    write-only

        Attributes:
            id (UUID):
            name (str):
            type_ (str):
            enabled (bool):  Default: True.
            url (None | str | Unset): Proxy URL (scheme://host:port); credentials are not shown here
            bound_interface (None | str | Unset): Host interface or source IP to bind (type interface)
            exit_ip (None | str | Unset): Last observed public exit IP from a test call
            health (EgressHealth | None | Unset):
            secrets_set (list[str] | Unset): Names of secrets that have a stored value
            created_at (datetime.datetime | Unset):
            updated_at (datetime.datetime | Unset):
            available (bool | Unset): Whether the server can send through this route now: false for a wireguard route (the
                server cannot bring a tunnel up), a disabled route, an interface it cannot find, or secrets it cannot open
                (R2-018)
            unavailable_reason (None | str | Unset): Why the route is unavailable; null when available
    """

    id: UUID
    name: str
    type_: str
    enabled: bool = True
    url: None | str | Unset = UNSET
    bound_interface: None | str | Unset = UNSET
    exit_ip: None | str | Unset = UNSET
    health: EgressHealth | None | Unset = UNSET
    secrets_set: list[str] | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    available: bool | Unset = UNSET
    unavailable_reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.egress_health import EgressHealth

        id = str(self.id)

        name = self.name

        type_ = self.type_

        enabled = self.enabled

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        bound_interface: None | str | Unset
        if isinstance(self.bound_interface, Unset):
            bound_interface = UNSET
        else:
            bound_interface = self.bound_interface

        exit_ip: None | str | Unset
        if isinstance(self.exit_ip, Unset):
            exit_ip = UNSET
        else:
            exit_ip = self.exit_ip

        health: dict[str, Any] | None | Unset
        if isinstance(self.health, Unset):
            health = UNSET
        elif isinstance(self.health, EgressHealth):
            health = self.health.to_dict()
        else:
            health = self.health

        secrets_set: list[str] | Unset = UNSET
        if not isinstance(self.secrets_set, Unset):
            secrets_set = self.secrets_set

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        available = self.available

        unavailable_reason: None | str | Unset
        if isinstance(self.unavailable_reason, Unset):
            unavailable_reason = UNSET
        else:
            unavailable_reason = self.unavailable_reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "type": type_,
                "enabled": enabled,
            }
        )
        if url is not UNSET:
            field_dict["url"] = url
        if bound_interface is not UNSET:
            field_dict["boundInterface"] = bound_interface
        if exit_ip is not UNSET:
            field_dict["exitIp"] = exit_ip
        if health is not UNSET:
            field_dict["health"] = health
        if secrets_set is not UNSET:
            field_dict["secretsSet"] = secrets_set
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if available is not UNSET:
            field_dict["available"] = available
        if unavailable_reason is not UNSET:
            field_dict["unavailableReason"] = unavailable_reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.egress_health import EgressHealth

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        name = d.pop("name")

        type_ = d.pop("type")

        enabled = d.pop("enabled")

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_bound_interface(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        bound_interface = _parse_bound_interface(d.pop("boundInterface", UNSET))

        def _parse_exit_ip(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        exit_ip = _parse_exit_ip(d.pop("exitIp", UNSET))

        def _parse_health(data: object) -> EgressHealth | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                health_type_0 = EgressHealth.from_dict(data)

                return health_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EgressHealth | None | Unset, data)

        health = _parse_health(d.pop("health", UNSET))

        secrets_set = cast(list[str], d.pop("secretsSet", UNSET))

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        available = d.pop("available", UNSET)

        def _parse_unavailable_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unavailable_reason = _parse_unavailable_reason(
            d.pop("unavailableReason", UNSET)
        )

        egress_route = cls(
            id=id,
            name=name,
            type_=type_,
            enabled=enabled,
            url=url,
            bound_interface=bound_interface,
            exit_ip=exit_ip,
            health=health,
            secrets_set=secrets_set,
            created_at=created_at,
            updated_at=updated_at,
            available=available,
            unavailable_reason=unavailable_reason,
        )

        egress_route.additional_properties = d
        return egress_route

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
