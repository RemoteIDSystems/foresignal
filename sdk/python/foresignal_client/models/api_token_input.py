from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.api_token_input_licence_policy_item import ApiTokenInputLicencePolicyItem
from ..models.api_token_input_scope import ApiTokenInputScope
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="ApiTokenInput")


@_attrs_define
class ApiTokenInput:
    """
    Attributes:
        name (str):
        scope (ApiTokenInputScope):
        expires_at (datetime.datetime | None | Unset):
        restrictions (None | ReadRestriction | Unset): Rejected on admin tokens (400 RESTRICTION_NOT_ALLOWED, 035)
        principal_id (None | str | Unset): Owning principal (id or handle); default: a new principal named after the
            token; not accepted by createMyToken, whose tokens are always the caller's (R2-025)
        private_access (bool | Unset): Grant visibility of private sources (039) Default: False.
        licence_policy (list[ApiTokenInputLicencePolicyItem] | Unset): Licence policy floor applied to every read with
            this token; requests may add flags but never remove these
    """

    name: str
    scope: ApiTokenInputScope
    expires_at: datetime.datetime | None | Unset = UNSET
    restrictions: None | ReadRestriction | Unset = UNSET
    principal_id: None | str | Unset = UNSET
    private_access: bool | Unset = False
    licence_policy: list[ApiTokenInputLicencePolicyItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.read_restriction import ReadRestriction

        name = self.name

        scope = self.scope.value

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        restrictions: dict[str, Any] | None | Unset
        if isinstance(self.restrictions, Unset):
            restrictions = UNSET
        elif isinstance(self.restrictions, ReadRestriction):
            restrictions = self.restrictions.to_dict()
        else:
            restrictions = self.restrictions

        principal_id: None | str | Unset
        if isinstance(self.principal_id, Unset):
            principal_id = UNSET
        else:
            principal_id = self.principal_id

        private_access = self.private_access

        licence_policy: list[str] | Unset = UNSET
        if not isinstance(self.licence_policy, Unset):
            licence_policy = []
            for licence_policy_item_data in self.licence_policy:
                licence_policy_item = licence_policy_item_data.value
                licence_policy.append(licence_policy_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "scope": scope,
            }
        )
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if restrictions is not UNSET:
            field_dict["restrictions"] = restrictions
        if principal_id is not UNSET:
            field_dict["principalId"] = principal_id
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access
        if licence_policy is not UNSET:
            field_dict["licencePolicy"] = licence_policy

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        name = d.pop("name")

        scope = ApiTokenInputScope(d.pop("scope"))

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

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

        def _parse_principal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal_id = _parse_principal_id(d.pop("principalId", UNSET))

        private_access = d.pop("privateAccess", UNSET)

        _licence_policy = d.pop("licencePolicy", UNSET)
        licence_policy: list[ApiTokenInputLicencePolicyItem] | Unset = UNSET
        if _licence_policy is not UNSET:
            licence_policy = []
            for licence_policy_item_data in _licence_policy:
                licence_policy_item = ApiTokenInputLicencePolicyItem(
                    licence_policy_item_data
                )

                licence_policy.append(licence_policy_item)

        api_token_input = cls(
            name=name,
            scope=scope,
            expires_at=expires_at,
            restrictions=restrictions,
            principal_id=principal_id,
            private_access=private_access,
            licence_policy=licence_policy,
        )

        api_token_input.additional_properties = d
        return api_token_input

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
