from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.coverage_stats_blindspot_type_1 import CoverageStatsBlindspotType1
from ..models.coverage_stats_blindspot_type_2_type_1 import (
    CoverageStatsBlindspotType2Type1,
)
from ..models.coverage_stats_blindspot_type_3_type_1 import (
    CoverageStatsBlindspotType3Type1,
)
from ..models.coverage_stats_scope import CoverageStatsScope
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.coverage_stats_articles import CoverageStatsArticles
    from ..models.coverage_stats_by_country import CoverageStatsByCountry
    from ..models.coverage_stats_by_factuality import CoverageStatsByFactuality
    from ..models.coverage_stats_by_outlet_type import CoverageStatsByOutletType
    from ..models.coverage_stats_coverage_balance import CoverageStatsCoverageBalance
    from ..models.coverage_stats_evidence_groups import CoverageStatsEvidenceGroups
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="CoverageStats")


@_attrs_define
class CoverageStats:
    """Coverage of a story, topic, situation or a hypothesis's news support (CoverageAnalyzer 1.0.0); describes reporting,
    never truth

        Attributes:
            articles (CoverageStatsArticles): Counts by lean bucket
            evidence_groups (CoverageStatsEvidenceGroups): Counts by lean bucket
            coverage_balance (CoverageStatsCoverageBalance): Shares by lean bucket (0–1)
            state_affiliated_share (float):
            n_rated (int): Rated evidence groups
            corroborated (bool): Linked to ≥ 1 non-news evidence group in the same place and window (116)
            scope (CoverageStatsScope | Unset):
            by_country (CoverageStatsByCountry | Unset): ISO country → evidence groups
            by_outlet_type (CoverageStatsByOutletType | Unset): Outlet type → evidence groups
            by_factuality (CoverageStatsByFactuality | Unset): Factuality band (1–5, unrated) → evidence groups
            blindspot (CoverageStatsBlindspotType1 | CoverageStatsBlindspotType2Type1 | CoverageStatsBlindspotType3Type1 |
                None | Unset): ≥ 75% of rated evidence groups from one side with nRated ≥ 8
            corroborated_share (float | None | Unset): Situations: share of news members corroborated
            skewed (bool | Unset): ≥ 75% from one bucket or from state-affiliated outlets (SKEWED_COVERAGE)
            caveats (list[Caveat] | Unset):
            analyzer (DetectorRef | Unset):
            manifest_id (None | str | Unset):
            computed_at (datetime.datetime | Unset):
    """

    articles: CoverageStatsArticles
    evidence_groups: CoverageStatsEvidenceGroups
    coverage_balance: CoverageStatsCoverageBalance
    state_affiliated_share: float
    n_rated: int
    corroborated: bool
    scope: CoverageStatsScope | Unset = UNSET
    by_country: CoverageStatsByCountry | Unset = UNSET
    by_outlet_type: CoverageStatsByOutletType | Unset = UNSET
    by_factuality: CoverageStatsByFactuality | Unset = UNSET
    blindspot: (
        CoverageStatsBlindspotType1
        | CoverageStatsBlindspotType2Type1
        | CoverageStatsBlindspotType3Type1
        | None
        | Unset
    ) = UNSET
    corroborated_share: float | None | Unset = UNSET
    skewed: bool | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    analyzer: DetectorRef | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    computed_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        articles = self.articles.to_dict()

        evidence_groups = self.evidence_groups.to_dict()

        coverage_balance = self.coverage_balance.to_dict()

        state_affiliated_share = self.state_affiliated_share

        n_rated = self.n_rated

        corroborated = self.corroborated

        scope: str | Unset = UNSET
        if not isinstance(self.scope, Unset):
            scope = self.scope.value

        by_country: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_country, Unset):
            by_country = self.by_country.to_dict()

        by_outlet_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_outlet_type, Unset):
            by_outlet_type = self.by_outlet_type.to_dict()

        by_factuality: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_factuality, Unset):
            by_factuality = self.by_factuality.to_dict()

        blindspot: None | str | Unset
        if isinstance(self.blindspot, Unset):
            blindspot = UNSET
        elif (
            isinstance(self.blindspot, CoverageStatsBlindspotType1)
            or isinstance(self.blindspot, CoverageStatsBlindspotType2Type1)
            or isinstance(self.blindspot, CoverageStatsBlindspotType3Type1)
        ):
            blindspot = self.blindspot.value
        else:
            blindspot = self.blindspot

        corroborated_share: float | None | Unset
        if isinstance(self.corroborated_share, Unset):
            corroborated_share = UNSET
        else:
            corroborated_share = self.corroborated_share

        skewed = self.skewed

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        analyzer: dict[str, Any] | Unset = UNSET
        if not isinstance(self.analyzer, Unset):
            analyzer = self.analyzer.to_dict()

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        computed_at: str | Unset = UNSET
        if not isinstance(self.computed_at, Unset):
            computed_at = self.computed_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "articles": articles,
                "evidenceGroups": evidence_groups,
                "coverageBalance": coverage_balance,
                "stateAffiliatedShare": state_affiliated_share,
                "nRated": n_rated,
                "corroborated": corroborated,
            }
        )
        if scope is not UNSET:
            field_dict["scope"] = scope
        if by_country is not UNSET:
            field_dict["byCountry"] = by_country
        if by_outlet_type is not UNSET:
            field_dict["byOutletType"] = by_outlet_type
        if by_factuality is not UNSET:
            field_dict["byFactuality"] = by_factuality
        if blindspot is not UNSET:
            field_dict["blindspot"] = blindspot
        if corroborated_share is not UNSET:
            field_dict["corroboratedShare"] = corroborated_share
        if skewed is not UNSET:
            field_dict["skewed"] = skewed
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if analyzer is not UNSET:
            field_dict["analyzer"] = analyzer
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if computed_at is not UNSET:
            field_dict["computedAt"] = computed_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.coverage_stats_articles import CoverageStatsArticles
        from ..models.coverage_stats_by_country import CoverageStatsByCountry
        from ..models.coverage_stats_by_factuality import CoverageStatsByFactuality
        from ..models.coverage_stats_by_outlet_type import CoverageStatsByOutletType
        from ..models.coverage_stats_coverage_balance import (
            CoverageStatsCoverageBalance,
        )
        from ..models.coverage_stats_evidence_groups import CoverageStatsEvidenceGroups
        from ..models.detector_ref import DetectorRef

        d = dict(src_dict)
        articles = CoverageStatsArticles.from_dict(d.pop("articles"))

        evidence_groups = CoverageStatsEvidenceGroups.from_dict(d.pop("evidenceGroups"))

        coverage_balance = CoverageStatsCoverageBalance.from_dict(
            d.pop("coverageBalance")
        )

        state_affiliated_share = d.pop("stateAffiliatedShare")

        n_rated = d.pop("nRated")

        corroborated = d.pop("corroborated")

        _scope = d.pop("scope", UNSET)
        scope: CoverageStatsScope | Unset
        if isinstance(_scope, Unset):
            scope = UNSET
        else:
            scope = CoverageStatsScope(_scope)

        _by_country = d.pop("byCountry", UNSET)
        by_country: CoverageStatsByCountry | Unset
        if isinstance(_by_country, Unset):
            by_country = UNSET
        else:
            by_country = CoverageStatsByCountry.from_dict(_by_country)

        _by_outlet_type = d.pop("byOutletType", UNSET)
        by_outlet_type: CoverageStatsByOutletType | Unset
        if isinstance(_by_outlet_type, Unset):
            by_outlet_type = UNSET
        else:
            by_outlet_type = CoverageStatsByOutletType.from_dict(_by_outlet_type)

        _by_factuality = d.pop("byFactuality", UNSET)
        by_factuality: CoverageStatsByFactuality | Unset
        if isinstance(_by_factuality, Unset):
            by_factuality = UNSET
        else:
            by_factuality = CoverageStatsByFactuality.from_dict(_by_factuality)

        def _parse_blindspot(
            data: object,
        ) -> (
            CoverageStatsBlindspotType1
            | CoverageStatsBlindspotType2Type1
            | CoverageStatsBlindspotType3Type1
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
                blindspot_type_1 = CoverageStatsBlindspotType1(data)

                return blindspot_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                blindspot_type_2_type_1 = CoverageStatsBlindspotType2Type1(data)

                return blindspot_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                blindspot_type_3_type_1 = CoverageStatsBlindspotType3Type1(data)

                return blindspot_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                CoverageStatsBlindspotType1
                | CoverageStatsBlindspotType2Type1
                | CoverageStatsBlindspotType3Type1
                | None
                | Unset,
                data,
            )

        blindspot = _parse_blindspot(d.pop("blindspot", UNSET))

        def _parse_corroborated_share(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        corroborated_share = _parse_corroborated_share(
            d.pop("corroboratedShare", UNSET)
        )

        skewed = d.pop("skewed", UNSET)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        _analyzer = d.pop("analyzer", UNSET)
        analyzer: DetectorRef | Unset
        if isinstance(_analyzer, Unset):
            analyzer = UNSET
        else:
            analyzer = DetectorRef.from_dict(_analyzer)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        _computed_at = d.pop("computedAt", UNSET)
        computed_at: datetime.datetime | Unset
        if isinstance(_computed_at, Unset):
            computed_at = UNSET
        else:
            computed_at = datetime.datetime.fromisoformat(_computed_at)

        coverage_stats = cls(
            articles=articles,
            evidence_groups=evidence_groups,
            coverage_balance=coverage_balance,
            state_affiliated_share=state_affiliated_share,
            n_rated=n_rated,
            corroborated=corroborated,
            scope=scope,
            by_country=by_country,
            by_outlet_type=by_outlet_type,
            by_factuality=by_factuality,
            blindspot=blindspot,
            corroborated_share=corroborated_share,
            skewed=skewed,
            caveats=caveats,
            analyzer=analyzer,
            manifest_id=manifest_id,
            computed_at=computed_at,
        )

        coverage_stats.additional_properties = d
        return coverage_stats

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
