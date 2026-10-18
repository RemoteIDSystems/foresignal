from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelBoardCalibrationOperatorType0")


@_attrs_define
class IntelBoardCalibrationOperatorType0:
    """Operator (≥ 20 resolved forecasts)

    Attributes:
        n (int):
        brier_skill (float | None | Unset):
        calibration (None | str | Unset):
    """

    n: int
    brier_skill: float | None | Unset = UNSET
    calibration: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        n = self.n

        brier_skill: float | None | Unset
        if isinstance(self.brier_skill, Unset):
            brier_skill = UNSET
        else:
            brier_skill = self.brier_skill

        calibration: None | str | Unset
        if isinstance(self.calibration, Unset):
            calibration = UNSET
        else:
            calibration = self.calibration

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "n": n,
            }
        )
        if brier_skill is not UNSET:
            field_dict["brierSkill"] = brier_skill
        if calibration is not UNSET:
            field_dict["calibration"] = calibration

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        n = d.pop("n")

        def _parse_brier_skill(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        brier_skill = _parse_brier_skill(d.pop("brierSkill", UNSET))

        def _parse_calibration(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        calibration = _parse_calibration(d.pop("calibration", UNSET))

        intel_board_calibration_operator_type_0 = cls(
            n=n,
            brier_skill=brier_skill,
            calibration=calibration,
        )

        intel_board_calibration_operator_type_0.additional_properties = d
        return intel_board_calibration_operator_type_0

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
