from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.correlation_recipe_definition import CorrelationRecipeDefinition


T = TypeVar("T", bound="CorrelationRecipeCreate")


@_attrs_define
class CorrelationRecipeCreate:
    """Authors a new operator correlation recipe (prompt 157).

    Attributes:
        id (str): A slug not already in use and not a bundled recipe id
        definition (CorrelationRecipeDefinition): Recipe definition: seed, joins, thresholds, contradictions, dedup and
            lifecycle. Conditions use the 017 condition grammar.
        enabled (bool | Unset): Defaults to true
    """

    id: str
    definition: CorrelationRecipeDefinition
    enabled: bool | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        definition = self.definition.to_dict()

        enabled = self.enabled

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "id": id,
                "definition": definition,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.correlation_recipe_definition import CorrelationRecipeDefinition

        d = dict(src_dict)
        id = d.pop("id")

        definition = CorrelationRecipeDefinition.from_dict(d.pop("definition"))

        enabled = d.pop("enabled", UNSET)

        correlation_recipe_create = cls(
            id=id,
            definition=definition,
            enabled=enabled,
        )

        return correlation_recipe_create
