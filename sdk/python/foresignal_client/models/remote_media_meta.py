from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RemoteMediaMeta")


@_attrs_define
class RemoteMediaMeta:
    """Metadata part of a remote media upload

    Attributes:
        composite (str):  Example: msu_mr_rgb_221.
        timestamp (datetime.datetime):
        satellite (str):  Example: METEOR-M2 3.
        pass_id (None | str | Unset): satellite-pass externalId to attach to; else the 049 join rule applies
    """

    composite: str
    timestamp: datetime.datetime
    satellite: str
    pass_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        composite = self.composite

        timestamp = self.timestamp.isoformat()

        satellite = self.satellite

        pass_id: None | str | Unset
        if isinstance(self.pass_id, Unset):
            pass_id = UNSET
        else:
            pass_id = self.pass_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "composite": composite,
                "timestamp": timestamp,
                "satellite": satellite,
            }
        )
        if pass_id is not UNSET:
            field_dict["passId"] = pass_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        composite = d.pop("composite")

        timestamp = datetime.datetime.fromisoformat(d.pop("timestamp"))

        satellite = d.pop("satellite")

        def _parse_pass_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        pass_id = _parse_pass_id(d.pop("passId", UNSET))

        remote_media_meta = cls(
            composite=composite,
            timestamp=timestamp,
            satellite=satellite,
            pass_id=pass_id,
        )

        remote_media_meta.additional_properties = d
        return remote_media_meta

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
