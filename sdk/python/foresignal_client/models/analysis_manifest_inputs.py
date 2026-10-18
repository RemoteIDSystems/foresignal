from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_manifest_inputs_query_type_0 import (
        AnalysisManifestInputsQueryType0,
    )


T = TypeVar("T", bound="AnalysisManifestInputs")


@_attrs_define
class AnalysisManifestInputs:
    """Explicit refs (≤ 10,000) or the query definition plus a hash of the ordered refs

    Attributes:
        count (int):
        hash_ (str):
        refs (list[str] | None | Unset):
        query (AnalysisManifestInputsQueryType0 | None | Unset):
    """

    count: int
    hash_: str
    refs: list[str] | None | Unset = UNSET
    query: AnalysisManifestInputsQueryType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.analysis_manifest_inputs_query_type_0 import (
            AnalysisManifestInputsQueryType0,
        )

        count = self.count

        hash_ = self.hash_

        refs: list[str] | None | Unset
        if isinstance(self.refs, Unset):
            refs = UNSET
        elif isinstance(self.refs, list):
            refs = self.refs

        else:
            refs = self.refs

        query: dict[str, Any] | None | Unset
        if isinstance(self.query, Unset):
            query = UNSET
        elif isinstance(self.query, AnalysisManifestInputsQueryType0):
            query = self.query.to_dict()
        else:
            query = self.query

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "count": count,
                "hash": hash_,
            }
        )
        if refs is not UNSET:
            field_dict["refs"] = refs
        if query is not UNSET:
            field_dict["query"] = query

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_manifest_inputs_query_type_0 import (
            AnalysisManifestInputsQueryType0,
        )

        d = dict(src_dict)
        count = d.pop("count")

        hash_ = d.pop("hash")

        def _parse_refs(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                refs_type_0 = cast(list[str], data)

                return refs_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        refs = _parse_refs(d.pop("refs", UNSET))

        def _parse_query(
            data: object,
        ) -> AnalysisManifestInputsQueryType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                query_type_0 = AnalysisManifestInputsQueryType0.from_dict(data)

                return query_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalysisManifestInputsQueryType0 | None | Unset, data)

        query = _parse_query(d.pop("query", UNSET))

        analysis_manifest_inputs = cls(
            count=count,
            hash_=hash_,
            refs=refs,
            query=query,
        )

        analysis_manifest_inputs.additional_properties = d
        return analysis_manifest_inputs

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
