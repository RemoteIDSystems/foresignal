from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.index_weights_proposed_event_proposed_weights import (
        IndexWeightsProposedEventProposedWeights,
    )
    from ..models.index_weights_proposed_event_skill_type_0 import (
        IndexWeightsProposedEventSkillType0,
    )


T = TypeVar("T", bound="IndexWeightsProposedEvent")


@_attrs_define
class IndexWeightsProposedEvent:
    """SSE payload for `index.weights_proposed` (topic `analytics`)

    Attributes:
        index_id (str):
        fit_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        weights_version (int):
        proposed_weights (IndexWeightsProposedEventProposedWeights):
        skill (IndexWeightsProposedEventSkillType0 | None | Unset):
        auto_applied (bool | Unset):
    """

    index_id: str
    fit_id: str
    weights_version: int
    proposed_weights: IndexWeightsProposedEventProposedWeights
    skill: IndexWeightsProposedEventSkillType0 | None | Unset = UNSET
    auto_applied: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.index_weights_proposed_event_skill_type_0 import (
            IndexWeightsProposedEventSkillType0,
        )

        index_id = self.index_id

        fit_id = self.fit_id

        weights_version = self.weights_version

        proposed_weights = self.proposed_weights.to_dict()

        skill: dict[str, Any] | None | Unset
        if isinstance(self.skill, Unset):
            skill = UNSET
        elif isinstance(self.skill, IndexWeightsProposedEventSkillType0):
            skill = self.skill.to_dict()
        else:
            skill = self.skill

        auto_applied = self.auto_applied

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "indexId": index_id,
                "fitId": fit_id,
                "weightsVersion": weights_version,
                "proposedWeights": proposed_weights,
            }
        )
        if skill is not UNSET:
            field_dict["skill"] = skill
        if auto_applied is not UNSET:
            field_dict["autoApplied"] = auto_applied

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.index_weights_proposed_event_proposed_weights import (
            IndexWeightsProposedEventProposedWeights,
        )
        from ..models.index_weights_proposed_event_skill_type_0 import (
            IndexWeightsProposedEventSkillType0,
        )

        d = dict(src_dict)
        index_id = d.pop("indexId")

        fit_id = d.pop("fitId")

        weights_version = d.pop("weightsVersion")

        proposed_weights = IndexWeightsProposedEventProposedWeights.from_dict(
            d.pop("proposedWeights")
        )

        def _parse_skill(
            data: object,
        ) -> IndexWeightsProposedEventSkillType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                skill_type_0 = IndexWeightsProposedEventSkillType0.from_dict(data)

                return skill_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndexWeightsProposedEventSkillType0 | None | Unset, data)

        skill = _parse_skill(d.pop("skill", UNSET))

        auto_applied = d.pop("autoApplied", UNSET)

        index_weights_proposed_event = cls(
            index_id=index_id,
            fit_id=fit_id,
            weights_version=weights_version,
            proposed_weights=proposed_weights,
            skill=skill,
            auto_applied=auto_applied,
        )

        index_weights_proposed_event.additional_properties = d
        return index_weights_proposed_event

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
