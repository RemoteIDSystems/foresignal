from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.export_spec_format import ExportSpecFormat
from ..models.export_spec_mode import ExportSpecMode
from ..models.export_spec_time_field import ExportSpecTimeField
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExportSpec")


@_attrs_define
class ExportSpec:
    """Bulk export request (111). FORBIDDEN and LOCAL_ONLY records are excluded unless personalUse=true

    Attributes:
        name (None | str | Unset):
        format_ (ExportSpecFormat | Unset): gpx requires mode=history Default: ExportSpecFormat.GEOPARQUET.
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        place_id (None | str | Unset):
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        time_field (ExportSpecTimeField | Unset): 014 semantics for from/to
        mode (ExportSpecMode | Unset):  Default: ExportSpecMode.CURRENT.
        as_of (datetime.datetime | None | Unset):
        at_seq (int | None | Unset):
        fields (list[str] | Unset): Payload allowlist
        flatten (bool | Unset): Promote fieldSpecs keys to columns Default: False.
        include_private (bool | Unset): Include private sources (039); admin only Default: False.
        personal_use (bool | Unset): Include FORBIDDEN and LOCAL_ONLY records; the sidecar then says personal use only —
            do not redistribute Default: False.
    """

    name: None | str | Unset = UNSET
    format_: ExportSpecFormat | Unset = ExportSpecFormat.GEOPARQUET
    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    place_id: None | str | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    time_field: ExportSpecTimeField | Unset = UNSET
    mode: ExportSpecMode | Unset = ExportSpecMode.CURRENT
    as_of: datetime.datetime | None | Unset = UNSET
    at_seq: int | None | Unset = UNSET
    fields: list[str] | Unset = UNSET
    flatten: bool | Unset = False
    include_private: bool | Unset = False
    personal_use: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        format_: str | Unset = UNSET
        if not isinstance(self.format_, Unset):
            format_ = self.format_.value

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

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

        time_field: str | Unset = UNSET
        if not isinstance(self.time_field, Unset):
            time_field = self.time_field.value

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        at_seq: int | None | Unset
        if isinstance(self.at_seq, Unset):
            at_seq = UNSET
        else:
            at_seq = self.at_seq

        fields: list[str] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = self.fields

        flatten = self.flatten

        include_private = self.include_private

        personal_use = self.personal_use

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if format_ is not UNSET:
            field_dict["format"] = format_
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if time_field is not UNSET:
            field_dict["timeField"] = time_field
        if mode is not UNSET:
            field_dict["mode"] = mode
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if at_seq is not UNSET:
            field_dict["atSeq"] = at_seq
        if fields is not UNSET:
            field_dict["fields"] = fields
        if flatten is not UNSET:
            field_dict["flatten"] = flatten
        if include_private is not UNSET:
            field_dict["includePrivate"] = include_private
        if personal_use is not UNSET:
            field_dict["personalUse"] = personal_use

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

        _format_ = d.pop("format", UNSET)
        format_: ExportSpecFormat | Unset
        if isinstance(_format_, Unset):
            format_ = UNSET
        else:
            format_ = ExportSpecFormat(_format_)

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

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        _time_field = d.pop("timeField", UNSET)
        time_field: ExportSpecTimeField | Unset
        if isinstance(_time_field, Unset):
            time_field = UNSET
        else:
            time_field = ExportSpecTimeField(_time_field)

        _mode = d.pop("mode", UNSET)
        mode: ExportSpecMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = ExportSpecMode(_mode)

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

        def _parse_at_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        at_seq = _parse_at_seq(d.pop("atSeq", UNSET))

        fields = cast(list[str], d.pop("fields", UNSET))

        flatten = d.pop("flatten", UNSET)

        include_private = d.pop("includePrivate", UNSET)

        personal_use = d.pop("personalUse", UNSET)

        export_spec = cls(
            name=name,
            format_=format_,
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            place_id=place_id,
            from_=from_,
            to=to,
            time_field=time_field,
            mode=mode,
            as_of=as_of,
            at_seq=at_seq,
            fields=fields,
            flatten=flatten,
            include_private=include_private,
            personal_use=personal_use,
        )

        export_spec.additional_properties = d
        return export_spec

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
