from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.index_fit_result import IndexFitResult
    from ..models.regional_index_def import RegionalIndexDef
    from ..models.regional_index_value import RegionalIndexValue


T = TypeVar("T", bound="RegionalIndexDetail")


@_attrs_define
class RegionalIndexDetail:
    """An index definition with values for a region and time window

    Attributes:
        index (RegionalIndexDef): A transparent, decomposed regional index with fitted (proposed) weights
        values (list[RegionalIndexValue]):
        region (None | str | Unset):
        from_ (datetime.date | None | Unset):
        to (datetime.date | None | Unset):
        latest_fit (IndexFitResult | None | Unset):
    """

    index: RegionalIndexDef
    values: list[RegionalIndexValue]
    region: None | str | Unset = UNSET
    from_: datetime.date | None | Unset = UNSET
    to: datetime.date | None | Unset = UNSET
    latest_fit: IndexFitResult | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.index_fit_result import IndexFitResult

        index = self.index.to_dict()

        values = []
        for values_item_data in self.values:
            values_item = values_item_data.to_dict()
            values.append(values_item)

        region: None | str | Unset
        if isinstance(self.region, Unset):
            region = UNSET
        else:
            region = self.region

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.date):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.date):
            to = self.to.isoformat()
        else:
            to = self.to

        latest_fit: dict[str, Any] | None | Unset
        if isinstance(self.latest_fit, Unset):
            latest_fit = UNSET
        elif isinstance(self.latest_fit, IndexFitResult):
            latest_fit = self.latest_fit.to_dict()
        else:
            latest_fit = self.latest_fit

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "index": index,
                "values": values,
            }
        )
        if region is not UNSET:
            field_dict["region"] = region
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if latest_fit is not UNSET:
            field_dict["latestFit"] = latest_fit

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.index_fit_result import IndexFitResult
        from ..models.regional_index_def import RegionalIndexDef
        from ..models.regional_index_value import RegionalIndexValue

        d = dict(src_dict)
        index = RegionalIndexDef.from_dict(d.pop("index"))

        values = []
        _values = d.pop("values")
        for values_item_data in _values:
            values_item = RegionalIndexValue.from_dict(values_item_data)

            values.append(values_item)

        def _parse_region(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region = _parse_region(d.pop("region", UNSET))

        def _parse_from_(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.date.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.date.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        def _parse_latest_fit(data: object) -> IndexFitResult | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                latest_fit_type_0 = IndexFitResult.from_dict(data)

                return latest_fit_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndexFitResult | None | Unset, data)

        latest_fit = _parse_latest_fit(d.pop("latestFit", UNSET))

        regional_index_detail = cls(
            index=index,
            values=values,
            region=region,
            from_=from_,
            to=to,
            latest_fit=latest_fit,
        )

        regional_index_detail.additional_properties = d
        return regional_index_detail

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
