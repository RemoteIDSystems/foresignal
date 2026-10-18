from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorTestResultLag")


@_attrs_define
class PrecursorTestResultLag:
    """Lag distribution of hits (ISO-8601 durations)

    Attributes:
        min_ (None | str | Unset):
        p25 (None | str | Unset):
        median (None | str | Unset):
        p75 (None | str | Unset):
        max_ (None | str | Unset):
        mean (None | str | Unset):
    """

    min_: None | str | Unset = UNSET
    p25: None | str | Unset = UNSET
    median: None | str | Unset = UNSET
    p75: None | str | Unset = UNSET
    max_: None | str | Unset = UNSET
    mean: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_: None | str | Unset
        if isinstance(self.min_, Unset):
            min_ = UNSET
        else:
            min_ = self.min_

        p25: None | str | Unset
        if isinstance(self.p25, Unset):
            p25 = UNSET
        else:
            p25 = self.p25

        median: None | str | Unset
        if isinstance(self.median, Unset):
            median = UNSET
        else:
            median = self.median

        p75: None | str | Unset
        if isinstance(self.p75, Unset):
            p75 = UNSET
        else:
            p75 = self.p75

        max_: None | str | Unset
        if isinstance(self.max_, Unset):
            max_ = UNSET
        else:
            max_ = self.max_

        mean: None | str | Unset
        if isinstance(self.mean, Unset):
            mean = UNSET
        else:
            mean = self.mean

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_ is not UNSET:
            field_dict["min"] = min_
        if p25 is not UNSET:
            field_dict["p25"] = p25
        if median is not UNSET:
            field_dict["median"] = median
        if p75 is not UNSET:
            field_dict["p75"] = p75
        if max_ is not UNSET:
            field_dict["max"] = max_
        if mean is not UNSET:
            field_dict["mean"] = mean

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_min_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        min_ = _parse_min_(d.pop("min", UNSET))

        def _parse_p25(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        p25 = _parse_p25(d.pop("p25", UNSET))

        def _parse_median(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        median = _parse_median(d.pop("median", UNSET))

        def _parse_p75(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        p75 = _parse_p75(d.pop("p75", UNSET))

        def _parse_max_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        max_ = _parse_max_(d.pop("max", UNSET))

        def _parse_mean(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        mean = _parse_mean(d.pop("mean", UNSET))

        precursor_test_result_lag = cls(
            min_=min_,
            p25=p25,
            median=median,
            p75=p75,
            max_=max_,
            mean=mean,
        )

        precursor_test_result_lag.additional_properties = d
        return precursor_test_result_lag

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
