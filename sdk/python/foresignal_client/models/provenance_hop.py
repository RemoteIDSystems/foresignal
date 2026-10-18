from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ProvenanceHop")


@_attrs_define
class ProvenanceHop:
    """One instance in a federation provenance chain (113)

    Attributes:
        instance_id (str): Install id of the instance (003)
        source_id (str):
        external_id (str):
        seq (int):
        ingested_at (datetime.datetime):
    """

    instance_id: str
    source_id: str
    external_id: str
    seq: int
    ingested_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        source_id = self.source_id

        external_id = self.external_id

        seq = self.seq

        ingested_at = self.ingested_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "sourceId": source_id,
                "externalId": external_id,
                "seq": seq,
                "ingestedAt": ingested_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        seq = d.pop("seq")

        ingested_at = datetime.datetime.fromisoformat(d.pop("ingestedAt"))

        provenance_hop = cls(
            instance_id=instance_id,
            source_id=source_id,
            external_id=external_id,
            seq=seq,
            ingested_at=ingested_at,
        )

        provenance_hop.additional_properties = d
        return provenance_hop

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
