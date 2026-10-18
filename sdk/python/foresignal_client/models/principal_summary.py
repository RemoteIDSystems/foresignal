from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.principal_summary_kind import PrincipalSummaryKind
from ..types import UNSET, Unset

T = TypeVar("T", bound="PrincipalSummary")


@_attrs_define
class PrincipalSummary:
    """Public view of a principal: handle, display name, kind and avatar only

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        handle (str):  Example: jsmith.
        display_name (str):
        kind (PrincipalSummaryKind):
        avatar_media_id (None | str | Unset): 043 media id of the avatar image
    """

    id: str
    handle: str
    display_name: str
    kind: PrincipalSummaryKind
    avatar_media_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        handle = self.handle

        display_name = self.display_name

        kind = self.kind.value

        avatar_media_id: None | str | Unset
        if isinstance(self.avatar_media_id, Unset):
            avatar_media_id = UNSET
        else:
            avatar_media_id = self.avatar_media_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "handle": handle,
                "displayName": display_name,
                "kind": kind,
            }
        )
        if avatar_media_id is not UNSET:
            field_dict["avatarMediaId"] = avatar_media_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        handle = d.pop("handle")

        display_name = d.pop("displayName")

        kind = PrincipalSummaryKind(d.pop("kind"))

        def _parse_avatar_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        avatar_media_id = _parse_avatar_media_id(d.pop("avatarMediaId", UNSET))

        principal_summary = cls(
            id=id,
            handle=handle,
            display_name=display_name,
            kind=kind,
            avatar_media_id=avatar_media_id,
        )

        principal_summary.additional_properties = d
        return principal_summary

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
