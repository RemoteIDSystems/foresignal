from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.ensemble_refit_result_extremizing import (
        EnsembleRefitResultExtremizing,
    )
    from ..models.ensemble_refit_result_platt_item import EnsembleRefitResultPlattItem
    from ..models.ensemble_refit_result_weights_item import (
        EnsembleRefitResultWeightsItem,
    )


T = TypeVar("T", bound="EnsembleRefitResult")


@_attrs_define
class EnsembleRefitResult:
    """Outcome of the monthly ensemble refit: weights, Platt fits and the extremizing gate

    Attributes:
        fitted_at (datetime.datetime):
        extremizing (EnsembleRefitResultExtremizing): Extremizing factor a and whether it is enabled
        platt (list[EnsembleRefitResultPlattItem]):
        weights (list[EnsembleRefitResultWeightsItem]):
    """

    fitted_at: datetime.datetime
    extremizing: EnsembleRefitResultExtremizing
    platt: list[EnsembleRefitResultPlattItem]
    weights: list[EnsembleRefitResultWeightsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        fitted_at = self.fitted_at.isoformat()

        extremizing = self.extremizing.to_dict()

        platt = []
        for platt_item_data in self.platt:
            platt_item = platt_item_data.to_dict()
            platt.append(platt_item)

        weights = []
        for weights_item_data in self.weights:
            weights_item = weights_item_data.to_dict()
            weights.append(weights_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fittedAt": fitted_at,
                "extremizing": extremizing,
                "platt": platt,
                "weights": weights,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ensemble_refit_result_extremizing import (
            EnsembleRefitResultExtremizing,
        )
        from ..models.ensemble_refit_result_platt_item import (
            EnsembleRefitResultPlattItem,
        )
        from ..models.ensemble_refit_result_weights_item import (
            EnsembleRefitResultWeightsItem,
        )

        d = dict(src_dict)
        fitted_at = datetime.datetime.fromisoformat(d.pop("fittedAt"))

        extremizing = EnsembleRefitResultExtremizing.from_dict(d.pop("extremizing"))

        platt = []
        _platt = d.pop("platt")
        for platt_item_data in _platt:
            platt_item = EnsembleRefitResultPlattItem.from_dict(platt_item_data)

            platt.append(platt_item)

        weights = []
        _weights = d.pop("weights")
        for weights_item_data in _weights:
            weights_item = EnsembleRefitResultWeightsItem.from_dict(weights_item_data)

            weights.append(weights_item)

        ensemble_refit_result = cls(
            fitted_at=fitted_at,
            extremizing=extremizing,
            platt=platt,
            weights=weights,
        )

        ensemble_refit_result.additional_properties = d
        return ensemble_refit_result

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
