from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.correlation_recipe_definition_type import CorrelationRecipeDefinitionType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.correlation_recipe_definition_activate_at import (
        CorrelationRecipeDefinitionActivateAt,
    )
    from ..models.correlation_recipe_definition_contradictions_item import (
        CorrelationRecipeDefinitionContradictionsItem,
    )
    from ..models.correlation_recipe_definition_dedup import (
        CorrelationRecipeDefinitionDedup,
    )
    from ..models.correlation_recipe_definition_joins_item import (
        CorrelationRecipeDefinitionJoinsItem,
    )
    from ..models.correlation_recipe_definition_seed import (
        CorrelationRecipeDefinitionSeed,
    )
    from ..models.correlation_recipe_definition_severity_map import (
        CorrelationRecipeDefinitionSeverityMap,
    )


T = TypeVar("T", bound="CorrelationRecipeDefinition")


@_attrs_define
class CorrelationRecipeDefinition:
    """Recipe definition: seed, joins, thresholds, contradictions, dedup and lifecycle. Conditions use the 017 condition
    grammar.

        Attributes:
            type_ (CorrelationRecipeDefinitionType):
            seed (CorrelationRecipeDefinitionSeed): Seed records
            joins (list[CorrelationRecipeDefinitionJoinsItem] | Unset):
            min_members (int | Unset):
            min_signal_families (int | Unset):
            activate_at (CorrelationRecipeDefinitionActivateAt | Unset): Threshold for emerging → active
            contradictions (list[CorrelationRecipeDefinitionContradictionsItem] | Unset):
            severity_map (CorrelationRecipeDefinitionSeverityMap | Unset): Rule → severity mapping
            dedup (CorrelationRecipeDefinitionDedup | Unset): Same-event dedup
            quiet_period (str | Unset): Resolve after this long without new supporting evidence. ISO-8601 duration or
                shorthand (30m, 6h, 7d).
            reopen_window (str | Unset): Reopen when evidence arrives within this window after resolution. ISO-8601 duration
                or shorthand (30m, 6h, 7d).
    """

    type_: CorrelationRecipeDefinitionType
    seed: CorrelationRecipeDefinitionSeed
    joins: list[CorrelationRecipeDefinitionJoinsItem] | Unset = UNSET
    min_members: int | Unset = UNSET
    min_signal_families: int | Unset = UNSET
    activate_at: CorrelationRecipeDefinitionActivateAt | Unset = UNSET
    contradictions: list[CorrelationRecipeDefinitionContradictionsItem] | Unset = UNSET
    severity_map: CorrelationRecipeDefinitionSeverityMap | Unset = UNSET
    dedup: CorrelationRecipeDefinitionDedup | Unset = UNSET
    quiet_period: str | Unset = UNSET
    reopen_window: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        seed = self.seed.to_dict()

        joins: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.joins, Unset):
            joins = []
            for joins_item_data in self.joins:
                joins_item = joins_item_data.to_dict()
                joins.append(joins_item)

        min_members = self.min_members

        min_signal_families = self.min_signal_families

        activate_at: dict[str, Any] | Unset = UNSET
        if not isinstance(self.activate_at, Unset):
            activate_at = self.activate_at.to_dict()

        contradictions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.contradictions, Unset):
            contradictions = []
            for contradictions_item_data in self.contradictions:
                contradictions_item = contradictions_item_data.to_dict()
                contradictions.append(contradictions_item)

        severity_map: dict[str, Any] | Unset = UNSET
        if not isinstance(self.severity_map, Unset):
            severity_map = self.severity_map.to_dict()

        dedup: dict[str, Any] | Unset = UNSET
        if not isinstance(self.dedup, Unset):
            dedup = self.dedup.to_dict()

        quiet_period = self.quiet_period

        reopen_window = self.reopen_window

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "seed": seed,
            }
        )
        if joins is not UNSET:
            field_dict["joins"] = joins
        if min_members is not UNSET:
            field_dict["minMembers"] = min_members
        if min_signal_families is not UNSET:
            field_dict["minSignalFamilies"] = min_signal_families
        if activate_at is not UNSET:
            field_dict["activateAt"] = activate_at
        if contradictions is not UNSET:
            field_dict["contradictions"] = contradictions
        if severity_map is not UNSET:
            field_dict["severityMap"] = severity_map
        if dedup is not UNSET:
            field_dict["dedup"] = dedup
        if quiet_period is not UNSET:
            field_dict["quietPeriod"] = quiet_period
        if reopen_window is not UNSET:
            field_dict["reopenWindow"] = reopen_window

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.correlation_recipe_definition_activate_at import (
            CorrelationRecipeDefinitionActivateAt,
        )
        from ..models.correlation_recipe_definition_contradictions_item import (
            CorrelationRecipeDefinitionContradictionsItem,
        )
        from ..models.correlation_recipe_definition_dedup import (
            CorrelationRecipeDefinitionDedup,
        )
        from ..models.correlation_recipe_definition_joins_item import (
            CorrelationRecipeDefinitionJoinsItem,
        )
        from ..models.correlation_recipe_definition_seed import (
            CorrelationRecipeDefinitionSeed,
        )
        from ..models.correlation_recipe_definition_severity_map import (
            CorrelationRecipeDefinitionSeverityMap,
        )

        d = dict(src_dict)
        type_ = CorrelationRecipeDefinitionType(d.pop("type"))

        seed = CorrelationRecipeDefinitionSeed.from_dict(d.pop("seed"))

        _joins = d.pop("joins", UNSET)
        joins: list[CorrelationRecipeDefinitionJoinsItem] | Unset = UNSET
        if _joins is not UNSET:
            joins = []
            for joins_item_data in _joins:
                joins_item = CorrelationRecipeDefinitionJoinsItem.from_dict(
                    joins_item_data
                )

                joins.append(joins_item)

        min_members = d.pop("minMembers", UNSET)

        min_signal_families = d.pop("minSignalFamilies", UNSET)

        _activate_at = d.pop("activateAt", UNSET)
        activate_at: CorrelationRecipeDefinitionActivateAt | Unset
        if isinstance(_activate_at, Unset):
            activate_at = UNSET
        else:
            activate_at = CorrelationRecipeDefinitionActivateAt.from_dict(_activate_at)

        _contradictions = d.pop("contradictions", UNSET)
        contradictions: list[CorrelationRecipeDefinitionContradictionsItem] | Unset = (
            UNSET
        )
        if _contradictions is not UNSET:
            contradictions = []
            for contradictions_item_data in _contradictions:
                contradictions_item = (
                    CorrelationRecipeDefinitionContradictionsItem.from_dict(
                        contradictions_item_data
                    )
                )

                contradictions.append(contradictions_item)

        _severity_map = d.pop("severityMap", UNSET)
        severity_map: CorrelationRecipeDefinitionSeverityMap | Unset
        if isinstance(_severity_map, Unset):
            severity_map = UNSET
        else:
            severity_map = CorrelationRecipeDefinitionSeverityMap.from_dict(
                _severity_map
            )

        _dedup = d.pop("dedup", UNSET)
        dedup: CorrelationRecipeDefinitionDedup | Unset
        if isinstance(_dedup, Unset):
            dedup = UNSET
        else:
            dedup = CorrelationRecipeDefinitionDedup.from_dict(_dedup)

        quiet_period = d.pop("quietPeriod", UNSET)

        reopen_window = d.pop("reopenWindow", UNSET)

        correlation_recipe_definition = cls(
            type_=type_,
            seed=seed,
            joins=joins,
            min_members=min_members,
            min_signal_families=min_signal_families,
            activate_at=activate_at,
            contradictions=contradictions,
            severity_map=severity_map,
            dedup=dedup,
            quiet_period=quiet_period,
            reopen_window=reopen_window,
        )

        correlation_recipe_definition.additional_properties = d
        return correlation_recipe_definition

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
