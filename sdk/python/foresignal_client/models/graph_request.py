from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.graph_request_types_item import GraphRequestTypesItem
from ..types import UNSET, Unset

T = TypeVar("T", bound="GraphRequest")


@_attrs_define
class GraphRequest:
    """Subgraph query from seed refs

    Attributes:
        refs (list[str]):
        depth (int | Unset):  Default: 1.
        types (list[GraphRequestTypesItem] | Unset):
        as_of (datetime.datetime | None | Unset):
        min_confidence (float | Unset):
        limit (int | Unset):  Default: 500.
    """

    refs: list[str]
    depth: int | Unset = 1
    types: list[GraphRequestTypesItem] | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    min_confidence: float | Unset = UNSET
    limit: int | Unset = 500
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        refs = self.refs

        depth = self.depth

        types: list[str] | Unset = UNSET
        if not isinstance(self.types, Unset):
            types = []
            for types_item_data in self.types:
                types_item = types_item_data.value
                types.append(types_item)

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        min_confidence = self.min_confidence

        limit = self.limit

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "refs": refs,
            }
        )
        if depth is not UNSET:
            field_dict["depth"] = depth
        if types is not UNSET:
            field_dict["types"] = types
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if min_confidence is not UNSET:
            field_dict["minConfidence"] = min_confidence
        if limit is not UNSET:
            field_dict["limit"] = limit

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        refs = cast(list[str], d.pop("refs"))

        depth = d.pop("depth", UNSET)

        _types = d.pop("types", UNSET)
        types: list[GraphRequestTypesItem] | Unset = UNSET
        if _types is not UNSET:
            types = []
            for types_item_data in _types:
                types_item = GraphRequestTypesItem(types_item_data)

                types.append(types_item)

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

        min_confidence = d.pop("minConfidence", UNSET)

        limit = d.pop("limit", UNSET)

        graph_request = cls(
            refs=refs,
            depth=depth,
            types=types,
            as_of=as_of,
            min_confidence=min_confidence,
            limit=limit,
        )

        graph_request.additional_properties = d
        return graph_request

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
