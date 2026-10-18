from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorTestResultHitRateCi")


@_attrs_define
class PrecursorTestResultHitRateCi:
    """Wilson 95% interval

    Attributes:
        lower (float | Unset):
        upper (float | Unset):
        method (Literal['wilson-95'] | Unset):
    """

    lower: float | Unset = UNSET
    upper: float | Unset = UNSET
    method: Literal["wilson-95"] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lower = self.lower

        upper = self.upper

        method = self.method

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if lower is not UNSET:
            field_dict["lower"] = lower
        if upper is not UNSET:
            field_dict["upper"] = upper
        if method is not UNSET:
            field_dict["method"] = method

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lower = d.pop("lower", UNSET)

        upper = d.pop("upper", UNSET)

        method = cast(Literal["wilson-95"] | Unset, d.pop("method", UNSET))
        if method != "wilson-95" and not isinstance(method, Unset):
            raise ValueError(f"method must match const 'wilson-95', got '{method}'")

        precursor_test_result_hit_rate_ci = cls(
            lower=lower,
            upper=upper,
            method=method,
        )

        precursor_test_result_hit_rate_ci.additional_properties = d
        return precursor_test_result_hit_rate_ci

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
