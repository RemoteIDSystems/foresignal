from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_credibility import IndicatorCredibility
from ..models.indicator_origin import IndicatorOrigin
from ..models.indicator_relevance import IndicatorRelevance
from ..models.indicator_state import IndicatorState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.indicator_absence_lr import IndicatorAbsenceLr
    from ..models.indicator_lr import IndicatorLr
    from ..models.indicator_observed_evidence_type_0 import (
        IndicatorObservedEvidenceType0,
    )
    from ..models.indicator_signal import IndicatorSignal


T = TypeVar("T", bound="Indicator")


@_attrs_define
class Indicator:
    """An observable signal with likelihood ratios that watches for itself through a trigger rule

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        description (str):
        signal (IndicatorSignal): What the indicator watches; `type` selects the populated member
        signal_family (str): 119 signal-family registry id Example: airspace.
        credibility (IndicatorCredibility):
        relevance (IndicatorRelevance):
        lr (IndicatorLr): Per hypothesis id (or label when created with its set)
        state (IndicatorState):
        origin (IndicatorOrigin):
        expected_by (datetime.datetime | None | Unset):
        absence_lr (IndicatorAbsenceLr | Unset): Optional absence LR overrides per hypothesis id
        watch_rule_id (None | str | Unset): 017 trigger rule tagged `indicator`, `origin-hypothesis` and `ind-{id in
            lower case}` (prompt 125 names `indicator:{id}` and `origin:hypothesis`, but a rule tag cannot hold a colon); it
            routes to no channel unless a channel names it in `ruleIds`; disabled once the indicator is observed, absent or
            retired
        p_detect (float | None | Unset): 116 detection probability for the family, area and window
        observed_evidence (IndicatorObservedEvidenceType0 | None | Unset):
        last_state_change_at (datetime.datetime | None | Unset):
        version (int | Unset):
    """

    id: str
    set_id: str
    description: str
    signal: IndicatorSignal
    signal_family: str
    credibility: IndicatorCredibility
    relevance: IndicatorRelevance
    lr: IndicatorLr
    state: IndicatorState
    origin: IndicatorOrigin
    expected_by: datetime.datetime | None | Unset = UNSET
    absence_lr: IndicatorAbsenceLr | Unset = UNSET
    watch_rule_id: None | str | Unset = UNSET
    p_detect: float | None | Unset = UNSET
    observed_evidence: IndicatorObservedEvidenceType0 | None | Unset = UNSET
    last_state_change_at: datetime.datetime | None | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.indicator_observed_evidence_type_0 import (
            IndicatorObservedEvidenceType0,
        )

        id = self.id

        set_id = self.set_id

        description = self.description

        signal = self.signal.to_dict()

        signal_family = self.signal_family

        credibility = self.credibility.value

        relevance = self.relevance.value

        lr = self.lr.to_dict()

        state = self.state.value

        origin = self.origin.value

        expected_by: None | str | Unset
        if isinstance(self.expected_by, Unset):
            expected_by = UNSET
        elif isinstance(self.expected_by, datetime.datetime):
            expected_by = self.expected_by.isoformat()
        else:
            expected_by = self.expected_by

        absence_lr: dict[str, Any] | Unset = UNSET
        if not isinstance(self.absence_lr, Unset):
            absence_lr = self.absence_lr.to_dict()

        watch_rule_id: None | str | Unset
        if isinstance(self.watch_rule_id, Unset):
            watch_rule_id = UNSET
        else:
            watch_rule_id = self.watch_rule_id

        p_detect: float | None | Unset
        if isinstance(self.p_detect, Unset):
            p_detect = UNSET
        else:
            p_detect = self.p_detect

        observed_evidence: dict[str, Any] | None | Unset
        if isinstance(self.observed_evidence, Unset):
            observed_evidence = UNSET
        elif isinstance(self.observed_evidence, IndicatorObservedEvidenceType0):
            observed_evidence = self.observed_evidence.to_dict()
        else:
            observed_evidence = self.observed_evidence

        last_state_change_at: None | str | Unset
        if isinstance(self.last_state_change_at, Unset):
            last_state_change_at = UNSET
        elif isinstance(self.last_state_change_at, datetime.datetime):
            last_state_change_at = self.last_state_change_at.isoformat()
        else:
            last_state_change_at = self.last_state_change_at

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "setId": set_id,
                "description": description,
                "signal": signal,
                "signalFamily": signal_family,
                "credibility": credibility,
                "relevance": relevance,
                "lr": lr,
                "state": state,
                "origin": origin,
            }
        )
        if expected_by is not UNSET:
            field_dict["expectedBy"] = expected_by
        if absence_lr is not UNSET:
            field_dict["absenceLr"] = absence_lr
        if watch_rule_id is not UNSET:
            field_dict["watchRuleId"] = watch_rule_id
        if p_detect is not UNSET:
            field_dict["pDetect"] = p_detect
        if observed_evidence is not UNSET:
            field_dict["observedEvidence"] = observed_evidence
        if last_state_change_at is not UNSET:
            field_dict["lastStateChangeAt"] = last_state_change_at
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.indicator_absence_lr import IndicatorAbsenceLr
        from ..models.indicator_lr import IndicatorLr
        from ..models.indicator_observed_evidence_type_0 import (
            IndicatorObservedEvidenceType0,
        )
        from ..models.indicator_signal import IndicatorSignal

        d = dict(src_dict)
        id = d.pop("id")

        set_id = d.pop("setId")

        description = d.pop("description")

        signal = IndicatorSignal.from_dict(d.pop("signal"))

        signal_family = d.pop("signalFamily")

        credibility = IndicatorCredibility(d.pop("credibility"))

        relevance = IndicatorRelevance(d.pop("relevance"))

        lr = IndicatorLr.from_dict(d.pop("lr"))

        state = IndicatorState(d.pop("state"))

        origin = IndicatorOrigin(d.pop("origin"))

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

        _absence_lr = d.pop("absenceLr", UNSET)
        absence_lr: IndicatorAbsenceLr | Unset
        if isinstance(_absence_lr, Unset):
            absence_lr = UNSET
        else:
            absence_lr = IndicatorAbsenceLr.from_dict(_absence_lr)

        def _parse_watch_rule_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        watch_rule_id = _parse_watch_rule_id(d.pop("watchRuleId", UNSET))

        def _parse_p_detect(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_detect = _parse_p_detect(d.pop("pDetect", UNSET))

        def _parse_observed_evidence(
            data: object,
        ) -> IndicatorObservedEvidenceType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                observed_evidence_type_0 = IndicatorObservedEvidenceType0.from_dict(
                    data
                )

                return observed_evidence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorObservedEvidenceType0 | None | Unset, data)

        observed_evidence = _parse_observed_evidence(d.pop("observedEvidence", UNSET))

        def _parse_last_state_change_at(
            data: object,
        ) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_state_change_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_state_change_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_state_change_at = _parse_last_state_change_at(
            d.pop("lastStateChangeAt", UNSET)
        )

        version = d.pop("version", UNSET)

        indicator = cls(
            id=id,
            set_id=set_id,
            description=description,
            signal=signal,
            signal_family=signal_family,
            credibility=credibility,
            relevance=relevance,
            lr=lr,
            state=state,
            origin=origin,
            expected_by=expected_by,
            absence_lr=absence_lr,
            watch_rule_id=watch_rule_id,
            p_detect=p_detect,
            observed_evidence=observed_evidence,
            last_state_change_at=last_state_change_at,
            version=version,
        )

        indicator.additional_properties = d
        return indicator

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
