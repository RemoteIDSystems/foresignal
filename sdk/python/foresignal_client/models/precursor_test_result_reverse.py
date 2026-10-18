from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorTestResultReverse")


@_attrs_define
class PrecursorTestResultReverse:
    """Same test with roles swapped

    Attributes:
        hits (int | Unset):
        hit_rate (float | None | Unset):
        lift (float | None | Unset):
        p_value (float | None | Unset):
    """

    hits: int | Unset = UNSET
    hit_rate: float | None | Unset = UNSET
    lift: float | None | Unset = UNSET
    p_value: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hits = self.hits

        hit_rate: float | None | Unset
        if isinstance(self.hit_rate, Unset):
            hit_rate = UNSET
        else:
            hit_rate = self.hit_rate

        lift: float | None | Unset
        if isinstance(self.lift, Unset):
            lift = UNSET
        else:
            lift = self.lift

        p_value: float | None | Unset
        if isinstance(self.p_value, Unset):
            p_value = UNSET
        else:
            p_value = self.p_value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if hits is not UNSET:
            field_dict["hits"] = hits
        if hit_rate is not UNSET:
            field_dict["hitRate"] = hit_rate
        if lift is not UNSET:
            field_dict["lift"] = lift
        if p_value is not UNSET:
            field_dict["pValue"] = p_value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hits = d.pop("hits", UNSET)

        def _parse_hit_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        hit_rate = _parse_hit_rate(d.pop("hitRate", UNSET))

        def _parse_lift(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lift = _parse_lift(d.pop("lift", UNSET))

        def _parse_p_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_value = _parse_p_value(d.pop("pValue", UNSET))

        precursor_test_result_reverse = cls(
            hits=hits,
            hit_rate=hit_rate,
            lift=lift,
            p_value=p_value,
        )

        precursor_test_result_reverse.additional_properties = d
        return precursor_test_result_reverse

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
