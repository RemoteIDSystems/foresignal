from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.confidence_level import ConfidenceLevel
from ..models.detectability_methods_item import DetectabilityMethodsItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.observability_estimate import ObservabilityEstimate


T = TypeVar("T", bound="Detectability")


@_attrs_define
class Detectability:
    """Combined detection probability for a family at a place and time: 1 − Π(1 − p) across independence groups, max within
    a group, using estimates valid at `at` (116)

        Attributes:
            family (str):
            at (datetime.datetime):
            p (float):
            lo (float):
            hi (float):
            methods (list[DetectabilityMethodsItem]):
            sources (list[str]):
            absence_ok (bool): Healthy sources and p ≥ absenceMinDetect: an absence here may be read as 'nothing happened'
            region_code (None | str | Unset):
            cell (None | str | Unset):
            band (None | str | Unset):
            confidence (ConfidenceLevel | Unset): Analytic confidence in the evidence (separate from probability)
            reason (None | str | Unset): When absenceOk is false, e.g. LOW_DETECTABILITY or a coverage reason
            estimates (list[ObservabilityEstimate] | Unset): Per-source estimates (include=estimates)
    """

    family: str
    at: datetime.datetime
    p: float
    lo: float
    hi: float
    methods: list[DetectabilityMethodsItem]
    sources: list[str]
    absence_ok: bool
    region_code: None | str | Unset = UNSET
    cell: None | str | Unset = UNSET
    band: None | str | Unset = UNSET
    confidence: ConfidenceLevel | Unset = UNSET
    reason: None | str | Unset = UNSET
    estimates: list[ObservabilityEstimate] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        family = self.family

        at = self.at.isoformat()

        p = self.p

        lo = self.lo

        hi = self.hi

        methods = []
        for methods_item_data in self.methods:
            methods_item = methods_item_data.value
            methods.append(methods_item)

        sources = self.sources

        absence_ok = self.absence_ok

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

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        estimates: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.estimates, Unset):
            estimates = []
            for estimates_item_data in self.estimates:
                estimates_item = estimates_item_data.to_dict()
                estimates.append(estimates_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "family": family,
                "at": at,
                "p": p,
                "lo": lo,
                "hi": hi,
                "methods": methods,
                "sources": sources,
                "absenceOk": absence_ok,
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
        if reason is not UNSET:
            field_dict["reason"] = reason
        if estimates is not UNSET:
            field_dict["estimates"] = estimates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.observability_estimate import ObservabilityEstimate

        d = dict(src_dict)
        family = d.pop("family")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        p = d.pop("p")

        lo = d.pop("lo")

        hi = d.pop("hi")

        methods = []
        _methods = d.pop("methods")
        for methods_item_data in _methods:
            methods_item = DetectabilityMethodsItem(methods_item_data)

            methods.append(methods_item)

        sources = cast(list[str], d.pop("sources"))

        absence_ok = d.pop("absenceOk")

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

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        _estimates = d.pop("estimates", UNSET)
        estimates: list[ObservabilityEstimate] | Unset = UNSET
        if _estimates is not UNSET:
            estimates = []
            for estimates_item_data in _estimates:
                estimates_item = ObservabilityEstimate.from_dict(estimates_item_data)

                estimates.append(estimates_item)

        detectability = cls(
            family=family,
            at=at,
            p=p,
            lo=lo,
            hi=hi,
            methods=methods,
            sources=sources,
            absence_ok=absence_ok,
            region_code=region_code,
            cell=cell,
            band=band,
            confidence=confidence,
            reason=reason,
            estimates=estimates,
        )

        detectability.additional_properties = d
        return detectability

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
