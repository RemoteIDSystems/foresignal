from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.entity_alias_alias_type import EntityAliasAliasType
from ..models.entity_alias_origin import EntityAliasOrigin
from ..types import UNSET, Unset

T = TypeVar("T", bound="EntityAlias")


@_attrs_define
class EntityAlias:
    """A display name or former name of an entity with its validity interval

    Attributes:
        id (int):
        alias (str):  Example: N123AB.
        alias_type (EntityAliasAliasType):
        valid_from (datetime.datetime):
        origin (EntityAliasOrigin):
        valid_to (datetime.datetime | None | Unset):
    """

    id: int
    alias: str
    alias_type: EntityAliasAliasType
    valid_from: datetime.datetime
    origin: EntityAliasOrigin
    valid_to: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        alias = self.alias

        alias_type = self.alias_type.value

        valid_from = self.valid_from.isoformat()

        origin = self.origin.value

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
                "alias": alias,
                "aliasType": alias_type,
                "validFrom": valid_from,
                "origin": origin,
            }
        )
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        alias = d.pop("alias")

        alias_type = EntityAliasAliasType(d.pop("aliasType"))

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        origin = EntityAliasOrigin(d.pop("origin"))

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

        entity_alias = cls(
            id=id,
            alias=alias,
            alias_type=alias_type,
            valid_from=valid_from,
            origin=origin,
            valid_to=valid_to,
        )

        entity_alias.additional_properties = d
        return entity_alias

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
