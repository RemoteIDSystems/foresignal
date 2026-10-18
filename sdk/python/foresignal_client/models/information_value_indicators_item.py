from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.information_value_indicators_item_state import (
    InformationValueIndicatorsItemState,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="InformationValueIndicatorsItem")


@_attrs_define
class InformationValueIndicatorsItem:
    """One pending indicator

    Attributes:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        rank (int):
        eig (float):
        potential_eig (float): EIG with p_detect = 1
        description (str | Unset):
        signal_family (str | Unset):
        state (InformationValueIndicatorsItemState | Unset):
        p_detect (float | None | Unset):
        p_observed (float | None | Unset):
        collection_gap (bool | Unset):
    """

    indicator_id: str
    rank: int
    eig: float
    potential_eig: float
    description: str | Unset = UNSET
    signal_family: str | Unset = UNSET
    state: InformationValueIndicatorsItemState | Unset = UNSET
    p_detect: float | None | Unset = UNSET
    p_observed: float | None | Unset = UNSET
    collection_gap: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        indicator_id = self.indicator_id

        rank = self.rank

        eig = self.eig

        potential_eig = self.potential_eig

        description = self.description

        signal_family = self.signal_family

        state: str | Unset = UNSET
        if not isinstance(self.state, Unset):
            state = self.state.value

        p_detect: float | None | Unset
        if isinstance(self.p_detect, Unset):
            p_detect = UNSET
        else:
            p_detect = self.p_detect

        p_observed: float | None | Unset
        if isinstance(self.p_observed, Unset):
            p_observed = UNSET
        else:
            p_observed = self.p_observed

        collection_gap = self.collection_gap

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "indicatorId": indicator_id,
                "rank": rank,
                "eig": eig,
                "potentialEig": potential_eig,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if state is not UNSET:
            field_dict["state"] = state
        if p_detect is not UNSET:
            field_dict["pDetect"] = p_detect
        if p_observed is not UNSET:
            field_dict["pObserved"] = p_observed
        if collection_gap is not UNSET:
            field_dict["collectionGap"] = collection_gap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        indicator_id = d.pop("indicatorId")

        rank = d.pop("rank")

        eig = d.pop("eig")

        potential_eig = d.pop("potentialEig")

        description = d.pop("description", UNSET)

        signal_family = d.pop("signalFamily", UNSET)

        _state = d.pop("state", UNSET)
        state: InformationValueIndicatorsItemState | Unset
        if isinstance(_state, Unset):
            state = UNSET
        else:
            state = InformationValueIndicatorsItemState(_state)

        def _parse_p_detect(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_detect = _parse_p_detect(d.pop("pDetect", UNSET))

        def _parse_p_observed(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_observed = _parse_p_observed(d.pop("pObserved", UNSET))

        collection_gap = d.pop("collectionGap", UNSET)

        information_value_indicators_item = cls(
            indicator_id=indicator_id,
            rank=rank,
            eig=eig,
            potential_eig=potential_eig,
            description=description,
            signal_family=signal_family,
            state=state,
            p_detect=p_detect,
            p_observed=p_observed,
            collection_gap=collection_gap,
        )

        information_value_indicators_item.additional_properties = d
        return information_value_indicators_item

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
