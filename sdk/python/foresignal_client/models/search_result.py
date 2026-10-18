from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.search_result_doc_type import SearchResultDocType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.record import Record


T = TypeVar("T", bound="SearchResult")


@_attrs_define
class SearchResult:
    """
    Attributes:
        doc_type (SearchResultDocType):
        doc_id (str):
        score (float):
        title (str):
        snippet (str | Unset):
        observed_at (datetime.datetime | None | Unset):
        source (None | str | Unset):
        kind (None | str | Unset):
        geometry (Geometry | None | Unset):
        record (None | Record | Unset):
        seq (int | None | Unset): Persisted version seq the document text came from (cite with it)
        effective_time (datetime.datetime | None | Unset): COALESCE(eventTime, observedAt) of that version
    """

    doc_type: SearchResultDocType
    doc_id: str
    score: float
    title: str
    snippet: str | Unset = UNSET
    observed_at: datetime.datetime | None | Unset = UNSET
    source: None | str | Unset = UNSET
    kind: None | str | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    record: None | Record | Unset = UNSET
    seq: int | None | Unset = UNSET
    effective_time: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.record import Record

        doc_type = self.doc_type.value

        doc_id = self.doc_id

        score = self.score

        title = self.title

        snippet = self.snippet

        observed_at: None | str | Unset
        if isinstance(self.observed_at, Unset):
            observed_at = UNSET
        elif isinstance(self.observed_at, datetime.datetime):
            observed_at = self.observed_at.isoformat()
        else:
            observed_at = self.observed_at

        source: None | str | Unset
        if isinstance(self.source, Unset):
            source = UNSET
        else:
            source = self.source

        kind: None | str | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        else:
            kind = self.kind

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        record: dict[str, Any] | None | Unset
        if isinstance(self.record, Unset):
            record = UNSET
        elif isinstance(self.record, Record):
            record = self.record.to_dict()
        else:
            record = self.record

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        effective_time: None | str | Unset
        if isinstance(self.effective_time, Unset):
            effective_time = UNSET
        elif isinstance(self.effective_time, datetime.datetime):
            effective_time = self.effective_time.isoformat()
        else:
            effective_time = self.effective_time

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "docType": doc_type,
                "docId": doc_id,
                "score": score,
                "title": title,
            }
        )
        if snippet is not UNSET:
            field_dict["snippet"] = snippet
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if source is not UNSET:
            field_dict["source"] = source
        if kind is not UNSET:
            field_dict["kind"] = kind
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if record is not UNSET:
            field_dict["record"] = record
        if seq is not UNSET:
            field_dict["seq"] = seq
        if effective_time is not UNSET:
            field_dict["effectiveTime"] = effective_time

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.record import Record

        d = dict(src_dict)
        doc_type = SearchResultDocType(d.pop("docType"))

        doc_id = d.pop("docId")

        score = d.pop("score")

        title = d.pop("title")

        snippet = d.pop("snippet", UNSET)

        def _parse_observed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                observed_at_type_0 = datetime.datetime.fromisoformat(data)

                return observed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        observed_at = _parse_observed_at(d.pop("observedAt", UNSET))

        def _parse_source(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source = _parse_source(d.pop("source", UNSET))

        def _parse_kind(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind = _parse_kind(d.pop("kind", UNSET))

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_record(data: object) -> None | Record | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                record_type_0 = Record.from_dict(data)

                return record_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Record | Unset, data)

        record = _parse_record(d.pop("record", UNSET))

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_effective_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                effective_time_type_0 = datetime.datetime.fromisoformat(data)

                return effective_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        effective_time = _parse_effective_time(d.pop("effectiveTime", UNSET))

        search_result = cls(
            doc_type=doc_type,
            doc_id=doc_id,
            score=score,
            title=title,
            snippet=snippet,
            observed_at=observed_at,
            source=source,
            kind=kind,
            geometry=geometry,
            record=record,
            seq=seq,
            effective_time=effective_time,
        )

        search_result.additional_properties = d
        return search_result

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
