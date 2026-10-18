from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalCatalogueEntryTimeDimensionType0")


@_attrs_define
class ExternalCatalogueEntryTimeDimensionType0:
    """Time dimension of the external layer

    Attributes:
        format_ (str | Unset): Time value format in the template Example: YYYY-MM-DDThh:mm:ssZ.
        period (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: PT10M.
        typical_latency (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: PT30M.
        default_lag (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: PT40M.
    """

    format_: str | Unset = UNSET
    period: str | Unset = UNSET
    typical_latency: str | Unset = UNSET
    default_lag: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        format_ = self.format_

        period = self.period

        typical_latency = self.typical_latency

        default_lag = self.default_lag

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if format_ is not UNSET:
            field_dict["format"] = format_
        if period is not UNSET:
            field_dict["period"] = period
        if typical_latency is not UNSET:
            field_dict["typicalLatency"] = typical_latency
        if default_lag is not UNSET:
            field_dict["defaultLag"] = default_lag

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        format_ = d.pop("format", UNSET)

        period = d.pop("period", UNSET)

        typical_latency = d.pop("typicalLatency", UNSET)

        default_lag = d.pop("defaultLag", UNSET)

        external_catalogue_entry_time_dimension_type_0 = cls(
            format_=format_,
            period=period,
            typical_latency=typical_latency,
            default_lag=default_lag,
        )

        external_catalogue_entry_time_dimension_type_0.additional_properties = d
        return external_catalogue_entry_time_dimension_type_0

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
