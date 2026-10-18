from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SnapshotInput")


@_attrs_define
class SnapshotInput:
    """Create an atSeq snapshot: state at seq S (default the current watermarkSeq, or asOf translated with S(T)) (111)

    Attributes:
        name (None | str | Unset):
        at_seq (int | None | Unset):
        as_of (datetime.datetime | None | Unset):
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        include_private (bool | Unset): Include private sources (039); the endpoint is admin-only Default: False.
        personal_use (bool | Unset): Include FORBIDDEN and LOCAL_ONLY sources; the sidecar then says personal use only —
            do not redistribute Default: False.
        expires_at (datetime.datetime | None | Unset): Snapshots never expire unless set
    """

    name: None | str | Unset = UNSET
    at_seq: int | None | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    include_private: bool | Unset = False
    personal_use: bool | Unset = False
    expires_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        at_seq: int | None | Unset
        if isinstance(self.at_seq, Unset):
            at_seq = UNSET
        else:
            at_seq = self.at_seq

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        include_private = self.include_private

        personal_use = self.personal_use

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if at_seq is not UNSET:
            field_dict["atSeq"] = at_seq
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if include_private is not UNSET:
            field_dict["includePrivate"] = include_private
        if personal_use is not UNSET:
            field_dict["personalUse"] = personal_use
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        def _parse_at_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        at_seq = _parse_at_seq(d.pop("atSeq", UNSET))

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        include_private = d.pop("includePrivate", UNSET)

        personal_use = d.pop("personalUse", UNSET)

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        snapshot_input = cls(
            name=name,
            at_seq=at_seq,
            as_of=as_of,
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            include_private=include_private,
            personal_use=personal_use,
            expires_at=expires_at,
        )

        snapshot_input.additional_properties = d
        return snapshot_input

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
