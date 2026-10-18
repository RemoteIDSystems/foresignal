from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_input_citations_item import ForecastInputCitationsItem
    from ..models.forecast_input_distribution_type_0 import (
        ForecastInputDistributionType0,
    )
    from ..models.quantiles import Quantiles


T = TypeVar("T", bound="ForecastInput")


@_attrs_define
class ForecastInput:
    """Submit a forecast; exactly one of probability, distribution or quantiles matching the question type

    Attributes:
        probability (float | None | Unset): Binary questions
        distribution (ForecastInputDistributionType0 | None | Unset): Multiple/ordered: outcome label → probability,
            summing to 1 ± 1e-6
        quantiles (None | Quantiles | Unset): Numeric: p05, p10, p25, p50, p75, p90, p95, monotone
        rationale (None | str | Unset):
        citations (list[ForecastInputCitationsItem] | Unset):
        forecaster_id (None | str | Unset): Agent scope only: another registered `operator` or `external` forecaster
    """

    probability: float | None | Unset = UNSET
    distribution: ForecastInputDistributionType0 | None | Unset = UNSET
    quantiles: None | Quantiles | Unset = UNSET
    rationale: None | str | Unset = UNSET
    citations: list[ForecastInputCitationsItem] | Unset = UNSET
    forecaster_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast_input_distribution_type_0 import (
            ForecastInputDistributionType0,
        )
        from ..models.quantiles import Quantiles

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        distribution: dict[str, Any] | None | Unset
        if isinstance(self.distribution, Unset):
            distribution = UNSET
        elif isinstance(self.distribution, ForecastInputDistributionType0):
            distribution = self.distribution.to_dict()
        else:
            distribution = self.distribution

        quantiles: dict[str, Any] | None | Unset
        if isinstance(self.quantiles, Unset):
            quantiles = UNSET
        elif isinstance(self.quantiles, Quantiles):
            quantiles = self.quantiles.to_dict()
        else:
            quantiles = self.quantiles

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        citations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = []
            for citations_item_data in self.citations:
                citations_item = citations_item_data.to_dict()
                citations.append(citations_item)

        forecaster_id: None | str | Unset
        if isinstance(self.forecaster_id, Unset):
            forecaster_id = UNSET
        else:
            forecaster_id = self.forecaster_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if probability is not UNSET:
            field_dict["probability"] = probability
        if distribution is not UNSET:
            field_dict["distribution"] = distribution
        if quantiles is not UNSET:
            field_dict["quantiles"] = quantiles
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if citations is not UNSET:
            field_dict["citations"] = citations
        if forecaster_id is not UNSET:
            field_dict["forecasterId"] = forecaster_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_input_citations_item import ForecastInputCitationsItem
        from ..models.forecast_input_distribution_type_0 import (
            ForecastInputDistributionType0,
        )
        from ..models.quantiles import Quantiles

        d = dict(src_dict)

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        def _parse_distribution(
            data: object,
        ) -> ForecastInputDistributionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                distribution_type_0 = ForecastInputDistributionType0.from_dict(data)

                return distribution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastInputDistributionType0 | None | Unset, data)

        distribution = _parse_distribution(d.pop("distribution", UNSET))

        def _parse_quantiles(data: object) -> None | Quantiles | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quantiles_type_0 = Quantiles.from_dict(data)

                return quantiles_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Quantiles | Unset, data)

        quantiles = _parse_quantiles(d.pop("quantiles", UNSET))

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        _citations = d.pop("citations", UNSET)
        citations: list[ForecastInputCitationsItem] | Unset = UNSET
        if _citations is not UNSET:
            citations = []
            for citations_item_data in _citations:
                citations_item = ForecastInputCitationsItem.from_dict(
                    citations_item_data
                )

                citations.append(citations_item)

        def _parse_forecaster_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        forecaster_id = _parse_forecaster_id(d.pop("forecasterId", UNSET))

        forecast_input = cls(
            probability=probability,
            distribution=distribution,
            quantiles=quantiles,
            rationale=rationale,
            citations=citations,
            forecaster_id=forecaster_id,
        )

        forecast_input.additional_properties = d
        return forecast_input

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
