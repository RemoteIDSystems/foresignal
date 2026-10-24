from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.source_relevance import SourceRelevance
    from ..models.source_relevance_analysis_shared import SourceRelevanceAnalysisShared
    from ..models.source_relevance_analysis_template import (
        SourceRelevanceAnalysisTemplate,
    )


T = TypeVar("T", bound="SourceRelevanceAnalysis")


@_attrs_define
class SourceRelevanceAnalysis:
    """Advisory relevance of a rule's candidate sources (`analyzeTriggerSources`). Nothing is changed by it.

    Attributes:
        advisory (bool): Always true: the verdicts are suggestions; the caller keeps or omits each source
        summary (None | str): The model's one sentence on what the set as a whole watches; null when it gave none
        sources (list[SourceRelevance]): Every requested source exactly once, in request order
        shared (SourceRelevanceAnalysisShared): What two or more of the sources sent to the model declare
        model (None | str): The model that answered; null when none was asked
        template (SourceRelevanceAnalysisTemplate): The prompt template asked
    """

    advisory: bool
    summary: None | str
    sources: list[SourceRelevance]
    shared: SourceRelevanceAnalysisShared
    model: None | str
    template: SourceRelevanceAnalysisTemplate
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        advisory = self.advisory

        summary: None | str
        summary = self.summary

        sources = []
        for sources_item_data in self.sources:
            sources_item = sources_item_data.to_dict()
            sources.append(sources_item)

        shared = self.shared.to_dict()

        model: None | str
        model = self.model

        template = self.template.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "advisory": advisory,
                "summary": summary,
                "sources": sources,
                "shared": shared,
                "model": model,
                "template": template,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.source_relevance import SourceRelevance
        from ..models.source_relevance_analysis_shared import (
            SourceRelevanceAnalysisShared,
        )
        from ..models.source_relevance_analysis_template import (
            SourceRelevanceAnalysisTemplate,
        )

        d = dict(src_dict)
        advisory = d.pop("advisory")

        def _parse_summary(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        summary = _parse_summary(d.pop("summary"))

        sources = []
        _sources = d.pop("sources")
        for sources_item_data in _sources:
            sources_item = SourceRelevance.from_dict(sources_item_data)

            sources.append(sources_item)

        shared = SourceRelevanceAnalysisShared.from_dict(d.pop("shared"))

        def _parse_model(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        model = _parse_model(d.pop("model"))

        template = SourceRelevanceAnalysisTemplate.from_dict(d.pop("template"))

        source_relevance_analysis = cls(
            advisory=advisory,
            summary=summary,
            sources=sources,
            shared=shared,
            model=model,
            template=template,
        )

        source_relevance_analysis.additional_properties = d
        return source_relevance_analysis

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
