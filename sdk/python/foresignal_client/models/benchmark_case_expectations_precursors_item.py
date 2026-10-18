from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_case_expectations_precursors_item_expected import (
    BenchmarkCaseExpectationsPrecursorsItemExpected,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="BenchmarkCaseExpectationsPrecursorsItem")


@_attrs_define
class BenchmarkCaseExpectationsPrecursorsItem:
    """Expected precursor outcome

    Attributes:
        expected (BenchmarkCaseExpectationsPrecursorsItemExpected):
        precursor_id (None | str | Unset):
        leader (None | str | Unset): Leading family or series ref
        follower (None | str | Unset): Following family or series ref
        lag_hours (float | None | Unset):
    """

    expected: BenchmarkCaseExpectationsPrecursorsItemExpected
    precursor_id: None | str | Unset = UNSET
    leader: None | str | Unset = UNSET
    follower: None | str | Unset = UNSET
    lag_hours: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        expected = self.expected.value

        precursor_id: None | str | Unset
        if isinstance(self.precursor_id, Unset):
            precursor_id = UNSET
        else:
            precursor_id = self.precursor_id

        leader: None | str | Unset
        if isinstance(self.leader, Unset):
            leader = UNSET
        else:
            leader = self.leader

        follower: None | str | Unset
        if isinstance(self.follower, Unset):
            follower = UNSET
        else:
            follower = self.follower

        lag_hours: float | None | Unset
        if isinstance(self.lag_hours, Unset):
            lag_hours = UNSET
        else:
            lag_hours = self.lag_hours

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "expected": expected,
            }
        )
        if precursor_id is not UNSET:
            field_dict["precursorId"] = precursor_id
        if leader is not UNSET:
            field_dict["leader"] = leader
        if follower is not UNSET:
            field_dict["follower"] = follower
        if lag_hours is not UNSET:
            field_dict["lagHours"] = lag_hours

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        expected = BenchmarkCaseExpectationsPrecursorsItemExpected(d.pop("expected"))

        def _parse_precursor_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        precursor_id = _parse_precursor_id(d.pop("precursorId", UNSET))

        def _parse_leader(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        leader = _parse_leader(d.pop("leader", UNSET))

        def _parse_follower(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        follower = _parse_follower(d.pop("follower", UNSET))

        def _parse_lag_hours(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lag_hours = _parse_lag_hours(d.pop("lagHours", UNSET))

        benchmark_case_expectations_precursors_item = cls(
            expected=expected,
            precursor_id=precursor_id,
            leader=leader,
            follower=follower,
            lag_hours=lag_hours,
        )

        benchmark_case_expectations_precursors_item.additional_properties = d
        return benchmark_case_expectations_precursors_item

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
