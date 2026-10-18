from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.context_summary_request_filters import ContextSummaryRequestFilters
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="ContextSummaryRequest")


@_attrs_define
class ContextSummaryRequest:
    """A viewport, window and filters to summarise

    Attributes:
        viewport (Any): Viewport: either `bbox` or `center` + `radiusKm`
        time_range (TimeRange):
        as_of (datetime.datetime | None | Unset):
        filters (ContextSummaryRequestFilters | Unset):
        layers (list[str] | Unset):
    """

    viewport: Any
    time_range: TimeRange
    as_of: datetime.datetime | None | Unset = UNSET
    filters: ContextSummaryRequestFilters | Unset = UNSET
    layers: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        viewport: Any
        viewport = self.viewport

        time_range = self.time_range.to_dict()

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        layers: list[str] | Unset = UNSET
        if not isinstance(self.layers, Unset):
            layers = self.layers

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "viewport": viewport,
                "timeRange": time_range,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if filters is not UNSET:
            field_dict["filters"] = filters
        if layers is not UNSET:
            field_dict["layers"] = layers

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.context_summary_request_filters import (
            ContextSummaryRequestFilters,
        )
        from ..models.time_range import TimeRange

        d = dict(src_dict)

        def _parse_viewport(data: object) -> Any:
            return cast(Any, data)

        viewport = _parse_viewport(d.pop("viewport"))

        time_range = TimeRange.from_dict(d.pop("timeRange"))

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

        _filters = d.pop("filters", UNSET)
        filters: ContextSummaryRequestFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = ContextSummaryRequestFilters.from_dict(_filters)

        layers = cast(list[str], d.pop("layers", UNSET))

        context_summary_request = cls(
            viewport=viewport,
            time_range=time_range,
            as_of=as_of,
            filters=filters,
            layers=layers,
        )

        context_summary_request.additional_properties = d
        return context_summary_request

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
