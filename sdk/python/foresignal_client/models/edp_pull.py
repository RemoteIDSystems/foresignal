from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_pull_auth import EdpPullAuth
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpPull")


@_attrs_define
class EdpPull:
    """Provider items endpoint Foresignal polls

    Attributes:
        items_url (str):
        interval_seconds (int | Unset):  Default: 300.
        auth (EdpPullAuth | Unset):  Default: EdpPullAuth.NONE.
        header (None | str | Unset):
    """

    items_url: str
    interval_seconds: int | Unset = 300
    auth: EdpPullAuth | Unset = EdpPullAuth.NONE
    header: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items_url = self.items_url

        interval_seconds = self.interval_seconds

        auth: str | Unset = UNSET
        if not isinstance(self.auth, Unset):
            auth = self.auth.value

        header: None | str | Unset
        if isinstance(self.header, Unset):
            header = UNSET
        else:
            header = self.header

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "itemsUrl": items_url,
            }
        )
        if interval_seconds is not UNSET:
            field_dict["intervalSeconds"] = interval_seconds
        if auth is not UNSET:
            field_dict["auth"] = auth
        if header is not UNSET:
            field_dict["header"] = header

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        items_url = d.pop("itemsUrl")

        interval_seconds = d.pop("intervalSeconds", UNSET)

        _auth = d.pop("auth", UNSET)
        auth: EdpPullAuth | Unset
        if isinstance(_auth, Unset):
            auth = UNSET
        else:
            auth = EdpPullAuth(_auth)

        def _parse_header(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        header = _parse_header(d.pop("header", UNSET))

        edp_pull = cls(
            items_url=items_url,
            interval_seconds=interval_seconds,
            auth=auth,
            header=header,
        )

        edp_pull.additional_properties = d
        return edp_pull

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
