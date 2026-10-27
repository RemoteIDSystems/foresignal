from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SourceEgressAssignment")


@_attrs_define
class SourceEgressAssignment:
    """Per-source egress assignment (C1/181)

    Attributes:
        route_id (None | Unset | UUID): Pin the source to this route (with rotation on); null uses the rotation. A
            pinned route that is unusable fails the pull rather than moving it (R2-018)
        exclude_from_rotation (bool | Unset): With rotation on and no pin, keep the source on the default route, or
            direct when there is none (R2-018) Default: False.
    """

    route_id: None | Unset | UUID = UNSET
    exclude_from_rotation: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        route_id: None | str | Unset
        if isinstance(self.route_id, Unset):
            route_id = UNSET
        elif isinstance(self.route_id, UUID):
            route_id = str(self.route_id)
        else:
            route_id = self.route_id

        exclude_from_rotation = self.exclude_from_rotation

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if route_id is not UNSET:
            field_dict["routeId"] = route_id
        if exclude_from_rotation is not UNSET:
            field_dict["excludeFromRotation"] = exclude_from_rotation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_route_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                route_id_type_0 = UUID(data)

                return route_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        route_id = _parse_route_id(d.pop("routeId", UNSET))

        exclude_from_rotation = d.pop("excludeFromRotation", UNSET)

        source_egress_assignment = cls(
            route_id=route_id,
            exclude_from_rotation=exclude_from_rotation,
        )

        source_egress_assignment.additional_properties = d
        return source_egress_assignment

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
