from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intelligence_event_speculation_narrative_origin_type_1 import (
    IntelligenceEventSpeculationNarrativeOriginType1,
)
from ..models.intelligence_event_speculation_narrative_origin_type_2_type_1 import (
    IntelligenceEventSpeculationNarrativeOriginType2Type1,
)
from ..models.intelligence_event_speculation_narrative_origin_type_3_type_1 import (
    IntelligenceEventSpeculationNarrativeOriginType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_event_alternative import IntelEventAlternative


T = TypeVar("T", bound="IntelligenceEventSpeculation")


@_attrs_define
class IntelligenceEventSpeculation:
    """Alternatives and any LLM narrative, always labelled

    Attributes:
        label (Literal['speculation']):
        alternatives (list[IntelEventAlternative]):
        narrative (None | str | Unset): Optional LLM text (102 annotate_event appends here)
        narrative_origin (IntelligenceEventSpeculationNarrativeOriginType1 |
            IntelligenceEventSpeculationNarrativeOriginType2Type1 | IntelligenceEventSpeculationNarrativeOriginType3Type1 |
            None | Unset):
    """

    label: Literal["speculation"]
    alternatives: list[IntelEventAlternative]
    narrative: None | str | Unset = UNSET
    narrative_origin: (
        IntelligenceEventSpeculationNarrativeOriginType1
        | IntelligenceEventSpeculationNarrativeOriginType2Type1
        | IntelligenceEventSpeculationNarrativeOriginType3Type1
        | None
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        label = self.label

        alternatives = []
        for alternatives_item_data in self.alternatives:
            alternatives_item = alternatives_item_data.to_dict()
            alternatives.append(alternatives_item)

        narrative: None | str | Unset
        if isinstance(self.narrative, Unset):
            narrative = UNSET
        else:
            narrative = self.narrative

        narrative_origin: None | str | Unset
        if isinstance(self.narrative_origin, Unset):
            narrative_origin = UNSET
        elif (
            isinstance(
                self.narrative_origin, IntelligenceEventSpeculationNarrativeOriginType1
            )
            or isinstance(
                self.narrative_origin,
                IntelligenceEventSpeculationNarrativeOriginType2Type1,
            )
            or isinstance(
                self.narrative_origin,
                IntelligenceEventSpeculationNarrativeOriginType3Type1,
            )
        ):
            narrative_origin = self.narrative_origin.value
        else:
            narrative_origin = self.narrative_origin

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "label": label,
                "alternatives": alternatives,
            }
        )
        if narrative is not UNSET:
            field_dict["narrative"] = narrative
        if narrative_origin is not UNSET:
            field_dict["narrativeOrigin"] = narrative_origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_event_alternative import IntelEventAlternative

        d = dict(src_dict)
        label = cast(Literal["speculation"], d.pop("label"))
        if label != "speculation":
            raise ValueError(f"label must match const 'speculation', got '{label}'")

        alternatives = []
        _alternatives = d.pop("alternatives")
        for alternatives_item_data in _alternatives:
            alternatives_item = IntelEventAlternative.from_dict(alternatives_item_data)

            alternatives.append(alternatives_item)

        def _parse_narrative(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        narrative = _parse_narrative(d.pop("narrative", UNSET))

        def _parse_narrative_origin(
            data: object,
        ) -> (
            IntelligenceEventSpeculationNarrativeOriginType1
            | IntelligenceEventSpeculationNarrativeOriginType2Type1
            | IntelligenceEventSpeculationNarrativeOriginType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                narrative_origin_type_1 = (
                    IntelligenceEventSpeculationNarrativeOriginType1(data)
                )

                return narrative_origin_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                narrative_origin_type_2_type_1 = (
                    IntelligenceEventSpeculationNarrativeOriginType2Type1(data)
                )

                return narrative_origin_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                narrative_origin_type_3_type_1 = (
                    IntelligenceEventSpeculationNarrativeOriginType3Type1(data)
                )

                return narrative_origin_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                IntelligenceEventSpeculationNarrativeOriginType1
                | IntelligenceEventSpeculationNarrativeOriginType2Type1
                | IntelligenceEventSpeculationNarrativeOriginType3Type1
                | None
                | Unset,
                data,
            )

        narrative_origin = _parse_narrative_origin(d.pop("narrativeOrigin", UNSET))

        intelligence_event_speculation = cls(
            label=label,
            alternatives=alternatives,
            narrative=narrative,
            narrative_origin=narrative_origin,
        )

        intelligence_event_speculation.additional_properties = d
        return intelligence_event_speculation

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
