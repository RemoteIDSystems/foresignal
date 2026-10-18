from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaMatchRequest")


@_attrs_define
class MediaMatchRequest:
    """Match a PDQ hash against the fingerprint store

    Attributes:
        pdq (str): 256-bit PDQ hash, hex
        max_distance (int | Unset): PDQ's recommended threshold is 31 Default: 31.
        limit (int | Unset):  Default: 20.
    """

    pdq: str
    max_distance: int | Unset = 31
    limit: int | Unset = 20
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pdq = self.pdq

        max_distance = self.max_distance

        limit = self.limit

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "pdq": pdq,
            }
        )
        if max_distance is not UNSET:
            field_dict["maxDistance"] = max_distance
        if limit is not UNSET:
            field_dict["limit"] = limit

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        pdq = d.pop("pdq")

        max_distance = d.pop("maxDistance", UNSET)

        limit = d.pop("limit", UNSET)

        media_match_request = cls(
            pdq=pdq,
            max_distance=max_distance,
            limit=limit,
        )

        media_match_request.additional_properties = d
        return media_match_request

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
