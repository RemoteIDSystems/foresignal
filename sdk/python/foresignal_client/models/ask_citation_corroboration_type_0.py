from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.ask_citation_corroboration_type_0_evidence_quality import (
    AskCitationCorroborationType0EvidenceQuality,
)
from ..models.ask_citation_corroboration_type_0_reliability_type_1 import (
    AskCitationCorroborationType0ReliabilityType1,
)
from ..models.ask_citation_corroboration_type_0_reliability_type_2_type_1 import (
    AskCitationCorroborationType0ReliabilityType2Type1,
)
from ..models.ask_citation_corroboration_type_0_reliability_type_3_type_1 import (
    AskCitationCorroborationType0ReliabilityType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="AskCitationCorroborationType0")


@_attrs_define
class AskCitationCorroborationType0:
    """Briefing-run citations of a briefing with `includeCorroboration` only: how well-supported the item is (NP-007), from
    the 154 corroboration policy; absent elsewhere

        Attributes:
            evidence_quality (AskCitationCorroborationType0EvidenceQuality):
            independent_sources (int):
            credibility (int | None | Unset): Admiralty information credibility; 6 means unknown
            reliability (AskCitationCorroborationType0ReliabilityType1 | AskCitationCorroborationType0ReliabilityType2Type1
                | AskCitationCorroborationType0ReliabilityType3Type1 | None | Unset): Source reliability (109); F means unknown
    """

    evidence_quality: AskCitationCorroborationType0EvidenceQuality
    independent_sources: int
    credibility: int | None | Unset = UNSET
    reliability: (
        AskCitationCorroborationType0ReliabilityType1
        | AskCitationCorroborationType0ReliabilityType2Type1
        | AskCitationCorroborationType0ReliabilityType3Type1
        | None
        | Unset
    ) = UNSET

    def to_dict(self) -> dict[str, Any]:
        evidence_quality = self.evidence_quality.value

        independent_sources = self.independent_sources

        credibility: int | None | Unset
        if isinstance(self.credibility, Unset):
            credibility = UNSET
        else:
            credibility = self.credibility

        reliability: None | str | Unset
        if isinstance(self.reliability, Unset):
            reliability = UNSET
        elif (
            isinstance(self.reliability, AskCitationCorroborationType0ReliabilityType1)
            or isinstance(
                self.reliability, AskCitationCorroborationType0ReliabilityType2Type1
            )
            or isinstance(
                self.reliability, AskCitationCorroborationType0ReliabilityType3Type1
            )
        ):
            reliability = self.reliability.value
        else:
            reliability = self.reliability

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "evidenceQuality": evidence_quality,
                "independentSources": independent_sources,
            }
        )
        if credibility is not UNSET:
            field_dict["credibility"] = credibility
        if reliability is not UNSET:
            field_dict["reliability"] = reliability

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        evidence_quality = AskCitationCorroborationType0EvidenceQuality(
            d.pop("evidenceQuality")
        )

        independent_sources = d.pop("independentSources")

        def _parse_credibility(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        credibility = _parse_credibility(d.pop("credibility", UNSET))

        def _parse_reliability(
            data: object,
        ) -> (
            AskCitationCorroborationType0ReliabilityType1
            | AskCitationCorroborationType0ReliabilityType2Type1
            | AskCitationCorroborationType0ReliabilityType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_1 = AskCitationCorroborationType0ReliabilityType1(data)

                return reliability_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_2_type_1 = (
                    AskCitationCorroborationType0ReliabilityType2Type1(data)
                )

                return reliability_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_3_type_1 = (
                    AskCitationCorroborationType0ReliabilityType3Type1(data)
                )

                return reliability_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AskCitationCorroborationType0ReliabilityType1
                | AskCitationCorroborationType0ReliabilityType2Type1
                | AskCitationCorroborationType0ReliabilityType3Type1
                | None
                | Unset,
                data,
            )

        reliability = _parse_reliability(d.pop("reliability", UNSET))

        ask_citation_corroboration_type_0 = cls(
            evidence_quality=evidence_quality,
            independent_sources=independent_sources,
            credibility=credibility,
            reliability=reliability,
        )

        return ask_citation_corroboration_type_0
