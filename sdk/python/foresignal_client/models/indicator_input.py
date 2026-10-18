from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_input_credibility import IndicatorInputCredibility
from ..models.indicator_input_relevance import IndicatorInputRelevance
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.indicator_input_absence_lr import IndicatorInputAbsenceLr
    from ..models.indicator_input_lr import IndicatorInputLr
    from ..models.indicator_signal import IndicatorSignal


T = TypeVar("T", bound="IndicatorInput")


@_attrs_define
class IndicatorInput:
    """A new indicator

    Attributes:
        description (str):
        signal (IndicatorSignal): What the indicator watches; `type` selects the populated member
        signal_family (str):
        credibility (IndicatorInputCredibility):
        relevance (IndicatorInputRelevance):
        lr (IndicatorInputLr): Per hypothesis id (or label when created with its set)
        expected_by (datetime.datetime | None | Unset):
        absence_lr (IndicatorInputAbsenceLr | Unset): Optional absence LR overrides per hypothesis id
    """

    description: str
    signal: IndicatorSignal
    signal_family: str
    credibility: IndicatorInputCredibility
    relevance: IndicatorInputRelevance
    lr: IndicatorInputLr
    expected_by: datetime.datetime | None | Unset = UNSET
    absence_lr: IndicatorInputAbsenceLr | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        description = self.description

        signal = self.signal.to_dict()

        signal_family = self.signal_family

        credibility = self.credibility.value

        relevance = self.relevance.value

        lr = self.lr.to_dict()

        expected_by: None | str | Unset
        if isinstance(self.expected_by, Unset):
            expected_by = UNSET
        elif isinstance(self.expected_by, datetime.datetime):
            expected_by = self.expected_by.isoformat()
        else:
            expected_by = self.expected_by

        absence_lr: dict[str, Any] | Unset = UNSET
        if not isinstance(self.absence_lr, Unset):
            absence_lr = self.absence_lr.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "description": description,
                "signal": signal,
                "signalFamily": signal_family,
                "credibility": credibility,
                "relevance": relevance,
                "lr": lr,
            }
        )
        if expected_by is not UNSET:
            field_dict["expectedBy"] = expected_by
        if absence_lr is not UNSET:
            field_dict["absenceLr"] = absence_lr

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.indicator_input_absence_lr import IndicatorInputAbsenceLr
        from ..models.indicator_input_lr import IndicatorInputLr
        from ..models.indicator_signal import IndicatorSignal

        d = dict(src_dict)
        description = d.pop("description")

        signal = IndicatorSignal.from_dict(d.pop("signal"))

        signal_family = d.pop("signalFamily")

        credibility = IndicatorInputCredibility(d.pop("credibility"))

        relevance = IndicatorInputRelevance(d.pop("relevance"))

        lr = IndicatorInputLr.from_dict(d.pop("lr"))

        def _parse_expected_by(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expected_by_type_0 = datetime.datetime.fromisoformat(data)

                return expected_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expected_by = _parse_expected_by(d.pop("expectedBy", UNSET))

        _absence_lr = d.pop("absenceLr", UNSET)
        absence_lr: IndicatorInputAbsenceLr | Unset
        if isinstance(_absence_lr, Unset):
            absence_lr = UNSET
        else:
            absence_lr = IndicatorInputAbsenceLr.from_dict(_absence_lr)

        indicator_input = cls(
            description=description,
            signal=signal,
            signal_family=signal_family,
            credibility=credibility,
            relevance=relevance,
            lr=lr,
            expected_by=expected_by,
            absence_lr=absence_lr,
        )

        indicator_input.additional_properties = d
        return indicator_input

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
