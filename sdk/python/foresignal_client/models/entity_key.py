from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.entity_key_key_type import EntityKeyKeyType
from ..models.entity_key_origin import EntityKeyOrigin
from ..models.entity_key_role import EntityKeyRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="EntityKey")


@_attrs_define
class EntityKey:
    """A deterministic identity key held by an entity for a validity interval; rows are never deleted, only closed or
    retracted

        Attributes:
            id (int): Key row id
            entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            key_type (EntityKeyKeyType): Key type. `callsign` values are domain-prefixed (`air:`, `sea:`, `ham:`). Corporate
                and person keys (prompt 158): `lei`, `company-number` (`JURISDICTION:NUMBER`), `tax-id`, `officer-id`
                (`REGISTRY:ID`) for organizations; `person-key` (a normalized name key) for people
            key_value (str): Normalised value (e.g. icao24 lowercase hex, registration without spaces or hyphens) Example:
                ae1234.
            role (EntityKeyRole): ANCHOR keys (airframe, imo, norad, cospar, lei, company-number, tax-id, officer-id)
                identify an entity on their own; WEAK keys (callsign, person-key) can be re-assigned
            confidence (float):
            origin (EntityKeyOrigin):
            valid_from (datetime.datetime):
            retracted (bool): True when an operator retracted the key
            origin_ref (None | str | Unset): `sourceId:externalId` of the asserting record, or the operator token id
            valid_to (datetime.datetime | None | Unset): null while the key is open
    """

    id: int
    entity_id: str
    key_type: EntityKeyKeyType
    key_value: str
    role: EntityKeyRole
    confidence: float
    origin: EntityKeyOrigin
    valid_from: datetime.datetime
    retracted: bool
    origin_ref: None | str | Unset = UNSET
    valid_to: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        entity_id = self.entity_id

        key_type = self.key_type.value

        key_value = self.key_value

        role = self.role.value

        confidence = self.confidence

        origin = self.origin.value

        valid_from = self.valid_from.isoformat()

        retracted = self.retracted

        origin_ref: None | str | Unset
        if isinstance(self.origin_ref, Unset):
            origin_ref = UNSET
        else:
            origin_ref = self.origin_ref

        valid_to: None | str | Unset
        if isinstance(self.valid_to, Unset):
            valid_to = UNSET
        elif isinstance(self.valid_to, datetime.datetime):
            valid_to = self.valid_to.isoformat()
        else:
            valid_to = self.valid_to

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "entityId": entity_id,
                "keyType": key_type,
                "keyValue": key_value,
                "role": role,
                "confidence": confidence,
                "origin": origin,
                "validFrom": valid_from,
                "retracted": retracted,
            }
        )
        if origin_ref is not UNSET:
            field_dict["originRef"] = origin_ref
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        entity_id = d.pop("entityId")

        key_type = EntityKeyKeyType(d.pop("keyType"))

        key_value = d.pop("keyValue")

        role = EntityKeyRole(d.pop("role"))

        confidence = d.pop("confidence")

        origin = EntityKeyOrigin(d.pop("origin"))

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        retracted = d.pop("retracted")

        def _parse_origin_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        origin_ref = _parse_origin_ref(d.pop("originRef", UNSET))

        def _parse_valid_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_to_type_0 = datetime.datetime.fromisoformat(data)

                return valid_to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_to = _parse_valid_to(d.pop("validTo", UNSET))

        entity_key = cls(
            id=id,
            entity_id=entity_id,
            key_type=key_type,
            key_value=key_value,
            role=role,
            confidence=confidence,
            origin=origin,
            valid_from=valid_from,
            retracted=retracted,
            origin_ref=origin_ref,
            valid_to=valid_to,
        )

        entity_key.additional_properties = d
        return entity_key

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
