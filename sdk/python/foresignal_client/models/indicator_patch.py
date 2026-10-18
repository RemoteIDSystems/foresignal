from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_patch_credibility import IndicatorPatchCredibility
from ..models.indicator_patch_relevance import IndicatorPatchRelevance
from ..models.indicator_patch_state import IndicatorPatchState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.indicator_patch_absence_lr import IndicatorPatchAbsenceLr
    from ..models.indicator_patch_lr import IndicatorPatchLr
    from ..models.indicator_patch_observed_evidence_type_0 import (
        IndicatorPatchObservedEvidenceType0,
    )
    from ..models.indicator_signal import IndicatorSignal


T = TypeVar("T", bound="IndicatorPatch")


@_attrs_define
class IndicatorPatch:
    """JSON merge patch for an indicator; signal edits update its watch rule

    Attributes:
        description (str | Unset):
        signal (IndicatorSignal | Unset): What the indicator watches; `type` selects the populated member
        signal_family (str | Unset):
        credibility (IndicatorPatchCredibility | Unset):
        relevance (IndicatorPatchRelevance | Unset):
        expected_by (datetime.datetime | None | Unset):
        lr (IndicatorPatchLr | Unset): Per hypothesis id (or label when created with its set)
        absence_lr (IndicatorPatchAbsenceLr | Unset): Optional absence LR overrides per hypothesis id
        state (IndicatorPatchState | Unset): Manual indicators only: record an observation or absence
        observed_evidence (IndicatorPatchObservedEvidenceType0 | None | Unset):
    """

    description: str | Unset = UNSET
    signal: IndicatorSignal | Unset = UNSET
    signal_family: str | Unset = UNSET
    credibility: IndicatorPatchCredibility | Unset = UNSET
    relevance: IndicatorPatchRelevance | Unset = UNSET
    expected_by: datetime.datetime | None | Unset = UNSET
    lr: IndicatorPatchLr | Unset = UNSET
    absence_lr: IndicatorPatchAbsenceLr | Unset = UNSET
    state: IndicatorPatchState | Unset = UNSET
    observed_evidence: IndicatorPatchObservedEvidenceType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.indicator_patch_observed_evidence_type_0 import (
            IndicatorPatchObservedEvidenceType0,
        )

        description = self.description

        signal: dict[str, Any] | Unset = UNSET
        if not isinstance(self.signal, Unset):
            signal = self.signal.to_dict()

        signal_family = self.signal_family

        credibility: str | Unset = UNSET
        if not isinstance(self.credibility, Unset):
            credibility = self.credibility.value

        relevance: str | Unset = UNSET
        if not isinstance(self.relevance, Unset):
            relevance = self.relevance.value

        expected_by: None | str | Unset
        if isinstance(self.expected_by, Unset):
            expected_by = UNSET
        elif isinstance(self.expected_by, datetime.datetime):
            expected_by = self.expected_by.isoformat()
        else:
            expected_by = self.expected_by

        lr: dict[str, Any] | Unset = UNSET
        if not isinstance(self.lr, Unset):
            lr = self.lr.to_dict()

        absence_lr: dict[str, Any] | Unset = UNSET
        if not isinstance(self.absence_lr, Unset):
            absence_lr = self.absence_lr.to_dict()

        state: str | Unset = UNSET
        if not isinstance(self.state, Unset):
            state = self.state.value

        observed_evidence: dict[str, Any] | None | Unset
        if isinstance(self.observed_evidence, Unset):
            observed_evidence = UNSET
        elif isinstance(self.observed_evidence, IndicatorPatchObservedEvidenceType0):
            observed_evidence = self.observed_evidence.to_dict()
        else:
            observed_evidence = self.observed_evidence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if description is not UNSET:
            field_dict["description"] = description
        if signal is not UNSET:
            field_dict["signal"] = signal
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if credibility is not UNSET:
            field_dict["credibility"] = credibility
        if relevance is not UNSET:
            field_dict["relevance"] = relevance
        if expected_by is not UNSET:
            field_dict["expectedBy"] = expected_by
        if lr is not UNSET:
            field_dict["lr"] = lr
        if absence_lr is not UNSET:
            field_dict["absenceLr"] = absence_lr
        if state is not UNSET:
            field_dict["state"] = state
        if observed_evidence is not UNSET:
            field_dict["observedEvidence"] = observed_evidence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.indicator_patch_absence_lr import IndicatorPatchAbsenceLr
        from ..models.indicator_patch_lr import IndicatorPatchLr
        from ..models.indicator_patch_observed_evidence_type_0 import (
            IndicatorPatchObservedEvidenceType0,
        )
        from ..models.indicator_signal import IndicatorSignal

        d = dict(src_dict)
        description = d.pop("description", UNSET)

        _signal = d.pop("signal", UNSET)
        signal: IndicatorSignal | Unset
        if isinstance(_signal, Unset):
            signal = UNSET
        else:
            signal = IndicatorSignal.from_dict(_signal)

        signal_family = d.pop("signalFamily", UNSET)

        _credibility = d.pop("credibility", UNSET)
        credibility: IndicatorPatchCredibility | Unset
        if isinstance(_credibility, Unset):
            credibility = UNSET
        else:
            credibility = IndicatorPatchCredibility(_credibility)

        _relevance = d.pop("relevance", UNSET)
        relevance: IndicatorPatchRelevance | Unset
        if isinstance(_relevance, Unset):
            relevance = UNSET
        else:
            relevance = IndicatorPatchRelevance(_relevance)

        def _parse_expected_by(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expected_by_type_0 = datetime.datetime.fromisoformat(data)

                return expected_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expected_by = _parse_expected_by(d.pop("expectedBy", UNSET))

        _lr = d.pop("lr", UNSET)
        lr: IndicatorPatchLr | Unset
        if isinstance(_lr, Unset):
            lr = UNSET
        else:
            lr = IndicatorPatchLr.from_dict(_lr)

        _absence_lr = d.pop("absenceLr", UNSET)
        absence_lr: IndicatorPatchAbsenceLr | Unset
        if isinstance(_absence_lr, Unset):
            absence_lr = UNSET
        else:
            absence_lr = IndicatorPatchAbsenceLr.from_dict(_absence_lr)

        _state = d.pop("state", UNSET)
        state: IndicatorPatchState | Unset
        if isinstance(_state, Unset):
            state = UNSET
        else:
            state = IndicatorPatchState(_state)

        def _parse_observed_evidence(
            data: object,
        ) -> IndicatorPatchObservedEvidenceType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                observed_evidence_type_0 = (
                    IndicatorPatchObservedEvidenceType0.from_dict(data)
                )

                return observed_evidence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorPatchObservedEvidenceType0 | None | Unset, data)

        observed_evidence = _parse_observed_evidence(d.pop("observedEvidence", UNSET))

        indicator_patch = cls(
            description=description,
            signal=signal,
            signal_family=signal_family,
            credibility=credibility,
            relevance=relevance,
            expected_by=expected_by,
            lr=lr,
            absence_lr=absence_lr,
            state=state,
            observed_evidence=observed_evidence,
        )

        indicator_patch.additional_properties = d
        return indicator_patch

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
