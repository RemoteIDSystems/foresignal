from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_candidate_input_kind import PrecursorCandidateInputKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precursor_side import PrecursorSide
    from ..models.precursor_spatial import PrecursorSpatial


T = TypeVar("T", bound="PrecursorCandidateInput")


@_attrs_define
class PrecursorCandidateInput:
    """Register a candidate pair (event selectors or series refs, spatial rule, lag bounds)

    Attributes:
        kind (PrecursorCandidateInputKind):
        antecedent (PrecursorSide): Antecedent or consequent of a precursor: an event selector or a series
        consequent (PrecursorSide): Antecedent or consequent of a precursor: an event selector or a series
        lag_max (str): ISO-8601 duration Example: P3D.
        name (None | str | Unset):
        spatial (PrecursorSpatial | Unset): Spatial rule linking antecedent and consequent occurrences (103 forms)
        lag_min (str | Unset): ISO-8601 duration Example: P3D.
        region_codes (list[str] | Unset):
    """

    kind: PrecursorCandidateInputKind
    antecedent: PrecursorSide
    consequent: PrecursorSide
    lag_max: str
    name: None | str | Unset = UNSET
    spatial: PrecursorSpatial | Unset = UNSET
    lag_min: str | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        antecedent = self.antecedent.to_dict()

        consequent = self.consequent.to_dict()

        lag_max = self.lag_max

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        spatial: dict[str, Any] | Unset = UNSET
        if not isinstance(self.spatial, Unset):
            spatial = self.spatial.to_dict()

        lag_min = self.lag_min

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
                "lagMax": lag_max,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if spatial is not UNSET:
            field_dict["spatial"] = spatial
        if lag_min is not UNSET:
            field_dict["lagMin"] = lag_min
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_side import PrecursorSide
        from ..models.precursor_spatial import PrecursorSpatial

        d = dict(src_dict)
        kind = PrecursorCandidateInputKind(d.pop("kind"))

        antecedent = PrecursorSide.from_dict(d.pop("antecedent"))

        consequent = PrecursorSide.from_dict(d.pop("consequent"))

        lag_max = d.pop("lagMax")

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

        lag_min = d.pop("lagMin", UNSET)

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        precursor_candidate_input = cls(
            kind=kind,
            antecedent=antecedent,
            consequent=consequent,
            lag_max=lag_max,
            name=name,
            spatial=spatial,
            lag_min=lag_min,
            region_codes=region_codes,
        )

        precursor_candidate_input.additional_properties = d
        return precursor_candidate_input

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
