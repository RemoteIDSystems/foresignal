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

T = TypeVar("T", bound="ContextLink")


@_attrs_define
class ContextLink:
    """Deep-link payload (Client-Contract §6): `{v, ws, ctx}` as base64url JSON, ≤ 2 KB

    Attributes:
        version (Literal[1]):
        payload (str): base64url JSON `{"v":1,"ws":…,"ctx":{…}}`
        fragment (str):  Example: #fs=eyJ2IjoxLCJ3cyI6….
        bytes_ (int):
        workspace_id (None | str | Unset):
    """

    version: Literal[1]
    payload: str
    fragment: str
    bytes_: int
    workspace_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        version = self.version

        payload = self.payload

        fragment = self.fragment

        bytes_ = self.bytes_

        workspace_id: None | str | Unset
        if isinstance(self.workspace_id, Unset):
            workspace_id = UNSET
        else:
            workspace_id = self.workspace_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "payload": payload,
                "fragment": fragment,
                "bytes": bytes_,
            }
        )
        if workspace_id is not UNSET:
            field_dict["workspaceId"] = workspace_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        version = cast(Literal[1], d.pop("version"))
        if version != 1:
            raise ValueError(f"version must match const 1, got '{version}'")

        payload = d.pop("payload")

        fragment = d.pop("fragment")

        bytes_ = d.pop("bytes")

        def _parse_workspace_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        workspace_id = _parse_workspace_id(d.pop("workspaceId", UNSET))

        context_link = cls(
            version=version,
            payload=payload,
            fragment=fragment,
            bytes_=bytes_,
            workspace_id=workspace_id,
        )

        context_link.additional_properties = d
        return context_link

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
