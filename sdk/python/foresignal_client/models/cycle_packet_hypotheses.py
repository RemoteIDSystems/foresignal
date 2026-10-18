from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_packet_item import CyclePacketItem


T = TypeVar("T", bound="CyclePacketHypotheses")


@_attrs_define
class CyclePacketHypotheses:
    """Hypothesis changes

    Attributes:
        revisions (list[CyclePacketItem] | Unset):
        band_changes (list[CyclePacketItem] | Unset):
        indicators_observed (list[CyclePacketItem] | Unset):
        indicators_absent (list[CyclePacketItem] | Unset):
        indicators_unknown_coverage (list[CyclePacketItem] | Unset):
        assumptions_broken (list[CyclePacketItem] | Unset):
        assumptions_questioned (list[CyclePacketItem] | Unset):
        due_for_review (list[CyclePacketItem] | Unset):
    """

    revisions: list[CyclePacketItem] | Unset = UNSET
    band_changes: list[CyclePacketItem] | Unset = UNSET
    indicators_observed: list[CyclePacketItem] | Unset = UNSET
    indicators_absent: list[CyclePacketItem] | Unset = UNSET
    indicators_unknown_coverage: list[CyclePacketItem] | Unset = UNSET
    assumptions_broken: list[CyclePacketItem] | Unset = UNSET
    assumptions_questioned: list[CyclePacketItem] | Unset = UNSET
    due_for_review: list[CyclePacketItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        revisions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.revisions, Unset):
            revisions = []
            for revisions_item_data in self.revisions:
                revisions_item = revisions_item_data.to_dict()
                revisions.append(revisions_item)

        band_changes: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.band_changes, Unset):
            band_changes = []
            for band_changes_item_data in self.band_changes:
                band_changes_item = band_changes_item_data.to_dict()
                band_changes.append(band_changes_item)

        indicators_observed: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.indicators_observed, Unset):
            indicators_observed = []
            for indicators_observed_item_data in self.indicators_observed:
                indicators_observed_item = indicators_observed_item_data.to_dict()
                indicators_observed.append(indicators_observed_item)

        indicators_absent: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.indicators_absent, Unset):
            indicators_absent = []
            for indicators_absent_item_data in self.indicators_absent:
                indicators_absent_item = indicators_absent_item_data.to_dict()
                indicators_absent.append(indicators_absent_item)

        indicators_unknown_coverage: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.indicators_unknown_coverage, Unset):
            indicators_unknown_coverage = []
            for (
                indicators_unknown_coverage_item_data
            ) in self.indicators_unknown_coverage:
                indicators_unknown_coverage_item = (
                    indicators_unknown_coverage_item_data.to_dict()
                )
                indicators_unknown_coverage.append(indicators_unknown_coverage_item)

        assumptions_broken: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.assumptions_broken, Unset):
            assumptions_broken = []
            for assumptions_broken_item_data in self.assumptions_broken:
                assumptions_broken_item = assumptions_broken_item_data.to_dict()
                assumptions_broken.append(assumptions_broken_item)

        assumptions_questioned: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.assumptions_questioned, Unset):
            assumptions_questioned = []
            for assumptions_questioned_item_data in self.assumptions_questioned:
                assumptions_questioned_item = assumptions_questioned_item_data.to_dict()
                assumptions_questioned.append(assumptions_questioned_item)

        due_for_review: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.due_for_review, Unset):
            due_for_review = []
            for due_for_review_item_data in self.due_for_review:
                due_for_review_item = due_for_review_item_data.to_dict()
                due_for_review.append(due_for_review_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if revisions is not UNSET:
            field_dict["revisions"] = revisions
        if band_changes is not UNSET:
            field_dict["bandChanges"] = band_changes
        if indicators_observed is not UNSET:
            field_dict["indicatorsObserved"] = indicators_observed
        if indicators_absent is not UNSET:
            field_dict["indicatorsAbsent"] = indicators_absent
        if indicators_unknown_coverage is not UNSET:
            field_dict["indicatorsUnknownCoverage"] = indicators_unknown_coverage
        if assumptions_broken is not UNSET:
            field_dict["assumptionsBroken"] = assumptions_broken
        if assumptions_questioned is not UNSET:
            field_dict["assumptionsQuestioned"] = assumptions_questioned
        if due_for_review is not UNSET:
            field_dict["dueForReview"] = due_for_review

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_packet_item import CyclePacketItem

        d = dict(src_dict)
        _revisions = d.pop("revisions", UNSET)
        revisions: list[CyclePacketItem] | Unset = UNSET
        if _revisions is not UNSET:
            revisions = []
            for revisions_item_data in _revisions:
                revisions_item = CyclePacketItem.from_dict(revisions_item_data)

                revisions.append(revisions_item)

        _band_changes = d.pop("bandChanges", UNSET)
        band_changes: list[CyclePacketItem] | Unset = UNSET
        if _band_changes is not UNSET:
            band_changes = []
            for band_changes_item_data in _band_changes:
                band_changes_item = CyclePacketItem.from_dict(band_changes_item_data)

                band_changes.append(band_changes_item)

        _indicators_observed = d.pop("indicatorsObserved", UNSET)
        indicators_observed: list[CyclePacketItem] | Unset = UNSET
        if _indicators_observed is not UNSET:
            indicators_observed = []
            for indicators_observed_item_data in _indicators_observed:
                indicators_observed_item = CyclePacketItem.from_dict(
                    indicators_observed_item_data
                )

                indicators_observed.append(indicators_observed_item)

        _indicators_absent = d.pop("indicatorsAbsent", UNSET)
        indicators_absent: list[CyclePacketItem] | Unset = UNSET
        if _indicators_absent is not UNSET:
            indicators_absent = []
            for indicators_absent_item_data in _indicators_absent:
                indicators_absent_item = CyclePacketItem.from_dict(
                    indicators_absent_item_data
                )

                indicators_absent.append(indicators_absent_item)

        _indicators_unknown_coverage = d.pop("indicatorsUnknownCoverage", UNSET)
        indicators_unknown_coverage: list[CyclePacketItem] | Unset = UNSET
        if _indicators_unknown_coverage is not UNSET:
            indicators_unknown_coverage = []
            for indicators_unknown_coverage_item_data in _indicators_unknown_coverage:
                indicators_unknown_coverage_item = CyclePacketItem.from_dict(
                    indicators_unknown_coverage_item_data
                )

                indicators_unknown_coverage.append(indicators_unknown_coverage_item)

        _assumptions_broken = d.pop("assumptionsBroken", UNSET)
        assumptions_broken: list[CyclePacketItem] | Unset = UNSET
        if _assumptions_broken is not UNSET:
            assumptions_broken = []
            for assumptions_broken_item_data in _assumptions_broken:
                assumptions_broken_item = CyclePacketItem.from_dict(
                    assumptions_broken_item_data
                )

                assumptions_broken.append(assumptions_broken_item)

        _assumptions_questioned = d.pop("assumptionsQuestioned", UNSET)
        assumptions_questioned: list[CyclePacketItem] | Unset = UNSET
        if _assumptions_questioned is not UNSET:
            assumptions_questioned = []
            for assumptions_questioned_item_data in _assumptions_questioned:
                assumptions_questioned_item = CyclePacketItem.from_dict(
                    assumptions_questioned_item_data
                )

                assumptions_questioned.append(assumptions_questioned_item)

        _due_for_review = d.pop("dueForReview", UNSET)
        due_for_review: list[CyclePacketItem] | Unset = UNSET
        if _due_for_review is not UNSET:
            due_for_review = []
            for due_for_review_item_data in _due_for_review:
                due_for_review_item = CyclePacketItem.from_dict(
                    due_for_review_item_data
                )

                due_for_review.append(due_for_review_item)

        cycle_packet_hypotheses = cls(
            revisions=revisions,
            band_changes=band_changes,
            indicators_observed=indicators_observed,
            indicators_absent=indicators_absent,
            indicators_unknown_coverage=indicators_unknown_coverage,
            assumptions_broken=assumptions_broken,
            assumptions_questioned=assumptions_questioned,
            due_for_review=due_for_review,
        )

        cycle_packet_hypotheses.additional_properties = d
        return cycle_packet_hypotheses

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
