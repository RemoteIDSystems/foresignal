from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_status_overall import BenchmarkStatusOverall

if TYPE_CHECKING:
    from ..models.benchmark_status_components_item import BenchmarkStatusComponentsItem


T = TypeVar("T", bound="BenchmarkStatus")


@_attrs_define
class BenchmarkStatus:
    """Latest benchmark status per component, used by `BenchmarkGate`

    Attributes:
        generated_at (datetime.datetime):
        overall (BenchmarkStatusOverall):
        components (list[BenchmarkStatusComponentsItem]):
    """

    generated_at: datetime.datetime
    overall: BenchmarkStatusOverall
    components: list[BenchmarkStatusComponentsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        generated_at = self.generated_at.isoformat()

        overall = self.overall.value

        components = []
        for components_item_data in self.components:
            components_item = components_item_data.to_dict()
            components.append(components_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "generatedAt": generated_at,
                "overall": overall,
                "components": components,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_status_components_item import (
            BenchmarkStatusComponentsItem,
        )

        d = dict(src_dict)
        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        overall = BenchmarkStatusOverall(d.pop("overall"))

        components = []
        _components = d.pop("components")
        for components_item_data in _components:
            components_item = BenchmarkStatusComponentsItem.from_dict(
                components_item_data
            )

            components.append(components_item)

        benchmark_status = cls(
            generated_at=generated_at,
            overall=overall,
            components=components,
        )

        benchmark_status.additional_properties = d
        return benchmark_status

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
