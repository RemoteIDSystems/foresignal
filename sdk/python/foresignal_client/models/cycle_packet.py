from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.cycle_packet_cadence import CyclePacketCadence
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.collection_gap import CollectionGap
    from ..models.cycle_packet_coverage import CyclePacketCoverage
    from ..models.cycle_packet_forecasts import CyclePacketForecasts
    from ..models.cycle_packet_hypotheses import CyclePacketHypotheses
    from ..models.cycle_packet_item import CyclePacketItem
    from ..models.cycle_packet_outlooks import CyclePacketOutlooks
    from ..models.cycle_packet_situations import CyclePacketSituations
    from ..models.cycle_packet_truncated_for_llm import CyclePacketTruncatedForLlm
    from ..models.key_judgment import KeyJudgment
    from ..models.social_versus_observed import SocialVersusObserved


T = TypeVar("T", bound="CyclePacket")


@_attrs_define
class CyclePacket:
    """Deterministic packet of what changed since the previous run (`cycle-packet` 1.0.0)

    Attributes:
        version (str):  Example: cycle-packet 1.0.0.
        since (datetime.datetime):
        until (datetime.datetime):
        first_run (bool | Unset): Lookback used the cadence's natural window
        situations (CyclePacketSituations | Unset): Situation changes (118 snapshot diffs)
        hypotheses (CyclePacketHypotheses | Unset): Hypothesis changes
        findings (list[CyclePacketItem] | Unset):
        forecasts (CyclePacketForecasts | Unset): Ledger changes
        outlooks (CyclePacketOutlooks | Unset): Outlook and index changes
        coverage (CyclePacketCoverage | Unset): Coverage caveats
        collection_gaps (list[CollectionGap] | Unset):
        verdicts (list[CyclePacketItem] | Unset):
        previous_judgments (list[KeyJudgment] | Unset):
        truncated_for_llm (CyclePacketTruncatedForLlm | Unset): Items beyond the 30-per-section LLM cap, per section
        social (None | SocialVersusObserved | Unset): Narratives by status, attention gaps, out-of-sample lead times,
            media reuse and coordination findings (137)
        cadence (CyclePacketCadence | Unset): The cadence the packet was built for
    """

    version: str
    since: datetime.datetime
    until: datetime.datetime
    first_run: bool | Unset = UNSET
    situations: CyclePacketSituations | Unset = UNSET
    hypotheses: CyclePacketHypotheses | Unset = UNSET
    findings: list[CyclePacketItem] | Unset = UNSET
    forecasts: CyclePacketForecasts | Unset = UNSET
    outlooks: CyclePacketOutlooks | Unset = UNSET
    coverage: CyclePacketCoverage | Unset = UNSET
    collection_gaps: list[CollectionGap] | Unset = UNSET
    verdicts: list[CyclePacketItem] | Unset = UNSET
    previous_judgments: list[KeyJudgment] | Unset = UNSET
    truncated_for_llm: CyclePacketTruncatedForLlm | Unset = UNSET
    social: None | SocialVersusObserved | Unset = UNSET
    cadence: CyclePacketCadence | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.social_versus_observed import SocialVersusObserved

        version = self.version

        since = self.since.isoformat()

        until = self.until.isoformat()

        first_run = self.first_run

        situations: dict[str, Any] | Unset = UNSET
        if not isinstance(self.situations, Unset):
            situations = self.situations.to_dict()

        hypotheses: dict[str, Any] | Unset = UNSET
        if not isinstance(self.hypotheses, Unset):
            hypotheses = self.hypotheses.to_dict()

        findings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.findings, Unset):
            findings = []
            for findings_item_data in self.findings:
                findings_item = findings_item_data.to_dict()
                findings.append(findings_item)

        forecasts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.forecasts, Unset):
            forecasts = self.forecasts.to_dict()

        outlooks: dict[str, Any] | Unset = UNSET
        if not isinstance(self.outlooks, Unset):
            outlooks = self.outlooks.to_dict()

        coverage: dict[str, Any] | Unset = UNSET
        if not isinstance(self.coverage, Unset):
            coverage = self.coverage.to_dict()

        collection_gaps: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.collection_gaps, Unset):
            collection_gaps = []
            for collection_gaps_item_data in self.collection_gaps:
                collection_gaps_item = collection_gaps_item_data.to_dict()
                collection_gaps.append(collection_gaps_item)

        verdicts: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.verdicts, Unset):
            verdicts = []
            for verdicts_item_data in self.verdicts:
                verdicts_item = verdicts_item_data.to_dict()
                verdicts.append(verdicts_item)

        previous_judgments: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.previous_judgments, Unset):
            previous_judgments = []
            for previous_judgments_item_data in self.previous_judgments:
                previous_judgments_item = previous_judgments_item_data.to_dict()
                previous_judgments.append(previous_judgments_item)

        truncated_for_llm: dict[str, Any] | Unset = UNSET
        if not isinstance(self.truncated_for_llm, Unset):
            truncated_for_llm = self.truncated_for_llm.to_dict()

        social: dict[str, Any] | None | Unset
        if isinstance(self.social, Unset):
            social = UNSET
        elif isinstance(self.social, SocialVersusObserved):
            social = self.social.to_dict()
        else:
            social = self.social

        cadence: str | Unset = UNSET
        if not isinstance(self.cadence, Unset):
            cadence = self.cadence.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "since": since,
                "until": until,
            }
        )
        if first_run is not UNSET:
            field_dict["firstRun"] = first_run
        if situations is not UNSET:
            field_dict["situations"] = situations
        if hypotheses is not UNSET:
            field_dict["hypotheses"] = hypotheses
        if findings is not UNSET:
            field_dict["findings"] = findings
        if forecasts is not UNSET:
            field_dict["forecasts"] = forecasts
        if outlooks is not UNSET:
            field_dict["outlooks"] = outlooks
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if collection_gaps is not UNSET:
            field_dict["collectionGaps"] = collection_gaps
        if verdicts is not UNSET:
            field_dict["verdicts"] = verdicts
        if previous_judgments is not UNSET:
            field_dict["previousJudgments"] = previous_judgments
        if truncated_for_llm is not UNSET:
            field_dict["truncatedForLlm"] = truncated_for_llm
        if social is not UNSET:
            field_dict["social"] = social
        if cadence is not UNSET:
            field_dict["cadence"] = cadence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.collection_gap import CollectionGap
        from ..models.cycle_packet_coverage import CyclePacketCoverage
        from ..models.cycle_packet_forecasts import CyclePacketForecasts
        from ..models.cycle_packet_hypotheses import CyclePacketHypotheses
        from ..models.cycle_packet_item import CyclePacketItem
        from ..models.cycle_packet_outlooks import CyclePacketOutlooks
        from ..models.cycle_packet_situations import CyclePacketSituations
        from ..models.cycle_packet_truncated_for_llm import CyclePacketTruncatedForLlm
        from ..models.key_judgment import KeyJudgment
        from ..models.social_versus_observed import SocialVersusObserved

        d = dict(src_dict)
        version = d.pop("version")

        since = datetime.datetime.fromisoformat(d.pop("since"))

        until = datetime.datetime.fromisoformat(d.pop("until"))

        first_run = d.pop("firstRun", UNSET)

        _situations = d.pop("situations", UNSET)
        situations: CyclePacketSituations | Unset
        if isinstance(_situations, Unset):
            situations = UNSET
        else:
            situations = CyclePacketSituations.from_dict(_situations)

        _hypotheses = d.pop("hypotheses", UNSET)
        hypotheses: CyclePacketHypotheses | Unset
        if isinstance(_hypotheses, Unset):
            hypotheses = UNSET
        else:
            hypotheses = CyclePacketHypotheses.from_dict(_hypotheses)

        _findings = d.pop("findings", UNSET)
        findings: list[CyclePacketItem] | Unset = UNSET
        if _findings is not UNSET:
            findings = []
            for findings_item_data in _findings:
                findings_item = CyclePacketItem.from_dict(findings_item_data)

                findings.append(findings_item)

        _forecasts = d.pop("forecasts", UNSET)
        forecasts: CyclePacketForecasts | Unset
        if isinstance(_forecasts, Unset):
            forecasts = UNSET
        else:
            forecasts = CyclePacketForecasts.from_dict(_forecasts)

        _outlooks = d.pop("outlooks", UNSET)
        outlooks: CyclePacketOutlooks | Unset
        if isinstance(_outlooks, Unset):
            outlooks = UNSET
        else:
            outlooks = CyclePacketOutlooks.from_dict(_outlooks)

        _coverage = d.pop("coverage", UNSET)
        coverage: CyclePacketCoverage | Unset
        if isinstance(_coverage, Unset):
            coverage = UNSET
        else:
            coverage = CyclePacketCoverage.from_dict(_coverage)

        _collection_gaps = d.pop("collectionGaps", UNSET)
        collection_gaps: list[CollectionGap] | Unset = UNSET
        if _collection_gaps is not UNSET:
            collection_gaps = []
            for collection_gaps_item_data in _collection_gaps:
                collection_gaps_item = CollectionGap.from_dict(
                    collection_gaps_item_data
                )

                collection_gaps.append(collection_gaps_item)

        _verdicts = d.pop("verdicts", UNSET)
        verdicts: list[CyclePacketItem] | Unset = UNSET
        if _verdicts is not UNSET:
            verdicts = []
            for verdicts_item_data in _verdicts:
                verdicts_item = CyclePacketItem.from_dict(verdicts_item_data)

                verdicts.append(verdicts_item)

        _previous_judgments = d.pop("previousJudgments", UNSET)
        previous_judgments: list[KeyJudgment] | Unset = UNSET
        if _previous_judgments is not UNSET:
            previous_judgments = []
            for previous_judgments_item_data in _previous_judgments:
                previous_judgments_item = KeyJudgment.from_dict(
                    previous_judgments_item_data
                )

                previous_judgments.append(previous_judgments_item)

        _truncated_for_llm = d.pop("truncatedForLlm", UNSET)
        truncated_for_llm: CyclePacketTruncatedForLlm | Unset
        if isinstance(_truncated_for_llm, Unset):
            truncated_for_llm = UNSET
        else:
            truncated_for_llm = CyclePacketTruncatedForLlm.from_dict(_truncated_for_llm)

        def _parse_social(data: object) -> None | SocialVersusObserved | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                social_type_0 = SocialVersusObserved.from_dict(data)

                return social_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialVersusObserved | Unset, data)

        social = _parse_social(d.pop("social", UNSET))

        _cadence = d.pop("cadence", UNSET)
        cadence: CyclePacketCadence | Unset
        if isinstance(_cadence, Unset):
            cadence = UNSET
        else:
            cadence = CyclePacketCadence(_cadence)

        cycle_packet = cls(
            version=version,
            since=since,
            until=until,
            first_run=first_run,
            situations=situations,
            hypotheses=hypotheses,
            findings=findings,
            forecasts=forecasts,
            outlooks=outlooks,
            coverage=coverage,
            collection_gaps=collection_gaps,
            verdicts=verdicts,
            previous_judgments=previous_judgments,
            truncated_for_llm=truncated_for_llm,
            social=social,
            cadence=cadence,
        )

        cycle_packet.additional_properties = d
        return cycle_packet

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
