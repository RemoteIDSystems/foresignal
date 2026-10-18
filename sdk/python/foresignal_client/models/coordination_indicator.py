from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CoordinationIndicator")


@_attrs_define
class CoordinationIndicator:
    """One coordination statistic against its null model

    Attributes:
        value (float | None):
        null_expectation (float | None):
        p_value (float | None):
        available (bool): False when the platform does not provide the data (e.g. account creation dates)
        q_value (float | None | Unset): Benjamini–Hochberg adjusted across the day's narratives
        n (int | Unset):
    """

    value: float | None
    null_expectation: float | None
    p_value: float | None
    available: bool
    q_value: float | None | Unset = UNSET
    n: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        value: float | None
        value = self.value

        null_expectation: float | None
        null_expectation = self.null_expectation

        p_value: float | None
        p_value = self.p_value

        available = self.available

        q_value: float | None | Unset
        if isinstance(self.q_value, Unset):
            q_value = UNSET
        else:
            q_value = self.q_value

        n = self.n

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "value": value,
                "nullExpectation": null_expectation,
                "pValue": p_value,
                "available": available,
            }
        )
        if q_value is not UNSET:
            field_dict["qValue"] = q_value
        if n is not UNSET:
            field_dict["n"] = n

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_value(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        value = _parse_value(d.pop("value"))

        def _parse_null_expectation(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        null_expectation = _parse_null_expectation(d.pop("nullExpectation"))

        def _parse_p_value(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        p_value = _parse_p_value(d.pop("pValue"))

        available = d.pop("available")

        def _parse_q_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        q_value = _parse_q_value(d.pop("qValue", UNSET))

        n = d.pop("n", UNSET)

        coordination_indicator = cls(
            value=value,
            null_expectation=null_expectation,
            p_value=p_value,
            available=available,
            q_value=q_value,
            n=n,
        )

        coordination_indicator.additional_properties = d
        return coordination_indicator

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
