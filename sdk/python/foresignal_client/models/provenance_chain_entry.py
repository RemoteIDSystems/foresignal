from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.provenance_chain_entry_role import ProvenanceChainEntryRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProvenanceChainEntry")


@_attrs_define
class ProvenanceChainEntry:
    """One hop of an analytic product's provenance chain, from origin to now (134)

    Attributes:
        instance_id (str):
        principal (str): Principal handle on that instance
        product_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        manifest_id (None | str | Unset):
        role (ProvenanceChainEntryRole | Unset):
        at (datetime.datetime | None | Unset):
    """

    instance_id: str
    principal: str
    product_ref: str
    manifest_id: None | str | Unset = UNSET
    role: ProvenanceChainEntryRole | Unset = UNSET
    at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        principal = self.principal

        product_ref = self.product_ref

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        at: None | str | Unset
        if isinstance(self.at, Unset):
            at = UNSET
        elif isinstance(self.at, datetime.datetime):
            at = self.at.isoformat()
        else:
            at = self.at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "principal": principal,
                "productRef": product_ref,
            }
        )
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if role is not UNSET:
            field_dict["role"] = role
        if at is not UNSET:
            field_dict["at"] = at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        principal = d.pop("principal")

        product_ref = d.pop("productRef")

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        _role = d.pop("role", UNSET)
        role: ProvenanceChainEntryRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = ProvenanceChainEntryRole(_role)

        def _parse_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                at_type_0 = datetime.datetime.fromisoformat(data)

                return at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        at = _parse_at(d.pop("at", UNSET))

        provenance_chain_entry = cls(
            instance_id=instance_id,
            principal=principal,
            product_ref=product_ref,
            manifest_id=manifest_id,
            role=role,
            at=at,
        )

        provenance_chain_entry.additional_properties = d
        return provenance_chain_entry

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
