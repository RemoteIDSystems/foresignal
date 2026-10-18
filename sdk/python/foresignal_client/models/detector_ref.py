from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref_params import DetectorRefParams


T = TypeVar("T", bound="DetectorRef")


@_attrs_define
class DetectorRef:
    """
    Attributes:
        name (str):
        version (str):
        params (DetectorRefParams | Unset):
    """

    name: str
    version: str
    params: DetectorRefParams | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        version = self.version

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "version": version,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref_params import DetectorRefParams

        d = dict(src_dict)
        name = d.pop("name")

        version = d.pop("version")

        _params = d.pop("params", UNSET)
        params: DetectorRefParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = DetectorRefParams.from_dict(_params)

        detector_ref = cls(
            name=name,
            version=version,
            params=params,
        )

        detector_ref.additional_properties = d
        return detector_ref

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
