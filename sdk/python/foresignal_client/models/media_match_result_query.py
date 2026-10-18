from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.media_match_result_query_source import MediaMatchResultQuerySource
from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaMatchResultQuery")


@_attrs_define
class MediaMatchResultQuery:
    """The query hash

    Attributes:
        pdq (str): 256-bit PDQ hash, hex
        source (MediaMatchResultQuerySource):
        quality (int | None | Unset):
    """

    pdq: str
    source: MediaMatchResultQuerySource
    quality: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pdq = self.pdq

        source = self.source.value

        quality: int | None | Unset
        if isinstance(self.quality, Unset):
            quality = UNSET
        else:
            quality = self.quality

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "pdq": pdq,
                "source": source,
            }
        )
        if quality is not UNSET:
            field_dict["quality"] = quality

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        pdq = d.pop("pdq")

        source = MediaMatchResultQuerySource(d.pop("source"))

        def _parse_quality(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        quality = _parse_quality(d.pop("quality", UNSET))

        media_match_result_query = cls(
            pdq=pdq,
            source=source,
            quality=quality,
        )

        media_match_result_query.additional_properties = d
        return media_match_result_query

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
