from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.correlation_recipe_update_params import CorrelationRecipeUpdateParams


T = TypeVar("T", bound="CorrelationRecipeUpdate")


@_attrs_define
class CorrelationRecipeUpdate:
    """Operator changes to a recipe

    Attributes:
        enabled (bool | Unset):
        params (CorrelationRecipeUpdateParams | Unset): Parameter overrides
    """

    enabled: bool | Unset = UNSET
    params: CorrelationRecipeUpdateParams | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.correlation_recipe_update_params import (
            CorrelationRecipeUpdateParams,
        )

        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        _params = d.pop("params", UNSET)
        params: CorrelationRecipeUpdateParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = CorrelationRecipeUpdateParams.from_dict(_params)

        correlation_recipe_update = cls(
            enabled=enabled,
            params=params,
        )

        return correlation_recipe_update
