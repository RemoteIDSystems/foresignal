from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AutoResolve")


@_attrs_define
class AutoResolve:
    """Automatic alert resolution (default all on for enter rules, off for match; not allowed on exit rules)

    Attributes:
        on_inverse_edge (bool | Unset):  Default: True.
        on_tombstone (bool | Unset):  Default: True.
        on_expiry (bool | Unset):  Default: True.
        stale_after (None | str | Unset): Resolve when the entity has not been observed for this long. ISO-8601 duration
            (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 2h.
    """

    on_inverse_edge: bool | Unset = True
    on_tombstone: bool | Unset = True
    on_expiry: bool | Unset = True
    stale_after: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        on_inverse_edge = self.on_inverse_edge

        on_tombstone = self.on_tombstone

        on_expiry = self.on_expiry

        stale_after: None | str | Unset
        if isinstance(self.stale_after, Unset):
            stale_after = UNSET
        else:
            stale_after = self.stale_after

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if on_inverse_edge is not UNSET:
            field_dict["onInverseEdge"] = on_inverse_edge
        if on_tombstone is not UNSET:
            field_dict["onTombstone"] = on_tombstone
        if on_expiry is not UNSET:
            field_dict["onExpiry"] = on_expiry
        if stale_after is not UNSET:
            field_dict["staleAfter"] = stale_after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        on_inverse_edge = d.pop("onInverseEdge", UNSET)

        on_tombstone = d.pop("onTombstone", UNSET)

        on_expiry = d.pop("onExpiry", UNSET)

        def _parse_stale_after(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        stale_after = _parse_stale_after(d.pop("staleAfter", UNSET))

        auto_resolve = cls(
            on_inverse_edge=on_inverse_edge,
            on_tombstone=on_tombstone,
            on_expiry=on_expiry,
            stale_after=stale_after,
        )

        auto_resolve.additional_properties = d
        return auto_resolve

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
