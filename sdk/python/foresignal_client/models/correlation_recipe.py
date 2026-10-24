from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.correlation_recipe_origin import CorrelationRecipeOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.correlation_recipe_definition import CorrelationRecipeDefinition
    from ..models.correlation_recipe_params_type_0 import CorrelationRecipeParamsType0


T = TypeVar("T", bound="CorrelationRecipe")


@_attrs_define
class CorrelationRecipe:
    """A versioned correlation recipe; built-ins load from resources and keep operator param overrides

    Attributes:
        id (str):  Example: maritime-disruption.
        version (int):
        enabled (bool):
        definition (CorrelationRecipeDefinition): Recipe definition: seed, joins, thresholds, contradictions, dedup and
            lifecycle. Conditions use the 017 condition grammar.
        built_in (bool | Unset):
        origin (CorrelationRecipeOrigin | Unset): Whether the recipe is a bundled classpath recipe or operator-authored
            (prompt 157)
        params (CorrelationRecipeParamsType0 | None | Unset): Operator overrides; null when there are none
        disabled_reason (None | str | Unset): e.g. seed kinds missing (recipe loaded disabled with a warning)
        last_run_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
    """

    id: str
    version: int
    enabled: bool
    definition: CorrelationRecipeDefinition
    built_in: bool | Unset = UNSET
    origin: CorrelationRecipeOrigin | Unset = UNSET
    params: CorrelationRecipeParamsType0 | None | Unset = UNSET
    disabled_reason: None | str | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.correlation_recipe_params_type_0 import (
            CorrelationRecipeParamsType0,
        )

        id = self.id

        version = self.version

        enabled = self.enabled

        definition = self.definition.to_dict()

        built_in = self.built_in

        origin: str | Unset = UNSET
        if not isinstance(self.origin, Unset):
            origin = self.origin.value

        params: dict[str, Any] | None | Unset
        if isinstance(self.params, Unset):
            params = UNSET
        elif isinstance(self.params, CorrelationRecipeParamsType0):
            params = self.params.to_dict()
        else:
            params = self.params

        disabled_reason: None | str | Unset
        if isinstance(self.disabled_reason, Unset):
            disabled_reason = UNSET
        else:
            disabled_reason = self.disabled_reason

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "version": version,
                "enabled": enabled,
                "definition": definition,
            }
        )
        if built_in is not UNSET:
            field_dict["builtIn"] = built_in
        if origin is not UNSET:
            field_dict["origin"] = origin
        if params is not UNSET:
            field_dict["params"] = params
        if disabled_reason is not UNSET:
            field_dict["disabledReason"] = disabled_reason
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.correlation_recipe_definition import CorrelationRecipeDefinition
        from ..models.correlation_recipe_params_type_0 import (
            CorrelationRecipeParamsType0,
        )

        d = dict(src_dict)
        id = d.pop("id")

        version = d.pop("version")

        enabled = d.pop("enabled")

        definition = CorrelationRecipeDefinition.from_dict(d.pop("definition"))

        built_in = d.pop("builtIn", UNSET)

        _origin = d.pop("origin", UNSET)
        origin: CorrelationRecipeOrigin | Unset
        if isinstance(_origin, Unset):
            origin = UNSET
        else:
            origin = CorrelationRecipeOrigin(_origin)

        def _parse_params(data: object) -> CorrelationRecipeParamsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                params_type_0 = CorrelationRecipeParamsType0.from_dict(data)

                return params_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CorrelationRecipeParamsType0 | None | Unset, data)

        params = _parse_params(d.pop("params", UNSET))

        def _parse_disabled_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        disabled_reason = _parse_disabled_reason(d.pop("disabledReason", UNSET))

        def _parse_last_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_run_at = _parse_last_run_at(d.pop("lastRunAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        correlation_recipe = cls(
            id=id,
            version=version,
            enabled=enabled,
            definition=definition,
            built_in=built_in,
            origin=origin,
            params=params,
            disabled_reason=disabled_reason,
            last_run_at=last_run_at,
            last_error=last_error,
        )

        correlation_recipe.additional_properties = d
        return correlation_recipe

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
