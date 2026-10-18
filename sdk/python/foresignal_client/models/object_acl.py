from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.shareable_type import ShareableType
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_acl_entry import ObjectAclEntry
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="ObjectAcl")


@_attrs_define
class ObjectAcl:
    """Owner, visibility, TLP and access list of a shareable object

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (ShareableType): Object types that carry an owner, visibility, access list and TLP (Client-Contract §10)
        owner (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        visibility (Visibility):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        entries (list[ObjectAclEntry]):
        updated_at (datetime.datetime | Unset):
    """

    ref: str
    type_: ShareableType
    owner: PrincipalSummary
    visibility: Visibility
    tlp: Tlp
    entries: list[ObjectAclEntry]
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        type_ = self.type_.value

        owner = self.owner.to_dict()

        visibility = self.visibility.value

        tlp = self.tlp.value

        entries = []
        for entries_item_data in self.entries:
            entries_item = entries_item_data.to_dict()
            entries.append(entries_item)

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "type": type_,
                "owner": owner,
                "visibility": visibility,
                "tlp": tlp,
                "entries": entries,
            }
        )
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_acl_entry import ObjectAclEntry
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        ref = d.pop("ref")

        type_ = ShareableType(d.pop("type"))

        owner = PrincipalSummary.from_dict(d.pop("owner"))

        visibility = Visibility(d.pop("visibility"))

        tlp = Tlp(d.pop("tlp"))

        entries = []
        _entries = d.pop("entries")
        for entries_item_data in _entries:
            entries_item = ObjectAclEntry.from_dict(entries_item_data)

            entries.append(entries_item)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        object_acl = cls(
            ref=ref,
            type_=type_,
            owner=owner,
            visibility=visibility,
            tlp=tlp,
            entries=entries,
            updated_at=updated_at,
        )

        object_acl.additional_properties = d
        return object_acl

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
