from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="SocialPlatformStreamType0LastGapType0")


@_attrs_define
class SocialPlatformStreamType0LastGapType0:
    """Last gap (reconnect with cursor loss)

    Attributes:
        from_ (datetime.datetime):
        to (datetime.datetime):
        replayed (bool):
    """

    from_: datetime.datetime
    to: datetime.datetime
    replayed: bool
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        replayed = self.replayed

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "replayed": replayed,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        replayed = d.pop("replayed")

        social_platform_stream_type_0_last_gap_type_0 = cls(
            from_=from_,
            to=to,
            replayed=replayed,
        )

        social_platform_stream_type_0_last_gap_type_0.additional_properties = d
        return social_platform_stream_type_0_last_gap_type_0

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
