from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.api_token_created_via import ApiTokenCreatedVia
from ..models.api_token_licence_policy_item import ApiTokenLicencePolicyItem
from ..models.api_token_scope import ApiTokenScope
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.device_info import DeviceInfo
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="ApiTokenCreated")


@_attrs_define
class ApiTokenCreated:
    """
    Attributes:
        id (UUID):
        name (str):
        scope (ApiTokenScope):
        token_prefix (str):  Example: fsr_AbC1.
        created_at (datetime.datetime):
        token (str): Shown once
        last_used_at (datetime.datetime | None | Unset):
        expires_at (datetime.datetime | None | Unset):
        revoked_at (datetime.datetime | None | Unset):
        bootstrap (bool | Unset):
        restrictions (None | ReadRestriction | Unset): Data and endpoint restrictions (data restrictions enforced from
            035)
        device (DeviceInfo | None | Unset):
        created_via (ApiTokenCreatedVia | Unset): `self`: minted by a signed-in user with createMyToken and owned by
            that user's principal (R2-025)
        principal_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        private_access (bool | Unset): May see private sources (039)
        licence_policy (list[ApiTokenLicencePolicyItem] | Unset): Licence policy floor applied to every read with this
            token; requests may add flags but never remove these
        principal_handle (str | Unset):
    """

    id: UUID
    name: str
    scope: ApiTokenScope
    token_prefix: str
    created_at: datetime.datetime
    token: str
    last_used_at: datetime.datetime | None | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    revoked_at: datetime.datetime | None | Unset = UNSET
    bootstrap: bool | Unset = UNSET
    restrictions: None | ReadRestriction | Unset = UNSET
    device: DeviceInfo | None | Unset = UNSET
    created_via: ApiTokenCreatedVia | Unset = UNSET
    principal_id: str | Unset = UNSET
    private_access: bool | Unset = UNSET
    licence_policy: list[ApiTokenLicencePolicyItem] | Unset = UNSET
    principal_handle: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.device_info import DeviceInfo
        from ..models.read_restriction import ReadRestriction

        id = str(self.id)

        name = self.name

        scope = self.scope.value

        token_prefix = self.token_prefix

        created_at = self.created_at.isoformat()

        token = self.token

        last_used_at: None | str | Unset
        if isinstance(self.last_used_at, Unset):
            last_used_at = UNSET
        elif isinstance(self.last_used_at, datetime.datetime):
            last_used_at = self.last_used_at.isoformat()
        else:
            last_used_at = self.last_used_at

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        revoked_at: None | str | Unset
        if isinstance(self.revoked_at, Unset):
            revoked_at = UNSET
        elif isinstance(self.revoked_at, datetime.datetime):
            revoked_at = self.revoked_at.isoformat()
        else:
            revoked_at = self.revoked_at

        bootstrap = self.bootstrap

        restrictions: dict[str, Any] | None | Unset
        if isinstance(self.restrictions, Unset):
            restrictions = UNSET
        elif isinstance(self.restrictions, ReadRestriction):
            restrictions = self.restrictions.to_dict()
        else:
            restrictions = self.restrictions

        device: dict[str, Any] | None | Unset
        if isinstance(self.device, Unset):
            device = UNSET
        elif isinstance(self.device, DeviceInfo):
            device = self.device.to_dict()
        else:
            device = self.device

        created_via: str | Unset = UNSET
        if not isinstance(self.created_via, Unset):
            created_via = self.created_via.value

        principal_id = self.principal_id

        private_access = self.private_access

        licence_policy: list[str] | Unset = UNSET
        if not isinstance(self.licence_policy, Unset):
            licence_policy = []
            for licence_policy_item_data in self.licence_policy:
                licence_policy_item = licence_policy_item_data.value
                licence_policy.append(licence_policy_item)

        principal_handle = self.principal_handle

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "scope": scope,
                "tokenPrefix": token_prefix,
                "createdAt": created_at,
                "token": token,
            }
        )
        if last_used_at is not UNSET:
            field_dict["lastUsedAt"] = last_used_at
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if revoked_at is not UNSET:
            field_dict["revokedAt"] = revoked_at
        if bootstrap is not UNSET:
            field_dict["bootstrap"] = bootstrap
        if restrictions is not UNSET:
            field_dict["restrictions"] = restrictions
        if device is not UNSET:
            field_dict["device"] = device
        if created_via is not UNSET:
            field_dict["createdVia"] = created_via
        if principal_id is not UNSET:
            field_dict["principalId"] = principal_id
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access
        if licence_policy is not UNSET:
            field_dict["licencePolicy"] = licence_policy
        if principal_handle is not UNSET:
            field_dict["principalHandle"] = principal_handle

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.device_info import DeviceInfo
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        name = d.pop("name")

        scope = ApiTokenScope(d.pop("scope"))

        token_prefix = d.pop("tokenPrefix")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        token = d.pop("token")

        def _parse_last_used_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_used_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_used_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_used_at = _parse_last_used_at(d.pop("lastUsedAt", UNSET))

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

        def _parse_revoked_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                revoked_at_type_0 = datetime.datetime.fromisoformat(data)

                return revoked_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        revoked_at = _parse_revoked_at(d.pop("revokedAt", UNSET))

        bootstrap = d.pop("bootstrap", UNSET)

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

        def _parse_device(data: object) -> DeviceInfo | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                device_type_0 = DeviceInfo.from_dict(data)

                return device_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DeviceInfo | None | Unset, data)

        device = _parse_device(d.pop("device", UNSET))

        _created_via = d.pop("createdVia", UNSET)
        created_via: ApiTokenCreatedVia | Unset
        if isinstance(_created_via, Unset):
            created_via = UNSET
        else:
            created_via = ApiTokenCreatedVia(_created_via)

        principal_id = d.pop("principalId", UNSET)

        private_access = d.pop("privateAccess", UNSET)

        _licence_policy = d.pop("licencePolicy", UNSET)
        licence_policy: list[ApiTokenLicencePolicyItem] | Unset = UNSET
        if _licence_policy is not UNSET:
            licence_policy = []
            for licence_policy_item_data in _licence_policy:
                licence_policy_item = ApiTokenLicencePolicyItem(
                    licence_policy_item_data
                )

                licence_policy.append(licence_policy_item)

        principal_handle = d.pop("principalHandle", UNSET)

        api_token_created = cls(
            id=id,
            name=name,
            scope=scope,
            token_prefix=token_prefix,
            created_at=created_at,
            token=token,
            last_used_at=last_used_at,
            expires_at=expires_at,
            revoked_at=revoked_at,
            bootstrap=bootstrap,
            restrictions=restrictions,
            device=device,
            created_via=created_via,
            principal_id=principal_id,
            private_access=private_access,
            licence_policy=licence_policy,
            principal_handle=principal_handle,
        )

        api_token_created.additional_properties = d
        return api_token_created

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
