from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.entity_status import EntityStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.entity_alias import EntityAlias
    from ..models.entity_attributes import EntityAttributes
    from ..models.entity_key import EntityKey
    from ..models.entity_open_link_counts import EntityOpenLinkCounts


T = TypeVar("T", bound="Entity")


@_attrs_define
class Entity:
    """A canonical vehicle or object, and (since prompt 158) a person or organization, with its keys, aliases and per-
    source link counts

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            type_ (str): Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
            provisional (bool): True when created from weak keys only
            status (EntityStatus):
            created_at (datetime.datetime):
            updated_at (datetime.datetime):
            requested_id (None | str | Unset): Set when the requested id was merged into this canonical entity
            display_name (None | str | Unset): Precedence: registration > vessel name > satellite name > key
            attributes (EntityAttributes | Unset): Declared attributes copied from records (≤ 16 KB); registry origin wins
                over observations. Never owner PII.
            merged_into (None | str | Unset):
            keys (list[EntityKey] | Unset):
            aliases (list[EntityAlias] | Unset):
            open_link_counts (EntityOpenLinkCounts | Unset): Open links per source id (links from private sources are
                counted only for admin tokens)
    """

    id: str
    type_: str
    provisional: bool
    status: EntityStatus
    created_at: datetime.datetime
    updated_at: datetime.datetime
    requested_id: None | str | Unset = UNSET
    display_name: None | str | Unset = UNSET
    attributes: EntityAttributes | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    keys: list[EntityKey] | Unset = UNSET
    aliases: list[EntityAlias] | Unset = UNSET
    open_link_counts: EntityOpenLinkCounts | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        type_ = self.type_

        provisional = self.provisional

        status = self.status.value

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        requested_id: None | str | Unset
        if isinstance(self.requested_id, Unset):
            requested_id = UNSET
        else:
            requested_id = self.requested_id

        display_name: None | str | Unset
        if isinstance(self.display_name, Unset):
            display_name = UNSET
        else:
            display_name = self.display_name

        attributes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attributes, Unset):
            attributes = self.attributes.to_dict()

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        keys: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.keys, Unset):
            keys = []
            for keys_item_data in self.keys:
                keys_item = keys_item_data.to_dict()
                keys.append(keys_item)

        aliases: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.aliases, Unset):
            aliases = []
            for aliases_item_data in self.aliases:
                aliases_item = aliases_item_data.to_dict()
                aliases.append(aliases_item)

        open_link_counts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.open_link_counts, Unset):
            open_link_counts = self.open_link_counts.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "provisional": provisional,
                "status": status,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if requested_id is not UNSET:
            field_dict["requestedId"] = requested_id
        if display_name is not UNSET:
            field_dict["displayName"] = display_name
        if attributes is not UNSET:
            field_dict["attributes"] = attributes
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if keys is not UNSET:
            field_dict["keys"] = keys
        if aliases is not UNSET:
            field_dict["aliases"] = aliases
        if open_link_counts is not UNSET:
            field_dict["openLinkCounts"] = open_link_counts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.entity_alias import EntityAlias
        from ..models.entity_attributes import EntityAttributes
        from ..models.entity_key import EntityKey
        from ..models.entity_open_link_counts import EntityOpenLinkCounts

        d = dict(src_dict)
        id = d.pop("id")

        type_ = d.pop("type")

        provisional = d.pop("provisional")

        status = EntityStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        def _parse_requested_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        requested_id = _parse_requested_id(d.pop("requestedId", UNSET))

        def _parse_display_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        display_name = _parse_display_name(d.pop("displayName", UNSET))

        _attributes = d.pop("attributes", UNSET)
        attributes: EntityAttributes | Unset
        if isinstance(_attributes, Unset):
            attributes = UNSET
        else:
            attributes = EntityAttributes.from_dict(_attributes)

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        _keys = d.pop("keys", UNSET)
        keys: list[EntityKey] | Unset = UNSET
        if _keys is not UNSET:
            keys = []
            for keys_item_data in _keys:
                keys_item = EntityKey.from_dict(keys_item_data)

                keys.append(keys_item)

        _aliases = d.pop("aliases", UNSET)
        aliases: list[EntityAlias] | Unset = UNSET
        if _aliases is not UNSET:
            aliases = []
            for aliases_item_data in _aliases:
                aliases_item = EntityAlias.from_dict(aliases_item_data)

                aliases.append(aliases_item)

        _open_link_counts = d.pop("openLinkCounts", UNSET)
        open_link_counts: EntityOpenLinkCounts | Unset
        if isinstance(_open_link_counts, Unset):
            open_link_counts = UNSET
        else:
            open_link_counts = EntityOpenLinkCounts.from_dict(_open_link_counts)

        entity = cls(
            id=id,
            type_=type_,
            provisional=provisional,
            status=status,
            created_at=created_at,
            updated_at=updated_at,
            requested_id=requested_id,
            display_name=display_name,
            attributes=attributes,
            merged_into=merged_into,
            keys=keys,
            aliases=aliases,
            open_link_counts=open_link_counts,
        )

        entity.additional_properties = d
        return entity

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
