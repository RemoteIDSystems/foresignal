from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.claim_place_precision import ClaimPlacePrecision
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ClaimPlace")


@_attrs_define
class ClaimPlace:
    """Where a claim says something happened (reported, never observed)

    Attributes:
        name (str):
        precision (ClaimPlacePrecision):
        confidence (float): By ambiguity and population
        ref (None | str | Unset): 036 place or 040 region; null for unresolved names
        geom (Geometry | None | Unset): Null when unresolved (LLM places not in the gazetteer)
        region_code (None | str | Unset): 040 region code of the resolved place (country or admin-1), or null (136)
    """

    name: str
    precision: ClaimPlacePrecision
    confidence: float
    ref: None | str | Unset = UNSET
    geom: Geometry | None | Unset = UNSET
    region_code: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        name = self.name

        precision = self.precision.value

        confidence = self.confidence

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        geom: dict[str, Any] | None | Unset
        if isinstance(self.geom, Unset):
            geom = UNSET
        elif isinstance(self.geom, Geometry):
            geom = self.geom.to_dict()
        else:
            geom = self.geom

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "precision": precision,
                "confidence": confidence,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if geom is not UNSET:
            field_dict["geom"] = geom
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        name = d.pop("name")

        precision = ClaimPlacePrecision(d.pop("precision"))

        confidence = d.pop("confidence")

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_geom(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geom_type_0 = Geometry.from_dict(data)

                return geom_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geom = _parse_geom(d.pop("geom", UNSET))

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        claim_place = cls(
            name=name,
            precision=precision,
            confidence=confidence,
            ref=ref,
            geom=geom,
            region_code=region_code,
        )

        claim_place.additional_properties = d
        return claim_place

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
