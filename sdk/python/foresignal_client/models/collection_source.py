from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CollectionSource")


@_attrs_define
class CollectionSource:
    """One source in the instance collection set (the union pulled by the paced collector, 173)

    Attributes:
        source_id (str):
        in_collection (bool): Whether the instance collects this source at all
        enabled_by_users (int | None | Unset): How many users enable it in their view
    """

    source_id: str
    in_collection: bool
    enabled_by_users: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        in_collection = self.in_collection

        enabled_by_users: int | None | Unset
        if isinstance(self.enabled_by_users, Unset):
            enabled_by_users = UNSET
        else:
            enabled_by_users = self.enabled_by_users

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "inCollection": in_collection,
            }
        )
        if enabled_by_users is not UNSET:
            field_dict["enabledByUsers"] = enabled_by_users

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        in_collection = d.pop("inCollection")

        def _parse_enabled_by_users(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        enabled_by_users = _parse_enabled_by_users(d.pop("enabledByUsers", UNSET))

        collection_source = cls(
            source_id=source_id,
            in_collection=in_collection,
            enabled_by_users=enabled_by_users,
        )

        collection_source.additional_properties = d
        return collection_source

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
