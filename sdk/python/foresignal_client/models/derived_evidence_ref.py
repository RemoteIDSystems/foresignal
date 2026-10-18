from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="DerivedEvidenceRef")


@_attrs_define
class DerivedEvidenceRef:
    """A sample input observation behind a derived record

    Attributes:
        source_id (str):
        external_id (str):
        observed_at (datetime.datetime):
    """

    source_id: str
    external_id: str
    observed_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        external_id = self.external_id

        observed_at = self.observed_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "externalId": external_id,
                "observedAt": observed_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        derived_evidence_ref = cls(
            source_id=source_id,
            external_id=external_id,
            observed_at=observed_at,
        )

        derived_evidence_ref.additional_properties = d
        return derived_evidence_ref

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
