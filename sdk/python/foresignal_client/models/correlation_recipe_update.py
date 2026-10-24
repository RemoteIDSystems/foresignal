from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.correlation_recipe_definition import CorrelationRecipeDefinition
    from ..models.correlation_recipe_update_params_type_0 import (
        CorrelationRecipeUpdateParamsType0,
    )


T = TypeVar("T", bound="CorrelationRecipeUpdate")


@_attrs_define
class CorrelationRecipeUpdate:
    """Operator changes to a recipe: the enabled flag and parameter overrides, and for an operator-authored recipe a full
    definition to replace (prompt 157).

        Attributes:
            enabled (bool | Unset):
            params (CorrelationRecipeUpdateParamsType0 | None | Unset): Parameter overrides; null removes them
            definition (CorrelationRecipeDefinition | Unset): Recipe definition: seed, joins, thresholds, contradictions,
                dedup and lifecycle. Conditions use the 017 condition grammar.
    """

    enabled: bool | Unset = UNSET
    params: CorrelationRecipeUpdateParamsType0 | None | Unset = UNSET
    definition: CorrelationRecipeDefinition | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.correlation_recipe_update_params_type_0 import (
            CorrelationRecipeUpdateParamsType0,
        )

        enabled = self.enabled

        params: dict[str, Any] | None | Unset
        if isinstance(self.params, Unset):
            params = UNSET
        elif isinstance(self.params, CorrelationRecipeUpdateParamsType0):
            params = self.params.to_dict()
        else:
            params = self.params

        definition: dict[str, Any] | Unset = UNSET
        if not isinstance(self.definition, Unset):
            definition = self.definition.to_dict()

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if params is not UNSET:
            field_dict["params"] = params
        if definition is not UNSET:
            field_dict["definition"] = definition

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.correlation_recipe_definition import CorrelationRecipeDefinition
        from ..models.correlation_recipe_update_params_type_0 import (
            CorrelationRecipeUpdateParamsType0,
        )

        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        def _parse_params(
            data: object,
        ) -> CorrelationRecipeUpdateParamsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                params_type_0 = CorrelationRecipeUpdateParamsType0.from_dict(data)

                return params_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CorrelationRecipeUpdateParamsType0 | None | Unset, data)

        params = _parse_params(d.pop("params", UNSET))

        _definition = d.pop("definition", UNSET)
        definition: CorrelationRecipeDefinition | Unset
        if isinstance(_definition, Unset):
            definition = UNSET
        else:
            definition = CorrelationRecipeDefinition.from_dict(_definition)

        correlation_recipe_update = cls(
            enabled=enabled,
            params=params,
            definition=definition,
        )

        return correlation_recipe_update
