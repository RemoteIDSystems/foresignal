from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="AnomalyObservationEvidenceSamplesItem")


@_attrs_define
class AnomalyObservationEvidenceSamplesItem:
    """Sample record version

    Attributes:
        source (str):
        external_id (str):
        seq (int):
    """

    source: str
    external_id: str
    seq: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source

        external_id = self.external_id

        seq = self.seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
                "externalId": external_id,
                "seq": seq,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source = d.pop("source")

        external_id = d.pop("externalId")

        seq = d.pop("seq")

        anomaly_observation_evidence_samples_item = cls(
            source=source,
            external_id=external_id,
            seq=seq,
        )

        anomaly_observation_evidence_samples_item.additional_properties = d
        return anomaly_observation_evidence_samples_item

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
