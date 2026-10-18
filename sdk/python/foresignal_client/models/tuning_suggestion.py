from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tuning_suggestion_kind import TuningSuggestionKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.tuning_suggestion_expected_effect_type_0 import (
        TuningSuggestionExpectedEffectType0,
    )
    from ..models.tuning_suggestion_patch import TuningSuggestionPatch


T = TypeVar("T", bound="TuningSuggestion")


@_attrs_define
class TuningSuggestion:
    """A deterministic tuning suggestion with a ready rule patch; never applied automatically

    Attributes:
        id (str):
        kind (TuningSuggestionKind):
        rationale (str):
        patch (TuningSuggestionPatch): Partial TriggerRuleInput ready to apply as an ordinary rule update
        expected_effect (None | TuningSuggestionExpectedEffectType0 | Unset): From a backtest of the patched variant
            when simulated
        caveats (list[Caveat] | Unset):
    """

    id: str
    kind: TuningSuggestionKind
    rationale: str
    patch: TuningSuggestionPatch
    expected_effect: None | TuningSuggestionExpectedEffectType0 | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.tuning_suggestion_expected_effect_type_0 import (
            TuningSuggestionExpectedEffectType0,
        )

        id = self.id

        kind = self.kind.value

        rationale = self.rationale

        patch = self.patch.to_dict()

        expected_effect: dict[str, Any] | None | Unset
        if isinstance(self.expected_effect, Unset):
            expected_effect = UNSET
        elif isinstance(self.expected_effect, TuningSuggestionExpectedEffectType0):
            expected_effect = self.expected_effect.to_dict()
        else:
            expected_effect = self.expected_effect

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "rationale": rationale,
                "patch": patch,
            }
        )
        if expected_effect is not UNSET:
            field_dict["expectedEffect"] = expected_effect
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.tuning_suggestion_expected_effect_type_0 import (
            TuningSuggestionExpectedEffectType0,
        )
        from ..models.tuning_suggestion_patch import TuningSuggestionPatch

        d = dict(src_dict)
        id = d.pop("id")

        kind = TuningSuggestionKind(d.pop("kind"))

        rationale = d.pop("rationale")

        patch = TuningSuggestionPatch.from_dict(d.pop("patch"))

        def _parse_expected_effect(
            data: object,
        ) -> None | TuningSuggestionExpectedEffectType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                expected_effect_type_0 = TuningSuggestionExpectedEffectType0.from_dict(
                    data
                )

                return expected_effect_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TuningSuggestionExpectedEffectType0 | Unset, data)

        expected_effect = _parse_expected_effect(d.pop("expectedEffect", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        tuning_suggestion = cls(
            id=id,
            kind=kind,
            rationale=rationale,
            patch=patch,
            expected_effect=expected_effect,
            caveats=caveats,
        )

        tuning_suggestion.additional_properties = d
        return tuning_suggestion

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
