from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="S3Target")


@_attrs_define
class S3Target:
    """S3-compatible target (SigV4; endpoint checked by the outbound URL policy)

    Attributes:
        endpoint (str):
        bucket (str):
        prefix (str | Unset):
        region (str | Unset):
        path_style (bool | Unset):
    """

    endpoint: str
    bucket: str
    prefix: str | Unset = UNSET
    region: str | Unset = UNSET
    path_style: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        endpoint = self.endpoint

        bucket = self.bucket

        prefix = self.prefix

        region = self.region

        path_style = self.path_style

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "endpoint": endpoint,
                "bucket": bucket,
            }
        )
        if prefix is not UNSET:
            field_dict["prefix"] = prefix
        if region is not UNSET:
            field_dict["region"] = region
        if path_style is not UNSET:
            field_dict["pathStyle"] = path_style

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        endpoint = d.pop("endpoint")

        bucket = d.pop("bucket")

        prefix = d.pop("prefix", UNSET)

        region = d.pop("region", UNSET)

        path_style = d.pop("pathStyle", UNSET)

        s3_target = cls(
            endpoint=endpoint,
            bucket=bucket,
            prefix=prefix,
            region=region,
            path_style=path_style,
        )

        s3_target.additional_properties = d
        return s3_target

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
