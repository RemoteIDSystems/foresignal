from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.deception_payload_checks_item_check import DeceptionPayloadChecksItemCheck
from ..types import UNSET, Unset

T = TypeVar("T", bound="DeceptionPayloadChecksItem")


@_attrs_define
class DeceptionPayloadChecksItem:
    """A matched check with its count

    Attributes:
        check (DeceptionPayloadChecksItemCheck):
        count (int):
        last_at (datetime.datetime | Unset):
        detail (None | str | Unset):
    """

    check: DeceptionPayloadChecksItemCheck
    count: int
    last_at: datetime.datetime | Unset = UNSET
    detail: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        check = self.check.value

        count = self.count

        last_at: str | Unset = UNSET
        if not isinstance(self.last_at, Unset):
            last_at = self.last_at.isoformat()

        detail: None | str | Unset
        if isinstance(self.detail, Unset):
            detail = UNSET
        else:
            detail = self.detail

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "check": check,
                "count": count,
            }
        )
        if last_at is not UNSET:
            field_dict["lastAt"] = last_at
        if detail is not UNSET:
            field_dict["detail"] = detail

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        check = DeceptionPayloadChecksItemCheck(d.pop("check"))

        count = d.pop("count")

        _last_at = d.pop("lastAt", UNSET)
        last_at: datetime.datetime | Unset
        if isinstance(_last_at, Unset):
            last_at = UNSET
        else:
            last_at = datetime.datetime.fromisoformat(_last_at)

        def _parse_detail(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        detail = _parse_detail(d.pop("detail", UNSET))

        deception_payload_checks_item = cls(
            check=check,
            count=count,
            last_at=last_at,
            detail=detail,
        )

        deception_payload_checks_item.additional_properties = d
        return deception_payload_checks_item

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
