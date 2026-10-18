from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_candidate_kind import PrecursorCandidateKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precursor_side import PrecursorSide
    from ..models.precursor_spatial import PrecursorSpatial


T = TypeVar("T", bound="PrecursorCandidate")


@_attrs_define
class PrecursorCandidate:
    """An operator-registered candidate pair, always included in mining

    Attributes:
        kind (PrecursorCandidateKind):
        antecedent (PrecursorSide): Antecedent or consequent of a precursor: an event selector or a series
        consequent (PrecursorSide): Antecedent or consequent of a precursor: an event selector or a series
        lag_min (str): ISO-8601 duration Example: P3D.
        lag_max (str): ISO-8601 duration Example: P3D.
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        created_by (str):
        created_at (datetime.datetime):
        name (None | str | Unset):
        spatial (PrecursorSpatial | Unset): Spatial rule linking antecedent and consequent occurrences (103 forms)
        region_codes (list[str] | Unset):
    """

    kind: PrecursorCandidateKind
    antecedent: PrecursorSide
    consequent: PrecursorSide
    lag_min: str
    lag_max: str
    id: str
    created_by: str
    created_at: datetime.datetime
    name: None | str | Unset = UNSET
    spatial: PrecursorSpatial | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        antecedent = self.antecedent.to_dict()

        consequent = self.consequent.to_dict()

        lag_min = self.lag_min

        lag_max = self.lag_max

        id = self.id

        created_by = self.created_by

        created_at = self.created_at.isoformat()

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        spatial: dict[str, Any] | Unset = UNSET
        if not isinstance(self.spatial, Unset):
            spatial = self.spatial.to_dict()

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "antecedent": antecedent,
                "consequent": consequent,
                "lagMin": lag_min,
                "lagMax": lag_max,
                "id": id,
                "createdBy": created_by,
                "createdAt": created_at,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if spatial is not UNSET:
            field_dict["spatial"] = spatial
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_side import PrecursorSide
        from ..models.precursor_spatial import PrecursorSpatial

        d = dict(src_dict)
        kind = PrecursorCandidateKind(d.pop("kind"))

        antecedent = PrecursorSide.from_dict(d.pop("antecedent"))

        consequent = PrecursorSide.from_dict(d.pop("consequent"))

        lag_min = d.pop("lagMin")

        lag_max = d.pop("lagMax")

        id = d.pop("id")

        created_by = d.pop("createdBy")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        _spatial = d.pop("spatial", UNSET)
        spatial: PrecursorSpatial | Unset
        if isinstance(_spatial, Unset):
            spatial = UNSET
        else:
            spatial = PrecursorSpatial.from_dict(_spatial)

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        precursor_candidate = cls(
            kind=kind,
            antecedent=antecedent,
            consequent=consequent,
            lag_min=lag_min,
            lag_max=lag_max,
            id=id,
            created_by=created_by,
            created_at=created_at,
            name=name,
            spatial=spatial,
            region_codes=region_codes,
        )

        precursor_candidate.additional_properties = d
        return precursor_candidate

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
