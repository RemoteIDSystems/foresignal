from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.anomaly_observation_evidence_fetch_log_ranges_item import (
        AnomalyObservationEvidenceFetchLogRangesItem,
    )
    from ..models.anomaly_observation_evidence_samples_item import (
        AnomalyObservationEvidenceSamplesItem,
    )


T = TypeVar("T", bound="AnomalyObservationEvidence")


@_attrs_define
class AnomalyObservationEvidence:
    """Sample evidence refs and fetch_log ranges

    Attributes:
        samples (list[AnomalyObservationEvidenceSamplesItem] | Unset):
        fetch_log_ranges (list[AnomalyObservationEvidenceFetchLogRangesItem] | Unset):
    """

    samples: list[AnomalyObservationEvidenceSamplesItem] | Unset = UNSET
    fetch_log_ranges: list[AnomalyObservationEvidenceFetchLogRangesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        samples: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.samples, Unset):
            samples = []
            for samples_item_data in self.samples:
                samples_item = samples_item_data.to_dict()
                samples.append(samples_item)

        fetch_log_ranges: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.fetch_log_ranges, Unset):
            fetch_log_ranges = []
            for fetch_log_ranges_item_data in self.fetch_log_ranges:
                fetch_log_ranges_item = fetch_log_ranges_item_data.to_dict()
                fetch_log_ranges.append(fetch_log_ranges_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if samples is not UNSET:
            field_dict["samples"] = samples
        if fetch_log_ranges is not UNSET:
            field_dict["fetchLogRanges"] = fetch_log_ranges

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.anomaly_observation_evidence_fetch_log_ranges_item import (
            AnomalyObservationEvidenceFetchLogRangesItem,
        )
        from ..models.anomaly_observation_evidence_samples_item import (
            AnomalyObservationEvidenceSamplesItem,
        )

        d = dict(src_dict)
        _samples = d.pop("samples", UNSET)
        samples: list[AnomalyObservationEvidenceSamplesItem] | Unset = UNSET
        if _samples is not UNSET:
            samples = []
            for samples_item_data in _samples:
                samples_item = AnomalyObservationEvidenceSamplesItem.from_dict(
                    samples_item_data
                )

                samples.append(samples_item)

        _fetch_log_ranges = d.pop("fetchLogRanges", UNSET)
        fetch_log_ranges: list[AnomalyObservationEvidenceFetchLogRangesItem] | Unset = (
            UNSET
        )
        if _fetch_log_ranges is not UNSET:
            fetch_log_ranges = []
            for fetch_log_ranges_item_data in _fetch_log_ranges:
                fetch_log_ranges_item = (
                    AnomalyObservationEvidenceFetchLogRangesItem.from_dict(
                        fetch_log_ranges_item_data
                    )
                )

                fetch_log_ranges.append(fetch_log_ranges_item)

        anomaly_observation_evidence = cls(
            samples=samples,
            fetch_log_ranges=fetch_log_ranges,
        )

        anomaly_observation_evidence.additional_properties = d
        return anomaly_observation_evidence

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
