from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_source_info_access_fragility import EdpSourceInfoAccessFragility
from ..models.edp_source_info_access_route import EdpSourceInfoAccessRoute
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpSourceInfoAccess")


@_attrs_define
class EdpSourceInfoAccess:
    """
    Attributes:
        route (EdpSourceInfoAccessRoute):
        fragility (EdpSourceInfoAccessFragility | Unset):
    """

    route: EdpSourceInfoAccessRoute
    fragility: EdpSourceInfoAccessFragility | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        route = self.route.value

        fragility: str | Unset = UNSET
        if not isinstance(self.fragility, Unset):
            fragility = self.fragility.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "route": route,
            }
        )
        if fragility is not UNSET:
            field_dict["fragility"] = fragility

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        route = EdpSourceInfoAccessRoute(d.pop("route"))

        _fragility = d.pop("fragility", UNSET)
        fragility: EdpSourceInfoAccessFragility | Unset
        if isinstance(_fragility, Unset):
            fragility = UNSET
        else:
            fragility = EdpSourceInfoAccessFragility(_fragility)

        edp_source_info_access = cls(
            route=route,
            fragility=fragility,
        )

        edp_source_info_access.additional_properties = d
        return edp_source_info_access

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
