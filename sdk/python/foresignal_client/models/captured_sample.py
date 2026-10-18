from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.captured_sample_method import CapturedSampleMethod
from ..types import UNSET, Unset

T = TypeVar("T", bound="CapturedSample")


@_attrs_define
class CapturedSample:
    """One redacted raw ingest request captured while `captureSamples` is on (059)

    Attributes:
        received_at (datetime.datetime):
        method (CapturedSampleMethod):
        format_ (None | str | Unset): Format path segment, null for the default endpoint
        content_type (None | str | Unset):
        content_encoding (None | str | Unset):  Example: gzip.
        query (None | str | Unset): Redacted query string
        body (None | str | Unset): Redacted, decompressed body (text)
        body_bytes (int | Unset):
        truncated (bool | Unset):
    """

    received_at: datetime.datetime
    method: CapturedSampleMethod
    format_: None | str | Unset = UNSET
    content_type: None | str | Unset = UNSET
    content_encoding: None | str | Unset = UNSET
    query: None | str | Unset = UNSET
    body: None | str | Unset = UNSET
    body_bytes: int | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        received_at = self.received_at.isoformat()

        method = self.method.value

        format_: None | str | Unset
        if isinstance(self.format_, Unset):
            format_ = UNSET
        else:
            format_ = self.format_

        content_type: None | str | Unset
        if isinstance(self.content_type, Unset):
            content_type = UNSET
        else:
            content_type = self.content_type

        content_encoding: None | str | Unset
        if isinstance(self.content_encoding, Unset):
            content_encoding = UNSET
        else:
            content_encoding = self.content_encoding

        query: None | str | Unset
        if isinstance(self.query, Unset):
            query = UNSET
        else:
            query = self.query

        body: None | str | Unset
        if isinstance(self.body, Unset):
            body = UNSET
        else:
            body = self.body

        body_bytes = self.body_bytes

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "receivedAt": received_at,
                "method": method,
            }
        )
        if format_ is not UNSET:
            field_dict["format"] = format_
        if content_type is not UNSET:
            field_dict["contentType"] = content_type
        if content_encoding is not UNSET:
            field_dict["contentEncoding"] = content_encoding
        if query is not UNSET:
            field_dict["query"] = query
        if body is not UNSET:
            field_dict["body"] = body
        if body_bytes is not UNSET:
            field_dict["bodyBytes"] = body_bytes
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        received_at = datetime.datetime.fromisoformat(d.pop("receivedAt"))

        method = CapturedSampleMethod(d.pop("method"))

        def _parse_format_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        format_ = _parse_format_(d.pop("format", UNSET))

        def _parse_content_type(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        content_type = _parse_content_type(d.pop("contentType", UNSET))

        def _parse_content_encoding(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        content_encoding = _parse_content_encoding(d.pop("contentEncoding", UNSET))

        def _parse_query(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        query = _parse_query(d.pop("query", UNSET))

        def _parse_body(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        body = _parse_body(d.pop("body", UNSET))

        body_bytes = d.pop("bodyBytes", UNSET)

        truncated = d.pop("truncated", UNSET)

        captured_sample = cls(
            received_at=received_at,
            method=method,
            format_=format_,
            content_type=content_type,
            content_encoding=content_encoding,
            query=query,
            body=body,
            body_bytes=body_bytes,
            truncated=truncated,
        )

        captured_sample.additional_properties = d
        return captured_sample

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
