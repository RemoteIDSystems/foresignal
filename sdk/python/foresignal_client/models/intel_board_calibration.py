from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_board_calibration_ensemble_type_0 import (
        IntelBoardCalibrationEnsembleType0,
    )
    from ..models.intel_board_calibration_operator_type_0 import (
        IntelBoardCalibrationOperatorType0,
    )


T = TypeVar("T", bound="IntelBoardCalibration")


@_attrs_define
class IntelBoardCalibration:
    """Brier skill and calibration over the last 90 days, operator vs machine

    Attributes:
        window (str | Unset):  Default: 'P90D'.
        ensemble (IntelBoardCalibrationEnsembleType0 | None | Unset):
        operator (IntelBoardCalibrationOperatorType0 | None | Unset):
    """

    window: str | Unset = "P90D"
    ensemble: IntelBoardCalibrationEnsembleType0 | None | Unset = UNSET
    operator: IntelBoardCalibrationOperatorType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.intel_board_calibration_ensemble_type_0 import (
            IntelBoardCalibrationEnsembleType0,
        )
        from ..models.intel_board_calibration_operator_type_0 import (
            IntelBoardCalibrationOperatorType0,
        )

        window = self.window

        ensemble: dict[str, Any] | None | Unset
        if isinstance(self.ensemble, Unset):
            ensemble = UNSET
        elif isinstance(self.ensemble, IntelBoardCalibrationEnsembleType0):
            ensemble = self.ensemble.to_dict()
        else:
            ensemble = self.ensemble

        operator: dict[str, Any] | None | Unset
        if isinstance(self.operator, Unset):
            operator = UNSET
        elif isinstance(self.operator, IntelBoardCalibrationOperatorType0):
            operator = self.operator.to_dict()
        else:
            operator = self.operator

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if window is not UNSET:
            field_dict["window"] = window
        if ensemble is not UNSET:
            field_dict["ensemble"] = ensemble
        if operator is not UNSET:
            field_dict["operator"] = operator

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_board_calibration_ensemble_type_0 import (
            IntelBoardCalibrationEnsembleType0,
        )
        from ..models.intel_board_calibration_operator_type_0 import (
            IntelBoardCalibrationOperatorType0,
        )

        d = dict(src_dict)
        window = d.pop("window", UNSET)

        def _parse_ensemble(
            data: object,
        ) -> IntelBoardCalibrationEnsembleType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                ensemble_type_0 = IntelBoardCalibrationEnsembleType0.from_dict(data)

                return ensemble_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IntelBoardCalibrationEnsembleType0 | None | Unset, data)

        ensemble = _parse_ensemble(d.pop("ensemble", UNSET))

        def _parse_operator(
            data: object,
        ) -> IntelBoardCalibrationOperatorType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                operator_type_0 = IntelBoardCalibrationOperatorType0.from_dict(data)

                return operator_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IntelBoardCalibrationOperatorType0 | None | Unset, data)

        operator = _parse_operator(d.pop("operator", UNSET))

        intel_board_calibration = cls(
            window=window,
            ensemble=ensemble,
            operator=operator,
        )

        intel_board_calibration.additional_properties = d
        return intel_board_calibration

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
