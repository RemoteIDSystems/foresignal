from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_run_input_suite import BenchmarkRunInputSuite
from ..types import UNSET, Unset

T = TypeVar("T", bound="BenchmarkRunInput")


@_attrs_define
class BenchmarkRunInput:
    """Request to start a benchmark run (a 103 job of type `benchmark`)

    Attributes:
        suite (BenchmarkRunInputSuite):
        components (list[str] | Unset): Components to benchmark; default all built components
        seed (int | Unset): Scenario seed; default the suite's fixed seed
    """

    suite: BenchmarkRunInputSuite
    components: list[str] | Unset = UNSET
    seed: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        suite = self.suite.value

        components: list[str] | Unset = UNSET
        if not isinstance(self.components, Unset):
            components = self.components

        seed = self.seed

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "suite": suite,
            }
        )
        if components is not UNSET:
            field_dict["components"] = components
        if seed is not UNSET:
            field_dict["seed"] = seed

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        suite = BenchmarkRunInputSuite(d.pop("suite"))

        components = cast(list[str], d.pop("components", UNSET))

        seed = d.pop("seed", UNSET)

        benchmark_run_input = cls(
            suite=suite,
            components=components,
            seed=seed,
        )

        benchmark_run_input.additional_properties = d
        return benchmark_run_input

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
