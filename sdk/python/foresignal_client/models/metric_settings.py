from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.metric_projection import MetricProjection


T = TypeVar("T", bound="MetricSettings")


@_attrs_define
class MetricSettings:
    """Metric projections served at /v1/metrics (038)

    Attributes:
        projections (list[MetricProjection]):
    """

    projections: list[MetricProjection]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        projections = []
        for projections_item_data in self.projections:
            projections_item = projections_item_data.to_dict()
            projections.append(projections_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "projections": projections,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.metric_projection import MetricProjection

        d = dict(src_dict)
        projections = []
        _projections = d.pop("projections")
        for projections_item_data in _projections:
            projections_item = MetricProjection.from_dict(projections_item_data)

            projections.append(projections_item)

        metric_settings = cls(
            projections=projections,
        )

        metric_settings.additional_properties = d
        return metric_settings

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
