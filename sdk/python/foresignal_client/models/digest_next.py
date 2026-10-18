from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DigestNext")


@_attrs_define
class DigestNext:
    """Cursor for the next call

    Attributes:
        since_seq (int):
        since_alert_id (int | None | Unset):
    """

    since_seq: int
    since_alert_id: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        since_seq = self.since_seq

        since_alert_id: int | None | Unset
        if isinstance(self.since_alert_id, Unset):
            since_alert_id = UNSET
        else:
            since_alert_id = self.since_alert_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sinceSeq": since_seq,
            }
        )
        if since_alert_id is not UNSET:
            field_dict["sinceAlertId"] = since_alert_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        since_seq = d.pop("sinceSeq")

        def _parse_since_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        since_alert_id = _parse_since_alert_id(d.pop("sinceAlertId", UNSET))

        digest_next = cls(
            since_seq=since_seq,
            since_alert_id=since_alert_id,
        )

        digest_next.additional_properties = d
        return digest_next

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
