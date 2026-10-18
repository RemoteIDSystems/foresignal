from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.context_summary_activity_item import ContextSummaryActivityItem
    from ..models.context_summary_insights_item import ContextSummaryInsightsItem
    from ..models.context_summary_layer_counts_item import ContextSummaryLayerCountsItem
    from ..models.context_summary_narrative_type_0 import ContextSummaryNarrativeType0
    from ..models.context_summary_request import ContextSummaryRequest
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="ContextSummary")


@_attrs_define
class ContextSummary:
    """Counts, activity, insights and related objects for a context (≤ 32 KB)

    Attributes:
        generated_at (datetime.datetime):
        layer_counts (list[ContextSummaryLayerCountsItem]):
        activity (list[ContextSummaryActivityItem]):
        insights (list[ContextSummaryInsightsItem]):
        related (list[ObjectEnvelope]):
        context (ContextSummaryRequest | Unset): A viewport, window and filters to summarise
        cache_age_seconds (int | Unset):
        narrative (ContextSummaryNarrativeType0 | None | Unset):
        truncated (bool | Unset):
    """

    generated_at: datetime.datetime
    layer_counts: list[ContextSummaryLayerCountsItem]
    activity: list[ContextSummaryActivityItem]
    insights: list[ContextSummaryInsightsItem]
    related: list[ObjectEnvelope]
    context: ContextSummaryRequest | Unset = UNSET
    cache_age_seconds: int | Unset = UNSET
    narrative: ContextSummaryNarrativeType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.context_summary_narrative_type_0 import (
            ContextSummaryNarrativeType0,
        )

        generated_at = self.generated_at.isoformat()

        layer_counts = []
        for layer_counts_item_data in self.layer_counts:
            layer_counts_item = layer_counts_item_data.to_dict()
            layer_counts.append(layer_counts_item)

        activity = []
        for activity_item_data in self.activity:
            activity_item = activity_item_data.to_dict()
            activity.append(activity_item)

        insights = []
        for insights_item_data in self.insights:
            insights_item = insights_item_data.to_dict()
            insights.append(insights_item)

        related = []
        for related_item_data in self.related:
            related_item = related_item_data.to_dict()
            related.append(related_item)

        context: dict[str, Any] | Unset = UNSET
        if not isinstance(self.context, Unset):
            context = self.context.to_dict()

        cache_age_seconds = self.cache_age_seconds

        narrative: dict[str, Any] | None | Unset
        if isinstance(self.narrative, Unset):
            narrative = UNSET
        elif isinstance(self.narrative, ContextSummaryNarrativeType0):
            narrative = self.narrative.to_dict()
        else:
            narrative = self.narrative

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "generatedAt": generated_at,
                "layerCounts": layer_counts,
                "activity": activity,
                "insights": insights,
                "related": related,
            }
        )
        if context is not UNSET:
            field_dict["context"] = context
        if cache_age_seconds is not UNSET:
            field_dict["cacheAgeSeconds"] = cache_age_seconds
        if narrative is not UNSET:
            field_dict["narrative"] = narrative
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.context_summary_activity_item import ContextSummaryActivityItem
        from ..models.context_summary_insights_item import ContextSummaryInsightsItem
        from ..models.context_summary_layer_counts_item import (
            ContextSummaryLayerCountsItem,
        )
        from ..models.context_summary_narrative_type_0 import (
            ContextSummaryNarrativeType0,
        )
        from ..models.context_summary_request import ContextSummaryRequest
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        layer_counts = []
        _layer_counts = d.pop("layerCounts")
        for layer_counts_item_data in _layer_counts:
            layer_counts_item = ContextSummaryLayerCountsItem.from_dict(
                layer_counts_item_data
            )

            layer_counts.append(layer_counts_item)

        activity = []
        _activity = d.pop("activity")
        for activity_item_data in _activity:
            activity_item = ContextSummaryActivityItem.from_dict(activity_item_data)

            activity.append(activity_item)

        insights = []
        _insights = d.pop("insights")
        for insights_item_data in _insights:
            insights_item = ContextSummaryInsightsItem.from_dict(insights_item_data)

            insights.append(insights_item)

        related = []
        _related = d.pop("related")
        for related_item_data in _related:
            related_item = ObjectEnvelope.from_dict(related_item_data)

            related.append(related_item)

        _context = d.pop("context", UNSET)
        context: ContextSummaryRequest | Unset
        if isinstance(_context, Unset):
            context = UNSET
        else:
            context = ContextSummaryRequest.from_dict(_context)

        cache_age_seconds = d.pop("cacheAgeSeconds", UNSET)

        def _parse_narrative(
            data: object,
        ) -> ContextSummaryNarrativeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                narrative_type_0 = ContextSummaryNarrativeType0.from_dict(data)

                return narrative_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ContextSummaryNarrativeType0 | None | Unset, data)

        narrative = _parse_narrative(d.pop("narrative", UNSET))

        truncated = d.pop("truncated", UNSET)

        context_summary = cls(
            generated_at=generated_at,
            layer_counts=layer_counts,
            activity=activity,
            insights=insights,
            related=related,
            context=context,
            cache_age_seconds=cache_age_seconds,
            narrative=narrative,
            truncated=truncated,
        )

        context_summary.additional_properties = d
        return context_summary

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
