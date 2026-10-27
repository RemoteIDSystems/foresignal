from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EgressSettings")


@_attrs_define
class EgressSettings:
    """Egress routing policy (settings key `egress`); default direct/off, opt-in (C1/181)

    Attributes:
        rotation (str): How outbound requests pick a route; direct means no egress routing at all: pins and the default
            route are ignored and every pull goes direct (R2-018) Default: 'direct'.
        default_route (None | Unset | UUID): With rotation on, the route a source excluded from rotation uses (an
            excluded source with no default route goes direct); it must name an existing route (R2-018)
    """

    rotation: str = "direct"
    default_route: None | Unset | UUID = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rotation = self.rotation

        default_route: None | str | Unset
        if isinstance(self.default_route, Unset):
            default_route = UNSET
        elif isinstance(self.default_route, UUID):
            default_route = str(self.default_route)
        else:
            default_route = self.default_route

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rotation": rotation,
            }
        )
        if default_route is not UNSET:
            field_dict["defaultRoute"] = default_route

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        rotation = d.pop("rotation")

        def _parse_default_route(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                default_route_type_0 = UUID(data)

                return default_route_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        default_route = _parse_default_route(d.pop("defaultRoute", UNSET))

        egress_settings = cls(
            rotation=rotation,
            default_route=default_route,
        )

        egress_settings.additional_properties = d
        return egress_settings

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
