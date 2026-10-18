from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.federation_ingest_result_dropped import FederationIngestResultDropped


T = TypeVar("T", bound="FederationIngestResult")


@_attrs_define
class FederationIngestResult:
    """Outcome of a federation ingest or bundle (113)

    Attributes:
        accepted (int):
        duplicates (int): Already seen (origin_ref, origin_seq)
        dropped (FederationIngestResultDropped): Items dropped by reason
    """

    accepted: int
    duplicates: int
    dropped: FederationIngestResultDropped
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        accepted = self.accepted

        duplicates = self.duplicates

        dropped = self.dropped.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "accepted": accepted,
                "duplicates": duplicates,
                "dropped": dropped,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.federation_ingest_result_dropped import (
            FederationIngestResultDropped,
        )

        d = dict(src_dict)
        accepted = d.pop("accepted")

        duplicates = d.pop("duplicates")

        dropped = FederationIngestResultDropped.from_dict(d.pop("dropped"))

        federation_ingest_result = cls(
            accepted=accepted,
            duplicates=duplicates,
            dropped=dropped,
        )

        federation_ingest_result.additional_properties = d
        return federation_ingest_result

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
