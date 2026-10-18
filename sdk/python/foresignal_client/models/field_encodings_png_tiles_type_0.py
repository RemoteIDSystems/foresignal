from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.field_encodings_png_tiles_type_0_tms import FieldEncodingsPngTilesType0Tms
from ..types import UNSET, Unset

T = TypeVar("T", bound="FieldEncodingsPngTilesType0")


@_attrs_define
class FieldEncodingsPngTilesType0:
    """108: tiled png-uv fields

    Attributes:
        template (str | Unset): …/fields/{frameKey}/{z}/{x}/{y}.png
        tms (FieldEncodingsPngTilesType0Tms | Unset):
        u_min (float | Unset):
        u_max (float | Unset):
        v_min (float | Unset):
        v_max (float | Unset):
        minzoom (int | Unset):
        maxzoom (int | Unset):
    """

    template: str | Unset = UNSET
    tms: FieldEncodingsPngTilesType0Tms | Unset = UNSET
    u_min: float | Unset = UNSET
    u_max: float | Unset = UNSET
    v_min: float | Unset = UNSET
    v_max: float | Unset = UNSET
    minzoom: int | Unset = UNSET
    maxzoom: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        template = self.template

        tms: str | Unset = UNSET
        if not isinstance(self.tms, Unset):
            tms = self.tms.value

        u_min = self.u_min

        u_max = self.u_max

        v_min = self.v_min

        v_max = self.v_max

        minzoom = self.minzoom

        maxzoom = self.maxzoom

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if template is not UNSET:
            field_dict["template"] = template
        if tms is not UNSET:
            field_dict["tms"] = tms
        if u_min is not UNSET:
            field_dict["uMin"] = u_min
        if u_max is not UNSET:
            field_dict["uMax"] = u_max
        if v_min is not UNSET:
            field_dict["vMin"] = v_min
        if v_max is not UNSET:
            field_dict["vMax"] = v_max
        if minzoom is not UNSET:
            field_dict["minzoom"] = minzoom
        if maxzoom is not UNSET:
            field_dict["maxzoom"] = maxzoom

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        template = d.pop("template", UNSET)

        _tms = d.pop("tms", UNSET)
        tms: FieldEncodingsPngTilesType0Tms | Unset
        if isinstance(_tms, Unset):
            tms = UNSET
        else:
            tms = FieldEncodingsPngTilesType0Tms(_tms)

        u_min = d.pop("uMin", UNSET)

        u_max = d.pop("uMax", UNSET)

        v_min = d.pop("vMin", UNSET)

        v_max = d.pop("vMax", UNSET)

        minzoom = d.pop("minzoom", UNSET)

        maxzoom = d.pop("maxzoom", UNSET)

        field_encodings_png_tiles_type_0 = cls(
            template=template,
            tms=tms,
            u_min=u_min,
            u_max=u_max,
            v_min=v_min,
            v_max=v_max,
            minzoom=minzoom,
            maxzoom=maxzoom,
        )

        field_encodings_png_tiles_type_0.additional_properties = d
        return field_encodings_png_tiles_type_0

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
