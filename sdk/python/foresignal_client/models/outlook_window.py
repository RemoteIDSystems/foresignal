from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.outlook_window_exceedance_type_1 import OutlookWindowExceedanceType1
from ..models.outlook_window_exceedance_type_2_type_1 import (
    OutlookWindowExceedanceType2Type1,
)
from ..models.outlook_window_exceedance_type_3_type_1 import (
    OutlookWindowExceedanceType3Type1,
)
from ..models.outlook_window_status import OutlookWindowStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.quantiles import Quantiles


T = TypeVar("T", bound="OutlookWindow")


@_attrs_define
class OutlookWindow:
    """One rolling 4-week window of a regional outlook

    Attributes:
        index (int):
        window_start (datetime.date):
        window_end (datetime.date):
        quantiles (Quantiles):
        status (OutlookWindowStatus):
        question_id (None | str | Unset):
        actual (int | None | Unset):
        within_p10p90 (bool | None | Unset):
        exceedance (None | OutlookWindowExceedanceType1 | OutlookWindowExceedanceType2Type1 |
            OutlookWindowExceedanceType3Type1 | Unset):
    """

    index: int
    window_start: datetime.date
    window_end: datetime.date
    quantiles: Quantiles
    status: OutlookWindowStatus
    question_id: None | str | Unset = UNSET
    actual: int | None | Unset = UNSET
    within_p10p90: bool | None | Unset = UNSET
    exceedance: (
        None
        | OutlookWindowExceedanceType1
        | OutlookWindowExceedanceType2Type1
        | OutlookWindowExceedanceType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index = self.index

        window_start = self.window_start.isoformat()

        window_end = self.window_end.isoformat()

        quantiles = self.quantiles.to_dict()

        status = self.status.value

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        actual: int | None | Unset
        if isinstance(self.actual, Unset):
            actual = UNSET
        else:
            actual = self.actual

        within_p10p90: bool | None | Unset
        if isinstance(self.within_p10p90, Unset):
            within_p10p90 = UNSET
        else:
            within_p10p90 = self.within_p10p90

        exceedance: None | str | Unset
        if isinstance(self.exceedance, Unset):
            exceedance = UNSET
        elif (
            isinstance(self.exceedance, OutlookWindowExceedanceType1)
            or isinstance(self.exceedance, OutlookWindowExceedanceType2Type1)
            or isinstance(self.exceedance, OutlookWindowExceedanceType3Type1)
        ):
            exceedance = self.exceedance.value
        else:
            exceedance = self.exceedance

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "index": index,
                "windowStart": window_start,
                "windowEnd": window_end,
                "quantiles": quantiles,
                "status": status,
            }
        )
        if question_id is not UNSET:
            field_dict["questionId"] = question_id
        if actual is not UNSET:
            field_dict["actual"] = actual
        if within_p10p90 is not UNSET:
            field_dict["withinP10P90"] = within_p10p90
        if exceedance is not UNSET:
            field_dict["exceedance"] = exceedance

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.quantiles import Quantiles

        d = dict(src_dict)
        index = d.pop("index")

        window_start = datetime.date.fromisoformat(d.pop("windowStart"))

        window_end = datetime.date.fromisoformat(d.pop("windowEnd"))

        quantiles = Quantiles.from_dict(d.pop("quantiles"))

        status = OutlookWindowStatus(d.pop("status"))

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        def _parse_actual(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        actual = _parse_actual(d.pop("actual", UNSET))

        def _parse_within_p10p90(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        within_p10p90 = _parse_within_p10p90(d.pop("withinP10P90", UNSET))

        def _parse_exceedance(
            data: object,
        ) -> (
            None
            | OutlookWindowExceedanceType1
            | OutlookWindowExceedanceType2Type1
            | OutlookWindowExceedanceType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                exceedance_type_1 = OutlookWindowExceedanceType1(data)

                return exceedance_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                exceedance_type_2_type_1 = OutlookWindowExceedanceType2Type1(data)

                return exceedance_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                exceedance_type_3_type_1 = OutlookWindowExceedanceType3Type1(data)

                return exceedance_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | OutlookWindowExceedanceType1
                | OutlookWindowExceedanceType2Type1
                | OutlookWindowExceedanceType3Type1
                | Unset,
                data,
            )

        exceedance = _parse_exceedance(d.pop("exceedance", UNSET))

        outlook_window = cls(
            index=index,
            window_start=window_start,
            window_end=window_end,
            quantiles=quantiles,
            status=status,
            question_id=question_id,
            actual=actual,
            within_p10p90=within_p10p90,
            exceedance=exceedance,
        )

        outlook_window.additional_properties = d
        return outlook_window

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
