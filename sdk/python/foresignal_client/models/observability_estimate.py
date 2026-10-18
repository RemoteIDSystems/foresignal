from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.confidence_level import ConfidenceLevel
from ..models.observability_estimate_method import ObservabilityEstimateMethod
from ..types import UNSET, Unset

T = TypeVar("T", bound="ObservabilityEstimate")


@_attrs_define
class ObservabilityEstimate:
    """Time-versioned detection probability of one source for a family, region or cell and band (116)

    Attributes:
        source_id (str):
        family (str):
        period_start (datetime.datetime):
        period_end (datetime.datetime):
        p_detect (float):
        lo (float):
        hi (float):
        n (int):
        method (ObservabilityEstimateMethod):
        computed_at (datetime.datetime):
        region_code (None | str | Unset):
        cell (None | str | Unset): H3 res-4 cell index
        band (None | str | Unset): Threshold bucket, e.g. M4.5-5
        confidence (ConfidenceLevel | Unset): Analytic confidence in the evidence (separate from probability)
        manifest_id (None | str | Unset):
    """

    source_id: str
    family: str
    period_start: datetime.datetime
    period_end: datetime.datetime
    p_detect: float
    lo: float
    hi: float
    n: int
    method: ObservabilityEstimateMethod
    computed_at: datetime.datetime
    region_code: None | str | Unset = UNSET
    cell: None | str | Unset = UNSET
    band: None | str | Unset = UNSET
    confidence: ConfidenceLevel | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        family = self.family

        period_start = self.period_start.isoformat()

        period_end = self.period_end.isoformat()

        p_detect = self.p_detect

        lo = self.lo

        hi = self.hi

        n = self.n

        method = self.method.value

        computed_at = self.computed_at.isoformat()

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        cell: None | str | Unset
        if isinstance(self.cell, Unset):
            cell = UNSET
        else:
            cell = self.cell

        band: None | str | Unset
        if isinstance(self.band, Unset):
            band = UNSET
        else:
            band = self.band

        confidence: str | Unset = UNSET
        if not isinstance(self.confidence, Unset):
            confidence = self.confidence.value

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "family": family,
                "periodStart": period_start,
                "periodEnd": period_end,
                "pDetect": p_detect,
                "lo": lo,
                "hi": hi,
                "n": n,
                "method": method,
                "computedAt": computed_at,
            }
        )
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code
        if cell is not UNSET:
            field_dict["cell"] = cell
        if band is not UNSET:
            field_dict["band"] = band
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        family = d.pop("family")

        period_start = datetime.datetime.fromisoformat(d.pop("periodStart"))

        period_end = datetime.datetime.fromisoformat(d.pop("periodEnd"))

        p_detect = d.pop("pDetect")

        lo = d.pop("lo")

        hi = d.pop("hi")

        n = d.pop("n")

        method = ObservabilityEstimateMethod(d.pop("method"))

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        def _parse_cell(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cell = _parse_cell(d.pop("cell", UNSET))

        def _parse_band(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        band = _parse_band(d.pop("band", UNSET))

        _confidence = d.pop("confidence", UNSET)
        confidence: ConfidenceLevel | Unset
        if isinstance(_confidence, Unset):
            confidence = UNSET
        else:
            confidence = ConfidenceLevel(_confidence)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        observability_estimate = cls(
            source_id=source_id,
            family=family,
            period_start=period_start,
            period_end=period_end,
            p_detect=p_detect,
            lo=lo,
            hi=hi,
            n=n,
            method=method,
            computed_at=computed_at,
            region_code=region_code,
            cell=cell,
            band=band,
            confidence=confidence,
            manifest_id=manifest_id,
        )

        observability_estimate.additional_properties = d
        return observability_estimate

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
