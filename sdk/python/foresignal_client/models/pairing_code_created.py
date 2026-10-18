from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.pairing_code_scope import PairingCodeScope
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="PairingCodeCreated")


@_attrs_define
class PairingCodeCreated:
    """A new pairing code, returned once with its code and QR payload (023)

    Attributes:
        id (UUID):
        name (str):
        scope (PairingCodeScope):
        created_at (datetime.datetime):
        expires_at (datetime.datetime):
        code (str): 8 Crockford base32 characters shown as XXXX-XXXX; shown once
        qr_payload (str): `clientLinks.pair` template filled with `publicBaseUrl` and the code Example:
            foresignal://pair?u=https%3A%2F%2Ffs.example&c=ABCD-EFGH.
        restrictions (None | ReadRestriction | Unset):
        principal_id (None | str | Unset):
        private_access (bool | Unset):
        created_by_token_id (None | Unset | UUID):
        claimed_at (datetime.datetime | None | Unset):
        claimed_token_id (None | Unset | UUID):
    """

    id: UUID
    name: str
    scope: PairingCodeScope
    created_at: datetime.datetime
    expires_at: datetime.datetime
    code: str
    qr_payload: str
    restrictions: None | ReadRestriction | Unset = UNSET
    principal_id: None | str | Unset = UNSET
    private_access: bool | Unset = UNSET
    created_by_token_id: None | Unset | UUID = UNSET
    claimed_at: datetime.datetime | None | Unset = UNSET
    claimed_token_id: None | Unset | UUID = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.read_restriction import ReadRestriction

        id = str(self.id)

        name = self.name

        scope = self.scope.value

        created_at = self.created_at.isoformat()

        expires_at = self.expires_at.isoformat()

        code = self.code

        qr_payload = self.qr_payload

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

        created_by_token_id: None | str | Unset
        if isinstance(self.created_by_token_id, Unset):
            created_by_token_id = UNSET
        elif isinstance(self.created_by_token_id, UUID):
            created_by_token_id = str(self.created_by_token_id)
        else:
            created_by_token_id = self.created_by_token_id

        claimed_at: None | str | Unset
        if isinstance(self.claimed_at, Unset):
            claimed_at = UNSET
        elif isinstance(self.claimed_at, datetime.datetime):
            claimed_at = self.claimed_at.isoformat()
        else:
            claimed_at = self.claimed_at

        claimed_token_id: None | str | Unset
        if isinstance(self.claimed_token_id, Unset):
            claimed_token_id = UNSET
        elif isinstance(self.claimed_token_id, UUID):
            claimed_token_id = str(self.claimed_token_id)
        else:
            claimed_token_id = self.claimed_token_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "scope": scope,
                "createdAt": created_at,
                "expiresAt": expires_at,
                "code": code,
                "qrPayload": qr_payload,
            }
        )
        if restrictions is not UNSET:
            field_dict["restrictions"] = restrictions
        if principal_id is not UNSET:
            field_dict["principalId"] = principal_id
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access
        if created_by_token_id is not UNSET:
            field_dict["createdByTokenId"] = created_by_token_id
        if claimed_at is not UNSET:
            field_dict["claimedAt"] = claimed_at
        if claimed_token_id is not UNSET:
            field_dict["claimedTokenId"] = claimed_token_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        name = d.pop("name")

        scope = PairingCodeScope(d.pop("scope"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        code = d.pop("code")

        qr_payload = d.pop("qrPayload")

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

        def _parse_created_by_token_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                created_by_token_id_type_0 = UUID(data)

                return created_by_token_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        created_by_token_id = _parse_created_by_token_id(
            d.pop("createdByTokenId", UNSET)
        )

        def _parse_claimed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                claimed_at_type_0 = datetime.datetime.fromisoformat(data)

                return claimed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        claimed_at = _parse_claimed_at(d.pop("claimedAt", UNSET))

        def _parse_claimed_token_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                claimed_token_id_type_0 = UUID(data)

                return claimed_token_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        claimed_token_id = _parse_claimed_token_id(d.pop("claimedTokenId", UNSET))

        pairing_code_created = cls(
            id=id,
            name=name,
            scope=scope,
            created_at=created_at,
            expires_at=expires_at,
            code=code,
            qr_payload=qr_payload,
            restrictions=restrictions,
            principal_id=principal_id,
            private_access=private_access,
            created_by_token_id=created_by_token_id,
            claimed_at=claimed_at,
            claimed_token_id=claimed_token_id,
        )

        pairing_code_created.additional_properties = d
        return pairing_code_created

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
