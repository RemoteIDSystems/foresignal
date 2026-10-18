from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_status import SampleStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.stats_response_thresholds_item import StatsResponseThresholdsItem


T = TypeVar("T", bound="StatsResponse")


@_attrs_define
class StatsResponse:
    """Area statistics of one product frame

    Attributes:
        product_id (str):
        status (SampleStatus): ok; no-coverage: outside the grid/footprint or a no-coverage code ("outside product
            coverage; this is not evidence of clear weather"); no-data: inside coverage but nodata; stale: frame older than
            maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or frame expired
        thresholds (list[StatsResponseThresholdsItem]):
        attribution (Attribution): Structured attribution clients must render for a product or derived output (Weather-
            Imagery-Design §6.10)
        frame_key (None | str | Unset):
        valid_time (datetime.datetime | None | Unset):
        run (None | str | Unset):
        age_minutes (float | None | Unset):
        units (None | str | Unset):
        status_text (None | str | Unset):
        min_ (float | None | Unset):
        max_ (float | None | Unset):
        mean (float | None | Unset):
        p95 (float | None | Unset):
        coverage_pct (float | None | Unset):
        area_km_2 (float | Unset): Area of the query geometry (spherical cell areas)
    """

    product_id: str
    status: SampleStatus
    thresholds: list[StatsResponseThresholdsItem]
    attribution: Attribution
    frame_key: None | str | Unset = UNSET
    valid_time: datetime.datetime | None | Unset = UNSET
    run: None | str | Unset = UNSET
    age_minutes: float | None | Unset = UNSET
    units: None | str | Unset = UNSET
    status_text: None | str | Unset = UNSET
    min_: float | None | Unset = UNSET
    max_: float | None | Unset = UNSET
    mean: float | None | Unset = UNSET
    p95: float | None | Unset = UNSET
    coverage_pct: float | None | Unset = UNSET
    area_km_2: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        status = self.status.value

        thresholds = []
        for thresholds_item_data in self.thresholds:
            thresholds_item = thresholds_item_data.to_dict()
            thresholds.append(thresholds_item)

        attribution = self.attribution.to_dict()

        frame_key: None | str | Unset
        if isinstance(self.frame_key, Unset):
            frame_key = UNSET
        else:
            frame_key = self.frame_key

        valid_time: None | str | Unset
        if isinstance(self.valid_time, Unset):
            valid_time = UNSET
        elif isinstance(self.valid_time, datetime.datetime):
            valid_time = self.valid_time.isoformat()
        else:
            valid_time = self.valid_time

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        age_minutes: float | None | Unset
        if isinstance(self.age_minutes, Unset):
            age_minutes = UNSET
        else:
            age_minutes = self.age_minutes

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        status_text: None | str | Unset
        if isinstance(self.status_text, Unset):
            status_text = UNSET
        else:
            status_text = self.status_text

        min_: float | None | Unset
        if isinstance(self.min_, Unset):
            min_ = UNSET
        else:
            min_ = self.min_

        max_: float | None | Unset
        if isinstance(self.max_, Unset):
            max_ = UNSET
        else:
            max_ = self.max_

        mean: float | None | Unset
        if isinstance(self.mean, Unset):
            mean = UNSET
        else:
            mean = self.mean

        p95: float | None | Unset
        if isinstance(self.p95, Unset):
            p95 = UNSET
        else:
            p95 = self.p95

        coverage_pct: float | None | Unset
        if isinstance(self.coverage_pct, Unset):
            coverage_pct = UNSET
        else:
            coverage_pct = self.coverage_pct

        area_km_2 = self.area_km_2

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "status": status,
                "thresholds": thresholds,
                "attribution": attribution,
            }
        )
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if valid_time is not UNSET:
            field_dict["validTime"] = valid_time
        if run is not UNSET:
            field_dict["run"] = run
        if age_minutes is not UNSET:
            field_dict["ageMinutes"] = age_minutes
        if units is not UNSET:
            field_dict["units"] = units
        if status_text is not UNSET:
            field_dict["statusText"] = status_text
        if min_ is not UNSET:
            field_dict["min"] = min_
        if max_ is not UNSET:
            field_dict["max"] = max_
        if mean is not UNSET:
            field_dict["mean"] = mean
        if p95 is not UNSET:
            field_dict["p95"] = p95
        if coverage_pct is not UNSET:
            field_dict["coveragePct"] = coverage_pct
        if area_km_2 is not UNSET:
            field_dict["areaKm2"] = area_km_2

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.stats_response_thresholds_item import StatsResponseThresholdsItem

        d = dict(src_dict)
        product_id = d.pop("productId")

        status = SampleStatus(d.pop("status"))

        thresholds = []
        _thresholds = d.pop("thresholds")
        for thresholds_item_data in _thresholds:
            thresholds_item = StatsResponseThresholdsItem.from_dict(
                thresholds_item_data
            )

            thresholds.append(thresholds_item)

        attribution = Attribution.from_dict(d.pop("attribution"))

        def _parse_frame_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_key = _parse_frame_key(d.pop("frameKey", UNSET))

        def _parse_valid_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_time_type_0 = datetime.datetime.fromisoformat(data)

                return valid_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_time = _parse_valid_time(d.pop("validTime", UNSET))

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        def _parse_age_minutes(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        age_minutes = _parse_age_minutes(d.pop("ageMinutes", UNSET))

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        def _parse_status_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_text = _parse_status_text(d.pop("statusText", UNSET))

        def _parse_min_(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        min_ = _parse_min_(d.pop("min", UNSET))

        def _parse_max_(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        max_ = _parse_max_(d.pop("max", UNSET))

        def _parse_mean(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean = _parse_mean(d.pop("mean", UNSET))

        def _parse_p95(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p95 = _parse_p95(d.pop("p95", UNSET))

        def _parse_coverage_pct(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        coverage_pct = _parse_coverage_pct(d.pop("coveragePct", UNSET))

        area_km_2 = d.pop("areaKm2", UNSET)

        stats_response = cls(
            product_id=product_id,
            status=status,
            thresholds=thresholds,
            attribution=attribution,
            frame_key=frame_key,
            valid_time=valid_time,
            run=run,
            age_minutes=age_minutes,
            units=units,
            status_text=status_text,
            min_=min_,
            max_=max_,
            mean=mean,
            p95=p95,
            coverage_pct=coverage_pct,
            area_km_2=area_km_2,
        )

        stats_response.additional_properties = d
        return stats_response

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
