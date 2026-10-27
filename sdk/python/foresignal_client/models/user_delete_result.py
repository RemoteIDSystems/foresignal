from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="UserDeleteResult")


@_attrs_define
class UserDeleteResult:
    """Outcome of deleting a user, including what became of the config they owned (168)

    Attributes:
        deleted_user_id (UUID):
        owned_config_disposition (str): Default reassigns the user's owned triggers, channels, providers and views to an
            admin
        reassigned_to (None | Unset | UUID):
    """

    deleted_user_id: UUID
    owned_config_disposition: str
    reassigned_to: None | Unset | UUID = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        deleted_user_id = str(self.deleted_user_id)

        owned_config_disposition = self.owned_config_disposition

        reassigned_to: None | str | Unset
        if isinstance(self.reassigned_to, Unset):
            reassigned_to = UNSET
        elif isinstance(self.reassigned_to, UUID):
            reassigned_to = str(self.reassigned_to)
        else:
            reassigned_to = self.reassigned_to

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "deletedUserId": deleted_user_id,
                "ownedConfigDisposition": owned_config_disposition,
            }
        )
        if reassigned_to is not UNSET:
            field_dict["reassignedTo"] = reassigned_to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        deleted_user_id = UUID(d.pop("deletedUserId"))

        owned_config_disposition = d.pop("ownedConfigDisposition")

        def _parse_reassigned_to(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reassigned_to_type_0 = UUID(data)

                return reassigned_to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        reassigned_to = _parse_reassigned_to(d.pop("reassignedTo", UNSET))

        user_delete_result = cls(
            deleted_user_id=deleted_user_id,
            owned_config_disposition=owned_config_disposition,
            reassigned_to=reassigned_to,
        )

        user_delete_result.additional_properties = d
        return user_delete_result

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
