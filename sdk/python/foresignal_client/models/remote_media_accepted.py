from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RemoteMediaAccepted")


@_attrs_define
class RemoteMediaAccepted:
    """Accepted remote media

    Attributes:
        media_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        sha256 (str):
        pass_id (None | str | Unset): Pass the image attached to, or the minted pass
    """

    media_id: str
    sha256: str
    pass_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        media_id = self.media_id

        sha256 = self.sha256

        pass_id: None | str | Unset
        if isinstance(self.pass_id, Unset):
            pass_id = UNSET
        else:
            pass_id = self.pass_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mediaId": media_id,
                "sha256": sha256,
            }
        )
        if pass_id is not UNSET:
            field_dict["passId"] = pass_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        media_id = d.pop("mediaId")

        sha256 = d.pop("sha256")

        def _parse_pass_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        pass_id = _parse_pass_id(d.pop("passId", UNSET))

        remote_media_accepted = cls(
            media_id=media_id,
            sha256=sha256,
            pass_id=pass_id,
        )

        remote_media_accepted.additional_properties = d
        return remote_media_accepted

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
