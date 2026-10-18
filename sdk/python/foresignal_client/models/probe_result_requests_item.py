from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProbeResultRequestsItem")


@_attrs_define
class ProbeResultRequestsItem:
    """
    Attributes:
        method (str | Unset):
        url (str | Unset): Credentials redacted
        status (int | Unset):
        duration_ms (int | Unset):
        content_type (str | Unset):
        bytes_ (int | Unset):
        truncated (bool | Unset):
        body (str | Unset):
    """

    method: str | Unset = UNSET
    url: str | Unset = UNSET
    status: int | Unset = UNSET
    duration_ms: int | Unset = UNSET
    content_type: str | Unset = UNSET
    bytes_: int | Unset = UNSET
    truncated: bool | Unset = UNSET
    body: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        method = self.method

        url = self.url

        status = self.status

        duration_ms = self.duration_ms

        content_type = self.content_type

        bytes_ = self.bytes_

        truncated = self.truncated

        body = self.body

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if method is not UNSET:
            field_dict["method"] = method
        if url is not UNSET:
            field_dict["url"] = url
        if status is not UNSET:
            field_dict["status"] = status
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms
        if content_type is not UNSET:
            field_dict["contentType"] = content_type
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if body is not UNSET:
            field_dict["body"] = body

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        method = d.pop("method", UNSET)

        url = d.pop("url", UNSET)

        status = d.pop("status", UNSET)

        duration_ms = d.pop("durationMs", UNSET)

        content_type = d.pop("contentType", UNSET)

        bytes_ = d.pop("bytes", UNSET)

        truncated = d.pop("truncated", UNSET)

        body = d.pop("body", UNSET)

        probe_result_requests_item = cls(
            method=method,
            url=url,
            status=status,
            duration_ms=duration_ms,
            content_type=content_type,
            bytes_=bytes_,
            truncated=truncated,
            body=body,
        )

        probe_result_requests_item.additional_properties = d
        return probe_result_requests_item

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
