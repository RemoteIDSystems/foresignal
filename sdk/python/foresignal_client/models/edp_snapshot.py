from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpSnapshot")


@_attrs_define
class EdpSnapshot:
    """Marks the batch as a page of a full snapshot of one scope of a snapshot stream. Absent ids are tombstoned only when
    the final page arrives, every page validated, within the snapshot window, and the absent share is within the safety
    limit.

        Attributes:
            id (str):
            stream (str):
            page (int):
            final (bool):
            scope (None | str | Unset): Value of the stream's snapshotScope property this snapshot covers; null = the whole
                stream
    """

    id: str
    stream: str
    page: int
    final: bool
    scope: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        stream = self.stream

        page = self.page

        final = self.final

        scope: None | str | Unset
        if isinstance(self.scope, Unset):
            scope = UNSET
        else:
            scope = self.scope

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "stream": stream,
                "page": page,
                "final": final,
            }
        )
        if scope is not UNSET:
            field_dict["scope"] = scope

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        stream = d.pop("stream")

        page = d.pop("page")

        final = d.pop("final")

        def _parse_scope(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        scope = _parse_scope(d.pop("scope", UNSET))

        edp_snapshot = cls(
            id=id,
            stream=stream,
            page=page,
            final=final,
            scope=scope,
        )

        edp_snapshot.additional_properties = d
        return edp_snapshot

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
