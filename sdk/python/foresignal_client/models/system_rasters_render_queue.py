from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemRastersRenderQueue")


@_attrs_define
class SystemRastersRenderQueue:
    """Render queue gauges (045)

    Attributes:
        depth (int | Unset):
        active (int | Unset):
        pool_size (int | Unset):
    """

    depth: int | Unset = UNSET
    active: int | Unset = UNSET
    pool_size: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        depth = self.depth

        active = self.active

        pool_size = self.pool_size

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if depth is not UNSET:
            field_dict["depth"] = depth
        if active is not UNSET:
            field_dict["active"] = active
        if pool_size is not UNSET:
            field_dict["poolSize"] = pool_size

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        depth = d.pop("depth", UNSET)

        active = d.pop("active", UNSET)

        pool_size = d.pop("poolSize", UNSET)

        system_rasters_render_queue = cls(
            depth=depth,
            active=active,
            pool_size=pool_size,
        )

        system_rasters_render_queue.additional_properties = d
        return system_rasters_render_queue

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
