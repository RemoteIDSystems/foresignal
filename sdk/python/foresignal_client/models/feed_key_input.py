from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="FeedKeyInput")


@_attrs_define
class FeedKeyInput:
    """Create a path-bound feed key for header-less clients (035)

    Attributes:
        name (str):
        prefixes (list[str]): Allowed canonical path prefixes (GET/HEAD only)
        restrictions (ReadRestriction): The effective read restriction of a principal (035): restricted tokens, feed
            keys and the anonymous profile. Intersected with every request filter by `RestrictionClamp`; out-of-scope single
            reads return 404.
        expires_at (datetime.datetime): ≤ 365 days ahead
        private_access (bool | Unset): May read private sources (039) Default: False.
    """

    name: str
    prefixes: list[str]
    restrictions: ReadRestriction
    expires_at: datetime.datetime
    private_access: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        prefixes = self.prefixes

        restrictions = self.restrictions.to_dict()

        expires_at = self.expires_at.isoformat()

        private_access = self.private_access

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "prefixes": prefixes,
                "restrictions": restrictions,
                "expiresAt": expires_at,
            }
        )
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        name = d.pop("name")

        prefixes = cast(list[str], d.pop("prefixes"))

        restrictions = ReadRestriction.from_dict(d.pop("restrictions"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        private_access = d.pop("privateAccess", UNSET)

        feed_key_input = cls(
            name=name,
            prefixes=prefixes,
            restrictions=restrictions,
            expires_at=expires_at,
            private_access=private_access,
        )

        feed_key_input.additional_properties = d
        return feed_key_input

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
