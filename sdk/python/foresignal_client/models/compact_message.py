from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CompactMessage")


@_attrs_define
class CompactMessage:
    """Compact prioritised event message: an integer-key CBOR map (084 MeshCborProfile extended by 113), at most 160 bytes;
    keys 9 then 8 are dropped first to fit, keys 10–12 are kept. Shown here with the integer keys as strings (113)

        Attributes:
            field_0 (Literal[1]): Format version
            field_1 (int): Message type (084 types; 5 = intelligence event)
            field_3 (int): Severity: 0 info, 1 warning, 2 critical
            field_7 (int): Epoch seconds
            field_2 (str | Unset): Short code
            field_4 (str | Unset): Kind
            field_5 (int | Unset): Latitude × 1e5
            field_6 (int | Unset): Longitude × 1e5
            field_8 (str | Unset): Title (truncated on a UTF-8 boundary)
            field_9 (str | Unset): Source id
            field_10 (str | Unset): Origin instance short id (4 bytes)
            field_11 (str | Unset): Message id: first 8 bytes of SHA-256 of the origin ref
            field_12 (int | Unset): Expiry in minutes after key 7
    """

    field_0: Literal[1]
    field_1: int
    field_3: int
    field_7: int
    field_2: str | Unset = UNSET
    field_4: str | Unset = UNSET
    field_5: int | Unset = UNSET
    field_6: int | Unset = UNSET
    field_8: str | Unset = UNSET
    field_9: str | Unset = UNSET
    field_10: str | Unset = UNSET
    field_11: str | Unset = UNSET
    field_12: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        field_0 = self.field_0

        field_1 = self.field_1

        field_3 = self.field_3

        field_7 = self.field_7

        field_2 = self.field_2

        field_4 = self.field_4

        field_5 = self.field_5

        field_6 = self.field_6

        field_8 = self.field_8

        field_9 = self.field_9

        field_10 = self.field_10

        field_11 = self.field_11

        field_12 = self.field_12

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "0": field_0,
                "1": field_1,
                "3": field_3,
                "7": field_7,
            }
        )
        if field_2 is not UNSET:
            field_dict["2"] = field_2
        if field_4 is not UNSET:
            field_dict["4"] = field_4
        if field_5 is not UNSET:
            field_dict["5"] = field_5
        if field_6 is not UNSET:
            field_dict["6"] = field_6
        if field_8 is not UNSET:
            field_dict["8"] = field_8
        if field_9 is not UNSET:
            field_dict["9"] = field_9
        if field_10 is not UNSET:
            field_dict["10"] = field_10
        if field_11 is not UNSET:
            field_dict["11"] = field_11
        if field_12 is not UNSET:
            field_dict["12"] = field_12

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        field_0 = cast(Literal[1], d.pop("0"))
        if field_0 != 1:
            raise ValueError(f"0 must match const 1, got '{field_0}'")

        field_1 = d.pop("1")

        field_3 = d.pop("3")

        field_7 = d.pop("7")

        field_2 = d.pop("2", UNSET)

        field_4 = d.pop("4", UNSET)

        field_5 = d.pop("5", UNSET)

        field_6 = d.pop("6", UNSET)

        field_8 = d.pop("8", UNSET)

        field_9 = d.pop("9", UNSET)

        field_10 = d.pop("10", UNSET)

        field_11 = d.pop("11", UNSET)

        field_12 = d.pop("12", UNSET)

        compact_message = cls(
            field_0=field_0,
            field_1=field_1,
            field_3=field_3,
            field_7=field_7,
            field_2=field_2,
            field_4=field_4,
            field_5=field_5,
            field_6=field_6,
            field_8=field_8,
            field_9=field_9,
            field_10=field_10,
            field_11=field_11,
            field_12=field_12,
        )

        compact_message.additional_properties = d
        return compact_message

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
