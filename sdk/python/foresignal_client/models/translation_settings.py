from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_spend_window import LlmSpendWindow
    from ..models.translation_settings_budget import TranslationSettingsBudget


T = TypeVar("T", bound="TranslationSettings")


@_attrs_define
class TranslationSettings:
    """News translation at ingest (NP-002): foreign-language news is translated into the target languages and stored beside
    the untouched original, with provenance.

        Attributes:
            enabled (bool): Master switch: off, no article or story is translated and reads carry the originals only
                Default: False.
            target_languages (list[str]): Languages foreign news is translated into; an article already in a target language
                is not translated into it
            fields (list[str]): Which fields are translated: `title` (the article or story title) and `excerpt` (the
                article's feed summary). Expected to grow (`x-extensible-enum`).
            engine (str): The translation engine: `llm` uses the 026 LLM layer under the `translation` feature, budget-
                governed. Expected to grow (`x-extensible-enum`). Default: 'llm'.
            budget (TranslationSettingsBudget | Unset): A spend cap for translation alone, inside the provider's own caps
                (162)
            min_source_language_confidence (float | Unset): The least confidence in the detected source language at which an
                article is translated; below it the article is left as is Default: 0.8.
            spend (LlmSpendWindow | None | Unset): Translation spend in the current `budget.window`, or null when no
                translation has been priced yet
    """

    target_languages: list[str]
    fields: list[str]
    enabled: bool = False
    engine: str = "llm"
    budget: TranslationSettingsBudget | Unset = UNSET
    min_source_language_confidence: float | Unset = 0.8
    spend: LlmSpendWindow | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.llm_spend_window import LlmSpendWindow

        enabled = self.enabled

        target_languages = self.target_languages

        fields = self.fields

        engine = self.engine

        budget: dict[str, Any] | Unset = UNSET
        if not isinstance(self.budget, Unset):
            budget = self.budget.to_dict()

        min_source_language_confidence = self.min_source_language_confidence

        spend: dict[str, Any] | None | Unset
        if isinstance(self.spend, Unset):
            spend = UNSET
        elif isinstance(self.spend, LlmSpendWindow):
            spend = self.spend.to_dict()
        else:
            spend = self.spend

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
                "targetLanguages": target_languages,
                "fields": fields,
                "engine": engine,
            }
        )
        if budget is not UNSET:
            field_dict["budget"] = budget
        if min_source_language_confidence is not UNSET:
            field_dict["minSourceLanguageConfidence"] = min_source_language_confidence
        if spend is not UNSET:
            field_dict["spend"] = spend

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_spend_window import LlmSpendWindow
        from ..models.translation_settings_budget import TranslationSettingsBudget

        d = dict(src_dict)
        enabled = d.pop("enabled")

        target_languages = cast(list[str], d.pop("targetLanguages"))

        fields = cast(list[str], d.pop("fields"))

        engine = d.pop("engine")

        _budget = d.pop("budget", UNSET)
        budget: TranslationSettingsBudget | Unset
        if isinstance(_budget, Unset):
            budget = UNSET
        else:
            budget = TranslationSettingsBudget.from_dict(_budget)

        min_source_language_confidence = d.pop("minSourceLanguageConfidence", UNSET)

        def _parse_spend(data: object) -> LlmSpendWindow | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                spend_type_0 = LlmSpendWindow.from_dict(data)

                return spend_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LlmSpendWindow | None | Unset, data)

        spend = _parse_spend(d.pop("spend", UNSET))

        translation_settings = cls(
            enabled=enabled,
            target_languages=target_languages,
            fields=fields,
            engine=engine,
            budget=budget,
            min_source_language_confidence=min_source_language_confidence,
            spend=spend,
        )

        translation_settings.additional_properties = d
        return translation_settings

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
