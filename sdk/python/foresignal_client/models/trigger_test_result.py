from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.record import Record
    from ..models.trigger_test_result_warnings_item import TriggerTestResultWarningsItem
    from ..models.trigger_test_result_window_evaluations_item import (
        TriggerTestResultWindowEvaluationsItem,
    )


T = TypeVar("T", bound="TriggerTestResult")


@_attrs_define
class TriggerTestResult:
    """
    Attributes:
        rule_id (UUID):
        matches (list[Record]):
        evaluated_from (datetime.datetime | Unset):
        truncated (bool | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        match_count (int | Unset):
        simulated (bool | Unset): True when simulate=true (cooldown, cap and active windows applied)
        would_fire (int | Unset):
        would_suppress (int | Unset):
        truncated_by_retention (bool | Unset): fromSeq is below RetentionState.lowestRetainedSeq
        enrich (bool | Unset): True when the rule would enrich; no LLM is called by testTrigger
        warnings (list[TriggerTestResultWarningsItem] | Unset): Warnings, e.g. DOWNSAMPLED_RANGE: match-mode results
            over downsampled history under-count; edge reasons are preserved
        approximate (bool | Unset): True for windowed types (versions only)
        approximate_reason (None | str | Unset):  Example: versions only; entities observed without a new version inside
            the window are missed when window < source keyframe.
        window_evaluations (list[TriggerTestResultWindowEvaluationsItem] | Unset): Windowed types only
    """

    rule_id: UUID
    matches: list[Record]
    evaluated_from: datetime.datetime | Unset = UNSET
    truncated: bool | Unset = UNSET
    from_seq: int | Unset = UNSET
    to_seq: int | Unset = UNSET
    match_count: int | Unset = UNSET
    simulated: bool | Unset = UNSET
    would_fire: int | Unset = UNSET
    would_suppress: int | Unset = UNSET
    truncated_by_retention: bool | Unset = UNSET
    enrich: bool | Unset = UNSET
    warnings: list[TriggerTestResultWarningsItem] | Unset = UNSET
    approximate: bool | Unset = UNSET
    approximate_reason: None | str | Unset = UNSET
    window_evaluations: list[TriggerTestResultWindowEvaluationsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        matches = []
        for matches_item_data in self.matches:
            matches_item = matches_item_data.to_dict()
            matches.append(matches_item)

        evaluated_from: str | Unset = UNSET
        if not isinstance(self.evaluated_from, Unset):
            evaluated_from = self.evaluated_from.isoformat()

        truncated = self.truncated

        from_seq = self.from_seq

        to_seq = self.to_seq

        match_count = self.match_count

        simulated = self.simulated

        would_fire = self.would_fire

        would_suppress = self.would_suppress

        truncated_by_retention = self.truncated_by_retention

        enrich = self.enrich

        warnings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = []
            for warnings_item_data in self.warnings:
                warnings_item = warnings_item_data.to_dict()
                warnings.append(warnings_item)

        approximate = self.approximate

        approximate_reason: None | str | Unset
        if isinstance(self.approximate_reason, Unset):
            approximate_reason = UNSET
        else:
            approximate_reason = self.approximate_reason

        window_evaluations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.window_evaluations, Unset):
            window_evaluations = []
            for window_evaluations_item_data in self.window_evaluations:
                window_evaluations_item = window_evaluations_item_data.to_dict()
                window_evaluations.append(window_evaluations_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "matches": matches,
            }
        )
        if evaluated_from is not UNSET:
            field_dict["evaluatedFrom"] = evaluated_from
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if match_count is not UNSET:
            field_dict["matchCount"] = match_count
        if simulated is not UNSET:
            field_dict["simulated"] = simulated
        if would_fire is not UNSET:
            field_dict["wouldFire"] = would_fire
        if would_suppress is not UNSET:
            field_dict["wouldSuppress"] = would_suppress
        if truncated_by_retention is not UNSET:
            field_dict["truncatedByRetention"] = truncated_by_retention
        if enrich is not UNSET:
            field_dict["enrich"] = enrich
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if approximate is not UNSET:
            field_dict["approximate"] = approximate
        if approximate_reason is not UNSET:
            field_dict["approximateReason"] = approximate_reason
        if window_evaluations is not UNSET:
            field_dict["windowEvaluations"] = window_evaluations

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.record import Record
        from ..models.trigger_test_result_warnings_item import (
            TriggerTestResultWarningsItem,
        )
        from ..models.trigger_test_result_window_evaluations_item import (
            TriggerTestResultWindowEvaluationsItem,
        )

        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        matches = []
        _matches = d.pop("matches")
        for matches_item_data in _matches:
            matches_item = Record.from_dict(matches_item_data)

            matches.append(matches_item)

        _evaluated_from = d.pop("evaluatedFrom", UNSET)
        evaluated_from: datetime.datetime | Unset
        if isinstance(_evaluated_from, Unset):
            evaluated_from = UNSET
        else:
            evaluated_from = datetime.datetime.fromisoformat(_evaluated_from)

        truncated = d.pop("truncated", UNSET)

        from_seq = d.pop("fromSeq", UNSET)

        to_seq = d.pop("toSeq", UNSET)

        match_count = d.pop("matchCount", UNSET)

        simulated = d.pop("simulated", UNSET)

        would_fire = d.pop("wouldFire", UNSET)

        would_suppress = d.pop("wouldSuppress", UNSET)

        truncated_by_retention = d.pop("truncatedByRetention", UNSET)

        enrich = d.pop("enrich", UNSET)

        _warnings = d.pop("warnings", UNSET)
        warnings: list[TriggerTestResultWarningsItem] | Unset = UNSET
        if _warnings is not UNSET:
            warnings = []
            for warnings_item_data in _warnings:
                warnings_item = TriggerTestResultWarningsItem.from_dict(
                    warnings_item_data
                )

                warnings.append(warnings_item)

        approximate = d.pop("approximate", UNSET)

        def _parse_approximate_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        approximate_reason = _parse_approximate_reason(
            d.pop("approximateReason", UNSET)
        )

        _window_evaluations = d.pop("windowEvaluations", UNSET)
        window_evaluations: list[TriggerTestResultWindowEvaluationsItem] | Unset = UNSET
        if _window_evaluations is not UNSET:
            window_evaluations = []
            for window_evaluations_item_data in _window_evaluations:
                window_evaluations_item = (
                    TriggerTestResultWindowEvaluationsItem.from_dict(
                        window_evaluations_item_data
                    )
                )

                window_evaluations.append(window_evaluations_item)

        trigger_test_result = cls(
            rule_id=rule_id,
            matches=matches,
            evaluated_from=evaluated_from,
            truncated=truncated,
            from_seq=from_seq,
            to_seq=to_seq,
            match_count=match_count,
            simulated=simulated,
            would_fire=would_fire,
            would_suppress=would_suppress,
            truncated_by_retention=truncated_by_retention,
            enrich=enrich,
            warnings=warnings,
            approximate=approximate,
            approximate_reason=approximate_reason,
            window_evaluations=window_evaluations,
        )

        trigger_test_result.additional_properties = d
        return trigger_test_result

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
