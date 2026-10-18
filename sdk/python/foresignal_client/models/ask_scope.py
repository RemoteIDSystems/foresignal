from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AskScope")


@_attrs_define
class AskScope:
    """
    Attributes:
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        bbox (None | str | Unset):
        since (datetime.datetime | None | Unset):
        until (datetime.datetime | None | Unset):
        place_ids (list[str] | Unset): Places (036) resolved server-side to areas; the model sees names and rounded
            distances only. Available after prompt 036: until then a scope with placeIds is refused with 400.
        view_id (None | str | Unset): Saved view id or slug (036) whose predicate is injected
    """

    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    bbox: None | str | Unset = UNSET
    since: datetime.datetime | None | Unset = UNSET
    until: datetime.datetime | None | Unset = UNSET
    place_ids: list[str] | Unset = UNSET
    view_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        bbox: None | str | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        else:
            bbox = self.bbox

        since: None | str | Unset
        if isinstance(self.since, Unset):
            since = UNSET
        elif isinstance(self.since, datetime.datetime):
            since = self.since.isoformat()
        else:
            since = self.since

        until: None | str | Unset
        if isinstance(self.until, Unset):
            until = UNSET
        elif isinstance(self.until, datetime.datetime):
            until = self.until.isoformat()
        else:
            until = self.until

        place_ids: list[str] | Unset = UNSET
        if not isinstance(self.place_ids, Unset):
            place_ids = self.place_ids

        view_id: None | str | Unset
        if isinstance(self.view_id, Unset):
            view_id = UNSET
        else:
            view_id = self.view_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if since is not UNSET:
            field_dict["since"] = since
        if until is not UNSET:
            field_dict["until"] = until
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if view_id is not UNSET:
            field_dict["viewId"] = view_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        def _parse_bbox(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        def _parse_since(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                since_type_0 = datetime.datetime.fromisoformat(data)

                return since_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        since = _parse_since(d.pop("since", UNSET))

        def _parse_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                until_type_0 = datetime.datetime.fromisoformat(data)

                return until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        until = _parse_until(d.pop("until", UNSET))

        place_ids = cast(list[str], d.pop("placeIds", UNSET))

        def _parse_view_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        view_id = _parse_view_id(d.pop("viewId", UNSET))

        ask_scope = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            since=since,
            until=until,
            place_ids=place_ids,
            view_id=view_id,
        )

        ask_scope.additional_properties = d
        return ask_scope

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
