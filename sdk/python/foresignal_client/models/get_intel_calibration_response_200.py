from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.intel_calibration import IntelCalibration


T = TypeVar("T", bound="GetIntelCalibrationResponse200")


@_attrs_define
class GetIntelCalibrationResponse200:
    """
    Attributes:
        scopes (list[IntelCalibration]):
    """

    scopes: list[IntelCalibration]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        scopes = []
        for scopes_item_data in self.scopes:
            scopes_item = scopes_item_data.to_dict()
            scopes.append(scopes_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "scopes": scopes,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_calibration import IntelCalibration

        d = dict(src_dict)
        scopes = []
        _scopes = d.pop("scopes")
        for scopes_item_data in _scopes:
            scopes_item = IntelCalibration.from_dict(scopes_item_data)

            scopes.append(scopes_item)

        get_intel_calibration_response_200 = cls(
            scopes=scopes,
        )

        get_intel_calibration_response_200.additional_properties = d
        return get_intel_calibration_response_200

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
