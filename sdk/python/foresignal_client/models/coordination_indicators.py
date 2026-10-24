from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.coordination_indicator import CoordinationIndicator
    from ..models.detector_ref import DetectorRef
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="CoordinationIndicators")


@_attrs_define
class CoordinationIndicators:
    """Coordinated-amplification indicators of a narrative (`CoordinationDetector` 1.1.0): statistics, never labels on
    accounts

        Attributes:
            narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            window (TimeRange):
            synchrony (CoordinationIndicator): One coordination statistic against its null model
            text_duplication (CoordinationIndicator): One coordination statistic against its null model
            account_age_skew (CoordinationIndicator): One coordination statistic against its null model
            co_repost_bursts (CoordinationIndicator): One coordination statistic against its null model
            significant_count (int): Indicators with q < 0.01
            detector (DetectorRef):
            computed_at (datetime.datetime):
            finding_id (None | str | Unset): `social-coordination` finding when ≥ 2 indicators are significant
            statement (None | str | Unset):  Example: coordinated-amplification indicators present.
    """

    narrative_id: str
    window: TimeRange
    synchrony: CoordinationIndicator
    text_duplication: CoordinationIndicator
    account_age_skew: CoordinationIndicator
    co_repost_bursts: CoordinationIndicator
    significant_count: int
    detector: DetectorRef
    computed_at: datetime.datetime
    finding_id: None | str | Unset = UNSET
    statement: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        narrative_id = self.narrative_id

        window = self.window.to_dict()

        synchrony = self.synchrony.to_dict()

        text_duplication = self.text_duplication.to_dict()

        account_age_skew = self.account_age_skew.to_dict()

        co_repost_bursts = self.co_repost_bursts.to_dict()

        significant_count = self.significant_count

        detector = self.detector.to_dict()

        computed_at = self.computed_at.isoformat()

        finding_id: None | str | Unset
        if isinstance(self.finding_id, Unset):
            finding_id = UNSET
        else:
            finding_id = self.finding_id

        statement: None | str | Unset
        if isinstance(self.statement, Unset):
            statement = UNSET
        else:
            statement = self.statement

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "narrativeId": narrative_id,
                "window": window,
                "synchrony": synchrony,
                "textDuplication": text_duplication,
                "accountAgeSkew": account_age_skew,
                "coRepostBursts": co_repost_bursts,
                "significantCount": significant_count,
                "detector": detector,
                "computedAt": computed_at,
            }
        )
        if finding_id is not UNSET:
            field_dict["findingId"] = finding_id
        if statement is not UNSET:
            field_dict["statement"] = statement

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coordination_indicator import CoordinationIndicator
        from ..models.detector_ref import DetectorRef
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        narrative_id = d.pop("narrativeId")

        window = TimeRange.from_dict(d.pop("window"))

        synchrony = CoordinationIndicator.from_dict(d.pop("synchrony"))

        text_duplication = CoordinationIndicator.from_dict(d.pop("textDuplication"))

        account_age_skew = CoordinationIndicator.from_dict(d.pop("accountAgeSkew"))

        co_repost_bursts = CoordinationIndicator.from_dict(d.pop("coRepostBursts"))

        significant_count = d.pop("significantCount")

        detector = DetectorRef.from_dict(d.pop("detector"))

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_finding_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        finding_id = _parse_finding_id(d.pop("findingId", UNSET))

        def _parse_statement(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        statement = _parse_statement(d.pop("statement", UNSET))

        coordination_indicators = cls(
            narrative_id=narrative_id,
            window=window,
            synchrony=synchrony,
            text_duplication=text_duplication,
            account_age_skew=account_age_skew,
            co_repost_bursts=co_repost_bursts,
            significant_count=significant_count,
            detector=detector,
            computed_at=computed_at,
            finding_id=finding_id,
            statement=statement,
        )

        coordination_indicators.additional_properties = d
        return coordination_indicators

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
