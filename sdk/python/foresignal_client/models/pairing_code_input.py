from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.pairing_code_input_scope import PairingCodeInputScope
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="PairingCodeInput")


@_attrs_define
class PairingCodeInput:
    """Create a pairing code a device can claim for a token (023)

    Attributes:
        name (str): Name given to the token created on claim
        scope (PairingCodeInputScope): `admin` is never pairable
        restrictions (None | ReadRestriction | Unset):
        ttl (str | Unset): Code lifetime: default 10m, max 1h Example: 10m.
        principal_id (None | str | Unset): Principal that will own the token; default: a new principal named after the
            token
        private_access (bool | Unset): Grant visibility of private sources (039) to the claimed token Default: False.
    """

    name: str
    scope: PairingCodeInputScope
    restrictions: None | ReadRestriction | Unset = UNSET
    ttl: str | Unset = UNSET
    principal_id: None | str | Unset = UNSET
    private_access: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.read_restriction import ReadRestriction

        name = self.name

        scope = self.scope.value

        restrictions: dict[str, Any] | None | Unset
        if isinstance(self.restrictions, Unset):
            restrictions = UNSET
        elif isinstance(self.restrictions, ReadRestriction):
            restrictions = self.restrictions.to_dict()
        else:
            restrictions = self.restrictions

        ttl = self.ttl

        principal_id: None | str | Unset
        if isinstance(self.principal_id, Unset):
            principal_id = UNSET
        else:
            principal_id = self.principal_id

        private_access = self.private_access

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "scope": scope,
            }
        )
        if restrictions is not UNSET:
            field_dict["restrictions"] = restrictions
        if ttl is not UNSET:
            field_dict["ttl"] = ttl
        if principal_id is not UNSET:
            field_dict["principalId"] = principal_id
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        name = d.pop("name")

        scope = PairingCodeInputScope(d.pop("scope"))

        def _parse_restrictions(data: object) -> None | ReadRestriction | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                restrictions_type_0 = ReadRestriction.from_dict(data)

                return restrictions_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ReadRestriction | Unset, data)

        restrictions = _parse_restrictions(d.pop("restrictions", UNSET))

        ttl = d.pop("ttl", UNSET)

        def _parse_principal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal_id = _parse_principal_id(d.pop("principalId", UNSET))

        private_access = d.pop("privateAccess", UNSET)

        pairing_code_input = cls(
            name=name,
            scope=scope,
            restrictions=restrictions,
            ttl=ttl,
            principal_id=principal_id,
            private_access=private_access,
        )

        pairing_code_input.additional_properties = d
        return pairing_code_input

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
