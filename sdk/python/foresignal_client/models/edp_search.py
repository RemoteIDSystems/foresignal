from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_search_auth import EdpSearchAuth
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpSearch")


@_attrs_define
class EdpSearch:
    """Optional provider search endpoint used by cues (137)

    Attributes:
        url (str):
        auth (EdpSearchAuth | Unset):  Default: EdpSearchAuth.NONE.
        header (None | str | Unset):
    """

    url: str
    auth: EdpSearchAuth | Unset = EdpSearchAuth.NONE
    header: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        url = self.url

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
                "url": url,
            }
        )
        if auth is not UNSET:
            field_dict["auth"] = auth
        if header is not UNSET:
            field_dict["header"] = header

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        url = d.pop("url")

        _auth = d.pop("auth", UNSET)
        auth: EdpSearchAuth | Unset
        if isinstance(_auth, Unset):
            auth = UNSET
        else:
            auth = EdpSearchAuth(_auth)

        def _parse_header(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        header = _parse_header(d.pop("header", UNSET))

        edp_search = cls(
            url=url,
            auth=auth,
            header=header,
        )

        edp_search.additional_properties = d
        return edp_search

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
