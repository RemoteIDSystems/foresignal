from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_geometry import EdpGeometry


T = TypeVar("T", bound="EdpCoverage")


@_attrs_define
class EdpCoverage:
    """Declared footprint and detection probability (low-confidence observability)

    Attributes:
        area (EdpGeometry | None | Unset):
        family (None | str | Unset):
        p_detect (float | None | Unset):
    """

    area: EdpGeometry | None | Unset = UNSET
    family: None | str | Unset = UNSET
    p_detect: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_geometry import EdpGeometry

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, EdpGeometry):
            area = self.area.to_dict()
        else:
            area = self.area

        family: None | str | Unset
        if isinstance(self.family, Unset):
            family = UNSET
        else:
            family = self.family

        p_detect: float | None | Unset
        if isinstance(self.p_detect, Unset):
            p_detect = UNSET
        else:
            p_detect = self.p_detect

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if area is not UNSET:
            field_dict["area"] = area
        if family is not UNSET:
            field_dict["family"] = family
        if p_detect is not UNSET:
            field_dict["pDetect"] = p_detect

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_geometry import EdpGeometry

        d = dict(src_dict)

        def _parse_area(data: object) -> EdpGeometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = EdpGeometry.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpGeometry | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        def _parse_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        family = _parse_family(d.pop("family", UNSET))

        def _parse_p_detect(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_detect = _parse_p_detect(d.pop("pDetect", UNSET))

        edp_coverage = cls(
            area=area,
            family=family,
            p_detect=p_detect,
        )

        edp_coverage.additional_properties = d
        return edp_coverage

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
