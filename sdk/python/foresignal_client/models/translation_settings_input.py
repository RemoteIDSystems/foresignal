from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.translation_settings_input_budget import (
        TranslationSettingsInputBudget,
    )


T = TypeVar("T", bound="TranslationSettingsInput")


@_attrs_define
class TranslationSettingsInput:
    """News translation settings (settings key `translation`, NP-002). Replaces the settings: an omitted member takes its
    default; the read-only `spend` of a GET may be sent back and is ignored.

        Attributes:
            enabled (bool): Master switch: off, no article or story is translated and reads carry the originals only
                Default: False.
            target_languages (list[str] | Unset): Languages foreign news is translated into; an article already in a target
                language is not translated into it
            fields (list[str] | Unset): Which fields are translated: `title` (the article or story title) and `excerpt` (the
                article's feed summary). Expected to grow (`x-extensible-enum`).
            engine (str | Unset): The translation engine: `llm` uses the 026 LLM layer under the `translation` feature,
                budget-governed. Expected to grow (`x-extensible-enum`). Default: 'llm'.
            budget (TranslationSettingsInputBudget | Unset): A spend cap for translation alone, inside the provider's own
                caps (162)
            min_source_language_confidence (float | Unset): The least confidence in the detected source language at which an
                article is translated; below it the article is left as is Default: 0.8.
    """

    enabled: bool = False
    target_languages: list[str] | Unset = UNSET
    fields: list[str] | Unset = UNSET
    engine: str | Unset = "llm"
    budget: TranslationSettingsInputBudget | Unset = UNSET
    min_source_language_confidence: float | Unset = 0.8
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        target_languages: list[str] | Unset = UNSET
        if not isinstance(self.target_languages, Unset):
            target_languages = self.target_languages

        fields: list[str] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = self.fields

        engine = self.engine

        budget: dict[str, Any] | Unset = UNSET
        if not isinstance(self.budget, Unset):
            budget = self.budget.to_dict()

        min_source_language_confidence = self.min_source_language_confidence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
            }
        )
        if target_languages is not UNSET:
            field_dict["targetLanguages"] = target_languages
        if fields is not UNSET:
            field_dict["fields"] = fields
        if engine is not UNSET:
            field_dict["engine"] = engine
        if budget is not UNSET:
            field_dict["budget"] = budget
        if min_source_language_confidence is not UNSET:
            field_dict["minSourceLanguageConfidence"] = min_source_language_confidence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.translation_settings_input_budget import (
            TranslationSettingsInputBudget,
        )

        d = dict(src_dict)
        enabled = d.pop("enabled")

        target_languages = cast(list[str], d.pop("targetLanguages", UNSET))

        fields = cast(list[str], d.pop("fields", UNSET))

        engine = d.pop("engine", UNSET)

        _budget = d.pop("budget", UNSET)
        budget: TranslationSettingsInputBudget | Unset
        if isinstance(_budget, Unset):
            budget = UNSET
        else:
            budget = TranslationSettingsInputBudget.from_dict(_budget)

        min_source_language_confidence = d.pop("minSourceLanguageConfidence", UNSET)

        translation_settings_input = cls(
            enabled=enabled,
            target_languages=target_languages,
            fields=fields,
            engine=engine,
            budget=budget,
            min_source_language_confidence=min_source_language_confidence,
        )

        translation_settings_input.additional_properties = d
        return translation_settings_input

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
