from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_set_event_cause_type_0 import HypothesisSetEventCauseType0
from ..models.hypothesis_set_event_status import HypothesisSetEventStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_probability_state import HypothesisProbabilityState
    from ..models.hypothesis_set_event_band_changes_item import (
        HypothesisSetEventBandChangesItem,
    )


T = TypeVar("T", bound="HypothesisSetEvent")


@_attrs_define
class HypothesisSetEvent:
    """SSE payload for `set.created|revised|band_changed|resolved` (topic `hypotheses`)

    Attributes:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        version (int):
        status (HypothesisSetEventStatus):
        at (datetime.datetime):
        question (str | Unset):
        revision_id (None | str | Unset):
        cause (HypothesisSetEventCauseType0 | None | Unset):
        hypotheses (list[HypothesisProbabilityState] | Unset):
        band_changes (list[HypothesisSetEventBandChangesItem] | Unset):
        private (bool | Unset):
    """

    set_id: str
    version: int
    status: HypothesisSetEventStatus
    at: datetime.datetime
    question: str | Unset = UNSET
    revision_id: None | str | Unset = UNSET
    cause: HypothesisSetEventCauseType0 | None | Unset = UNSET
    hypotheses: list[HypothesisProbabilityState] | Unset = UNSET
    band_changes: list[HypothesisSetEventBandChangesItem] | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        set_id = self.set_id

        version = self.version

        status = self.status.value

        at = self.at.isoformat()

        question = self.question

        revision_id: None | str | Unset
        if isinstance(self.revision_id, Unset):
            revision_id = UNSET
        else:
            revision_id = self.revision_id

        cause: None | str | Unset
        if isinstance(self.cause, Unset):
            cause = UNSET
        elif isinstance(self.cause, HypothesisSetEventCauseType0):
            cause = self.cause.value
        else:
            cause = self.cause

        hypotheses: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.hypotheses, Unset):
            hypotheses = []
            for hypotheses_item_data in self.hypotheses:
                hypotheses_item = hypotheses_item_data.to_dict()
                hypotheses.append(hypotheses_item)

        band_changes: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.band_changes, Unset):
            band_changes = []
            for band_changes_item_data in self.band_changes:
                band_changes_item = band_changes_item_data.to_dict()
                band_changes.append(band_changes_item)

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "setId": set_id,
                "version": version,
                "status": status,
                "at": at,
            }
        )
        if question is not UNSET:
            field_dict["question"] = question
        if revision_id is not UNSET:
            field_dict["revisionId"] = revision_id
        if cause is not UNSET:
            field_dict["cause"] = cause
        if hypotheses is not UNSET:
            field_dict["hypotheses"] = hypotheses
        if band_changes is not UNSET:
            field_dict["bandChanges"] = band_changes
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_probability_state import HypothesisProbabilityState
        from ..models.hypothesis_set_event_band_changes_item import (
            HypothesisSetEventBandChangesItem,
        )

        d = dict(src_dict)
        set_id = d.pop("setId")

        version = d.pop("version")

        status = HypothesisSetEventStatus(d.pop("status"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        question = d.pop("question", UNSET)

        def _parse_revision_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        revision_id = _parse_revision_id(d.pop("revisionId", UNSET))

        def _parse_cause(data: object) -> HypothesisSetEventCauseType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                cause_type_0 = HypothesisSetEventCauseType0(data)

                return cause_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisSetEventCauseType0 | None | Unset, data)

        cause = _parse_cause(d.pop("cause", UNSET))

        _hypotheses = d.pop("hypotheses", UNSET)
        hypotheses: list[HypothesisProbabilityState] | Unset = UNSET
        if _hypotheses is not UNSET:
            hypotheses = []
            for hypotheses_item_data in _hypotheses:
                hypotheses_item = HypothesisProbabilityState.from_dict(
                    hypotheses_item_data
                )

                hypotheses.append(hypotheses_item)

        _band_changes = d.pop("bandChanges", UNSET)
        band_changes: list[HypothesisSetEventBandChangesItem] | Unset = UNSET
        if _band_changes is not UNSET:
            band_changes = []
            for band_changes_item_data in _band_changes:
                band_changes_item = HypothesisSetEventBandChangesItem.from_dict(
                    band_changes_item_data
                )

                band_changes.append(band_changes_item)

        private = d.pop("private", UNSET)

        hypothesis_set_event = cls(
            set_id=set_id,
            version=version,
            status=status,
            at=at,
            question=question,
            revision_id=revision_id,
            cause=cause,
            hypotheses=hypotheses,
            band_changes=band_changes,
            private=private,
        )

        hypothesis_set_event.additional_properties = d
        return hypothesis_set_event

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
