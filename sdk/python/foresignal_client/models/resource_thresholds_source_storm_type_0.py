from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="ResourceThresholdsSourceStormType0")


@_attrs_define
class ResourceThresholdsSourceStormType0:
    """Raises `system.source_storm` when at least this many sources are failing at once (R2-014)

    Attributes:
        failed_sources (int | None | Unset): null turns the storm alert off
    """

    failed_sources: int | None | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        failed_sources: int | None | Unset
        if isinstance(self.failed_sources, Unset):
            failed_sources = UNSET
        else:
            failed_sources = self.failed_sources

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if failed_sources is not UNSET:
            field_dict["failedSources"] = failed_sources

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_failed_sources(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        failed_sources = _parse_failed_sources(d.pop("failedSources", UNSET))

        resource_thresholds_source_storm_type_0 = cls(
            failed_sources=failed_sources,
        )

        return resource_thresholds_source_storm_type_0
