from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exposure_population_status import ExposurePopulationStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution


T = TypeVar("T", bound="ExposurePopulation")


@_attrs_define
class ExposurePopulation:
    """Optional GHSL population estimate (pack)

    Attributes:
        status (ExposurePopulationStatus):
        attribution (list[Attribution] | Unset):
        estimate (float | None | Unset):
        epoch (int | None | Unset):
        method (None | str | Unset):  Example: GHSL→H3 r7 centroid-in-polygon.
        note (None | str | Unset):  Example: modelled residential population; not a casualty or impact estimate.
    """

    status: ExposurePopulationStatus
    attribution: list[Attribution] | Unset = UNSET
    estimate: float | None | Unset = UNSET
    epoch: int | None | Unset = UNSET
    method: None | str | Unset = UNSET
    note: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        estimate: float | None | Unset
        if isinstance(self.estimate, Unset):
            estimate = UNSET
        else:
            estimate = self.estimate

        epoch: int | None | Unset
        if isinstance(self.epoch, Unset):
            epoch = UNSET
        else:
            epoch = self.epoch

        method: None | str | Unset
        if isinstance(self.method, Unset):
            method = UNSET
        else:
            method = self.method

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if estimate is not UNSET:
            field_dict["estimate"] = estimate
        if epoch is not UNSET:
            field_dict["epoch"] = epoch
        if method is not UNSET:
            field_dict["method"] = method
        if note is not UNSET:
            field_dict["note"] = note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution

        d = dict(src_dict)
        status = ExposurePopulationStatus(d.pop("status"))

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        def _parse_estimate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        estimate = _parse_estimate(d.pop("estimate", UNSET))

        def _parse_epoch(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        epoch = _parse_epoch(d.pop("epoch", UNSET))

        def _parse_method(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        method = _parse_method(d.pop("method", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        exposure_population = cls(
            status=status,
            attribution=attribution,
            estimate=estimate,
            epoch=epoch,
            method=method,
            note=note,
        )

        exposure_population.additional_properties = d
        return exposure_population

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
