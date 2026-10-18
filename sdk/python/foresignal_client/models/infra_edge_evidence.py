from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_edge_evidence_matched import InfraEdgeEvidenceMatched


T = TypeVar("T", bound="InfraEdgeEvidence")


@_attrs_define
class InfraEdgeEvidence:
    """Why the edge exists

    Attributes:
        dataset (str | Unset):
        distance_km (float | None | Unset):
        matched (InfraEdgeEvidenceMatched | Unset):
        notes (None | str | Unset):
    """

    dataset: str | Unset = UNSET
    distance_km: float | None | Unset = UNSET
    matched: InfraEdgeEvidenceMatched | Unset = UNSET
    notes: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        dataset = self.dataset

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        matched: dict[str, Any] | Unset = UNSET
        if not isinstance(self.matched, Unset):
            matched = self.matched.to_dict()

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if dataset is not UNSET:
            field_dict["dataset"] = dataset
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if matched is not UNSET:
            field_dict["matched"] = matched
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_edge_evidence_matched import InfraEdgeEvidenceMatched

        d = dict(src_dict)
        dataset = d.pop("dataset", UNSET)

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        _matched = d.pop("matched", UNSET)
        matched: InfraEdgeEvidenceMatched | Unset
        if isinstance(_matched, Unset):
            matched = UNSET
        else:
            matched = InfraEdgeEvidenceMatched.from_dict(_matched)

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        infra_edge_evidence = cls(
            dataset=dataset,
            distance_km=distance_km,
            matched=matched,
            notes=notes,
        )

        infra_edge_evidence.additional_properties = d
        return infra_edge_evidence

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
