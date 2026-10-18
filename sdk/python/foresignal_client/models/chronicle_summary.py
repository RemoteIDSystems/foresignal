from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_summary_gaps_item import ChronicleSummaryGapsItem
    from ..models.chronicle_summary_layers_item import ChronicleSummaryLayersItem
    from ..models.chronicle_summary_storage import ChronicleSummaryStorage
    from ..models.chronicle_summary_top_entities_item import (
        ChronicleSummaryTopEntitiesItem,
    )
    from ..models.object_envelope import ObjectEnvelope
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="ChronicleSummary")


@_attrs_define
class ChronicleSummary:
    """Summary of a chronicle's capture (reused by 132 reports)

    Attributes:
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        span (TimeRange):
        layers (list[ChronicleSummaryLayersItem]):
        storage (ChronicleSummaryStorage): Storage use
        gaps (list[ChronicleSummaryGapsItem]):
        top_entities (list[ChronicleSummaryTopEntitiesItem] | Unset):
        situations (list[ObjectEnvelope] | Unset):
        hypothesis_sets (list[ObjectEnvelope] | Unset):
        forecasts (list[ObjectEnvelope] | Unset):
    """

    chronicle_id: str
    span: TimeRange
    layers: list[ChronicleSummaryLayersItem]
    storage: ChronicleSummaryStorage
    gaps: list[ChronicleSummaryGapsItem]
    top_entities: list[ChronicleSummaryTopEntitiesItem] | Unset = UNSET
    situations: list[ObjectEnvelope] | Unset = UNSET
    hypothesis_sets: list[ObjectEnvelope] | Unset = UNSET
    forecasts: list[ObjectEnvelope] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_id = self.chronicle_id

        span = self.span.to_dict()

        layers = []
        for layers_item_data in self.layers:
            layers_item = layers_item_data.to_dict()
            layers.append(layers_item)

        storage = self.storage.to_dict()

        gaps = []
        for gaps_item_data in self.gaps:
            gaps_item = gaps_item_data.to_dict()
            gaps.append(gaps_item)

        top_entities: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.top_entities, Unset):
            top_entities = []
            for top_entities_item_data in self.top_entities:
                top_entities_item = top_entities_item_data.to_dict()
                top_entities.append(top_entities_item)

        situations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.situations, Unset):
            situations = []
            for situations_item_data in self.situations:
                situations_item = situations_item_data.to_dict()
                situations.append(situations_item)

        hypothesis_sets: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.hypothesis_sets, Unset):
            hypothesis_sets = []
            for hypothesis_sets_item_data in self.hypothesis_sets:
                hypothesis_sets_item = hypothesis_sets_item_data.to_dict()
                hypothesis_sets.append(hypothesis_sets_item)

        forecasts: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.forecasts, Unset):
            forecasts = []
            for forecasts_item_data in self.forecasts:
                forecasts_item = forecasts_item_data.to_dict()
                forecasts.append(forecasts_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chronicleId": chronicle_id,
                "span": span,
                "layers": layers,
                "storage": storage,
                "gaps": gaps,
            }
        )
        if top_entities is not UNSET:
            field_dict["topEntities"] = top_entities
        if situations is not UNSET:
            field_dict["situations"] = situations
        if hypothesis_sets is not UNSET:
            field_dict["hypothesisSets"] = hypothesis_sets
        if forecasts is not UNSET:
            field_dict["forecasts"] = forecasts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_summary_gaps_item import ChronicleSummaryGapsItem
        from ..models.chronicle_summary_layers_item import ChronicleSummaryLayersItem
        from ..models.chronicle_summary_storage import ChronicleSummaryStorage
        from ..models.chronicle_summary_top_entities_item import (
            ChronicleSummaryTopEntitiesItem,
        )
        from ..models.object_envelope import ObjectEnvelope
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        span = TimeRange.from_dict(d.pop("span"))

        layers = []
        _layers = d.pop("layers")
        for layers_item_data in _layers:
            layers_item = ChronicleSummaryLayersItem.from_dict(layers_item_data)

            layers.append(layers_item)

        storage = ChronicleSummaryStorage.from_dict(d.pop("storage"))

        gaps = []
        _gaps = d.pop("gaps")
        for gaps_item_data in _gaps:
            gaps_item = ChronicleSummaryGapsItem.from_dict(gaps_item_data)

            gaps.append(gaps_item)

        _top_entities = d.pop("topEntities", UNSET)
        top_entities: list[ChronicleSummaryTopEntitiesItem] | Unset = UNSET
        if _top_entities is not UNSET:
            top_entities = []
            for top_entities_item_data in _top_entities:
                top_entities_item = ChronicleSummaryTopEntitiesItem.from_dict(
                    top_entities_item_data
                )

                top_entities.append(top_entities_item)

        _situations = d.pop("situations", UNSET)
        situations: list[ObjectEnvelope] | Unset = UNSET
        if _situations is not UNSET:
            situations = []
            for situations_item_data in _situations:
                situations_item = ObjectEnvelope.from_dict(situations_item_data)

                situations.append(situations_item)

        _hypothesis_sets = d.pop("hypothesisSets", UNSET)
        hypothesis_sets: list[ObjectEnvelope] | Unset = UNSET
        if _hypothesis_sets is not UNSET:
            hypothesis_sets = []
            for hypothesis_sets_item_data in _hypothesis_sets:
                hypothesis_sets_item = ObjectEnvelope.from_dict(
                    hypothesis_sets_item_data
                )

                hypothesis_sets.append(hypothesis_sets_item)

        _forecasts = d.pop("forecasts", UNSET)
        forecasts: list[ObjectEnvelope] | Unset = UNSET
        if _forecasts is not UNSET:
            forecasts = []
            for forecasts_item_data in _forecasts:
                forecasts_item = ObjectEnvelope.from_dict(forecasts_item_data)

                forecasts.append(forecasts_item)

        chronicle_summary = cls(
            chronicle_id=chronicle_id,
            span=span,
            layers=layers,
            storage=storage,
            gaps=gaps,
            top_entities=top_entities,
            situations=situations,
            hypothesis_sets=hypothesis_sets,
            forecasts=forecasts,
        )

        chronicle_summary.additional_properties = d
        return chronicle_summary

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
