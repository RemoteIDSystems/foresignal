from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exposure_watchlist_entities_status import ExposureWatchlistEntitiesStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.exposure_watchlist_entities_counts_item import (
        ExposureWatchlistEntitiesCountsItem,
    )


T = TypeVar("T", bound="ExposureWatchlistEntities")


@_attrs_define
class ExposureWatchlistEntities:
    """Current counts of watchlist entities inside (036/098)

    Attributes:
        status (ExposureWatchlistEntitiesStatus):
        attribution (list[Attribution] | Unset):
        counts (list[ExposureWatchlistEntitiesCountsItem] | Unset):
    """

    status: ExposureWatchlistEntitiesStatus
    attribution: list[Attribution] | Unset = UNSET
    counts: list[ExposureWatchlistEntitiesCountsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        counts: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.counts, Unset):
            counts = []
            for counts_item_data in self.counts:
                counts_item = counts_item_data.to_dict()
                counts.append(counts_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if counts is not UNSET:
            field_dict["counts"] = counts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.exposure_watchlist_entities_counts_item import (
            ExposureWatchlistEntitiesCountsItem,
        )

        d = dict(src_dict)
        status = ExposureWatchlistEntitiesStatus(d.pop("status"))

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        _counts = d.pop("counts", UNSET)
        counts: list[ExposureWatchlistEntitiesCountsItem] | Unset = UNSET
        if _counts is not UNSET:
            counts = []
            for counts_item_data in _counts:
                counts_item = ExposureWatchlistEntitiesCountsItem.from_dict(
                    counts_item_data
                )

                counts.append(counts_item)

        exposure_watchlist_entities = cls(
            status=status,
            attribution=attribution,
            counts=counts,
        )

        exposure_watchlist_entities.additional_properties = d
        return exposure_watchlist_entities

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
