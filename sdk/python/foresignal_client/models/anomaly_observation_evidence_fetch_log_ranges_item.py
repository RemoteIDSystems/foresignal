from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="AnomalyObservationEvidenceFetchLogRangesItem")


@_attrs_define
class AnomalyObservationEvidenceFetchLogRangesItem:
    """fetch_log id range per source

    Attributes:
        source_id (str):
        from_id (int):
        to_id (int):
    """

    source_id: str
    from_id: int
    to_id: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        from_id = self.from_id

        to_id = self.to_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "fromId": from_id,
                "toId": to_id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        from_id = d.pop("fromId")

        to_id = d.pop("toId")

        anomaly_observation_evidence_fetch_log_ranges_item = cls(
            source_id=source_id,
            from_id=from_id,
            to_id=to_id,
        )

        anomaly_observation_evidence_fetch_log_ranges_item.additional_properties = d
        return anomaly_observation_evidence_fetch_log_ranges_item

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
