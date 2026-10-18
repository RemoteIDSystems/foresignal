from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.shared_item_role import SharedItemRole
from ..models.shared_item_via import SharedItemVia
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="SharedItem")


@_attrs_define
class SharedItem:
    """An object shared with the caller

    Attributes:
        envelope (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        role (SharedItemRole):
        via (SharedItemVia):
        shared_at (datetime.datetime):
        group_id (None | str | Unset):
        shared_by (None | PrincipalSummary | Unset):
    """

    envelope: ObjectEnvelope
    role: SharedItemRole
    via: SharedItemVia
    shared_at: datetime.datetime
    group_id: None | str | Unset = UNSET
    shared_by: None | PrincipalSummary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.principal_summary import PrincipalSummary

        envelope = self.envelope.to_dict()

        role = self.role.value

        via = self.via.value

        shared_at = self.shared_at.isoformat()

        group_id: None | str | Unset
        if isinstance(self.group_id, Unset):
            group_id = UNSET
        else:
            group_id = self.group_id

        shared_by: dict[str, Any] | None | Unset
        if isinstance(self.shared_by, Unset):
            shared_by = UNSET
        elif isinstance(self.shared_by, PrincipalSummary):
            shared_by = self.shared_by.to_dict()
        else:
            shared_by = self.shared_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "envelope": envelope,
                "role": role,
                "via": via,
                "sharedAt": shared_at,
            }
        )
        if group_id is not UNSET:
            field_dict["groupId"] = group_id
        if shared_by is not UNSET:
            field_dict["sharedBy"] = shared_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        envelope = ObjectEnvelope.from_dict(d.pop("envelope"))

        role = SharedItemRole(d.pop("role"))

        via = SharedItemVia(d.pop("via"))

        shared_at = datetime.datetime.fromisoformat(d.pop("sharedAt"))

        def _parse_group_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        group_id = _parse_group_id(d.pop("groupId", UNSET))

        def _parse_shared_by(data: object) -> None | PrincipalSummary | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                shared_by_type_0 = PrincipalSummary.from_dict(data)

                return shared_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrincipalSummary | Unset, data)

        shared_by = _parse_shared_by(d.pop("sharedBy", UNSET))

        shared_item = cls(
            envelope=envelope,
            role=role,
            via=via,
            shared_at=shared_at,
            group_id=group_id,
            shared_by=shared_by,
        )

        shared_item.additional_properties = d
        return shared_item

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
