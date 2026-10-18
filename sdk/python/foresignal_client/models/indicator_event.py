from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_event_previous_state import IndicatorEventPreviousState
from ..models.indicator_event_state import IndicatorEventState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.indicator_event_evidence_type_0 import IndicatorEventEvidenceType0


T = TypeVar("T", bound="IndicatorEvent")


@_attrs_define
class IndicatorEvent:
    """SSE payload for `indicator.observed|absent` (topic `hypotheses`)

    Attributes:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        state (IndicatorEventState):
        at (datetime.datetime):
        description (str | Unset):
        signal_family (str | Unset):
        previous_state (IndicatorEventPreviousState | Unset):
        evidence (IndicatorEventEvidenceType0 | None | Unset):
        revision_id (None | str | Unset):
        private (bool | Unset):
    """

    indicator_id: str
    set_id: str
    state: IndicatorEventState
    at: datetime.datetime
    description: str | Unset = UNSET
    signal_family: str | Unset = UNSET
    previous_state: IndicatorEventPreviousState | Unset = UNSET
    evidence: IndicatorEventEvidenceType0 | None | Unset = UNSET
    revision_id: None | str | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.indicator_event_evidence_type_0 import IndicatorEventEvidenceType0

        indicator_id = self.indicator_id

        set_id = self.set_id

        state = self.state.value

        at = self.at.isoformat()

        description = self.description

        signal_family = self.signal_family

        previous_state: str | Unset = UNSET
        if not isinstance(self.previous_state, Unset):
            previous_state = self.previous_state.value

        evidence: dict[str, Any] | None | Unset
        if isinstance(self.evidence, Unset):
            evidence = UNSET
        elif isinstance(self.evidence, IndicatorEventEvidenceType0):
            evidence = self.evidence.to_dict()
        else:
            evidence = self.evidence

        revision_id: None | str | Unset
        if isinstance(self.revision_id, Unset):
            revision_id = UNSET
        else:
            revision_id = self.revision_id

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "indicatorId": indicator_id,
                "setId": set_id,
                "state": state,
                "at": at,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if previous_state is not UNSET:
            field_dict["previousState"] = previous_state
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if revision_id is not UNSET:
            field_dict["revisionId"] = revision_id
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.indicator_event_evidence_type_0 import IndicatorEventEvidenceType0

        d = dict(src_dict)
        indicator_id = d.pop("indicatorId")

        set_id = d.pop("setId")

        state = IndicatorEventState(d.pop("state"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        description = d.pop("description", UNSET)

        signal_family = d.pop("signalFamily", UNSET)

        _previous_state = d.pop("previousState", UNSET)
        previous_state: IndicatorEventPreviousState | Unset
        if isinstance(_previous_state, Unset):
            previous_state = UNSET
        else:
            previous_state = IndicatorEventPreviousState(_previous_state)

        def _parse_evidence(data: object) -> IndicatorEventEvidenceType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                evidence_type_0 = IndicatorEventEvidenceType0.from_dict(data)

                return evidence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorEventEvidenceType0 | None | Unset, data)

        evidence = _parse_evidence(d.pop("evidence", UNSET))

        def _parse_revision_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        revision_id = _parse_revision_id(d.pop("revisionId", UNSET))

        private = d.pop("private", UNSET)

        indicator_event = cls(
            indicator_id=indicator_id,
            set_id=set_id,
            state=state,
            at=at,
            description=description,
            signal_family=signal_family,
            previous_state=previous_state,
            evidence=evidence,
            revision_id=revision_id,
            private=private,
        )

        indicator_event.additional_properties = d
        return indicator_event

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
