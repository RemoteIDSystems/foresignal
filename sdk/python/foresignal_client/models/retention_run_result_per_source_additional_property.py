from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RetentionRunResultPerSourceAdditionalProperty")


@_attrs_define
class RetentionRunResultPerSourceAdditionalProperty:
    """
    Attributes:
        versions_deleted (int | Unset):
        floor_seq (int | None | Unset):
    """

    versions_deleted: int | Unset = UNSET
    floor_seq: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        versions_deleted = self.versions_deleted

        floor_seq: int | None | Unset
        if isinstance(self.floor_seq, Unset):
            floor_seq = UNSET
        else:
            floor_seq = self.floor_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if versions_deleted is not UNSET:
            field_dict["versionsDeleted"] = versions_deleted
        if floor_seq is not UNSET:
            field_dict["floorSeq"] = floor_seq

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        versions_deleted = d.pop("versionsDeleted", UNSET)

        def _parse_floor_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        floor_seq = _parse_floor_seq(d.pop("floorSeq", UNSET))

        retention_run_result_per_source_additional_property = cls(
            versions_deleted=versions_deleted,
            floor_seq=floor_seq,
        )

        retention_run_result_per_source_additional_property.additional_properties = d
        return retention_run_result_per_source_additional_property

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
