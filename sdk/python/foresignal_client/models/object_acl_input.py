from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_acl_entry_input import ObjectAclEntryInput


T = TypeVar("T", bound="ObjectAclInput")


@_attrs_define
class ObjectAclInput:
    """Replace an object's access list, visibility and TLP label; changing `owner` (transfer) or granting `owner` is
    allowed only to owners

        Attributes:
            entries (list[ObjectAclEntryInput]):
            visibility (Visibility | Unset):
            owner (str | Unset): Principal id or handle of the new owner (owners only)
            tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
    """

    entries: list[ObjectAclEntryInput]
    visibility: Visibility | Unset = UNSET
    owner: str | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        entries = []
        for entries_item_data in self.entries:
            entries_item = entries_item_data.to_dict()
            entries.append(entries_item)

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        owner = self.owner

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "entries": entries,
            }
        )
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if owner is not UNSET:
            field_dict["owner"] = owner
        if tlp is not UNSET:
            field_dict["tlp"] = tlp

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_acl_entry_input import ObjectAclEntryInput

        d = dict(src_dict)
        entries = []
        _entries = d.pop("entries")
        for entries_item_data in _entries:
            entries_item = ObjectAclEntryInput.from_dict(entries_item_data)

            entries.append(entries_item)

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        owner = d.pop("owner", UNSET)

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        object_acl_input = cls(
            entries=entries,
            visibility=visibility,
            owner=owner,
            tlp=tlp,
        )

        object_acl_input.additional_properties = d
        return object_acl_input

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
