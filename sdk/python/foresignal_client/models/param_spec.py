from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.param_spec_type import ParamSpecType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ParamSpec")


@_attrs_define
class ParamSpec:
    """
    Attributes:
        name (str):
        type_ (ParamSpecType):
        description (str | Unset):
        default (Any | Unset):
        required (bool | Unset):
        allowed (list[str] | Unset): When non-empty, the only accepted values. For type lan-url it lists the accepted
            URL schemes instead (069): empty means http, https, ws and wss; tcp (tcp://host:port, resolved once to a
            loopback, RFC 1918 or unique-local address and pinned) and udp (udp://bindHost:port, bindHost 0.0.0.0, :: or a
            local interface address, with the required string-list companion allowSenders of private IPs or CIDRs) are
            accepted only when listed; so are mqtt and mqtts (083: an operator's broker, mqtt://host:port or
            mqtts://host:port, pinned to a private address like tcp). The port of a tcp, udp, mqtt or mqtts URL is always
            explicit.
    """

    name: str
    type_: ParamSpecType
    description: str | Unset = UNSET
    default: Any | Unset = UNSET
    required: bool | Unset = UNSET
    allowed: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        type_ = self.type_.value

        description = self.description

        default = self.default

        required = self.required

        allowed: list[str] | Unset = UNSET
        if not isinstance(self.allowed, Unset):
            allowed = self.allowed

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "type": type_,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if default is not UNSET:
            field_dict["default"] = default
        if required is not UNSET:
            field_dict["required"] = required
        if allowed is not UNSET:
            field_dict["allowed"] = allowed

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        type_ = ParamSpecType(d.pop("type"))

        description = d.pop("description", UNSET)

        default = d.pop("default", UNSET)

        required = d.pop("required", UNSET)

        allowed = cast(list[str], d.pop("allowed", UNSET))

        param_spec = cls(
            name=name,
            type_=type_,
            description=description,
            default=default,
            required=required,
            allowed=allowed,
        )

        param_spec.additional_properties = d
        return param_spec

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
