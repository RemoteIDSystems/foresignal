from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.media_match import MediaMatch
    from ..models.media_match_result_query import MediaMatchResultQuery


T = TypeVar("T", bound="MediaMatchResult")


@_attrs_define
class MediaMatchResult:
    """Matches with first-seen dates and narratives; Foresignal only knows media it has fingerprinted (REFERENCE_WINDOW
    caveat)

        Attributes:
            query (MediaMatchResultQuery): The query hash
            max_distance (int):
            matches (list[MediaMatch]):
            caveats (list[Caveat]):
            reference_window_start (datetime.datetime | None | Unset): Start of the fingerprint store
    """

    query: MediaMatchResultQuery
    max_distance: int
    matches: list[MediaMatch]
    caveats: list[Caveat]
    reference_window_start: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        query = self.query.to_dict()

        max_distance = self.max_distance

        matches = []
        for matches_item_data in self.matches:
            matches_item = matches_item_data.to_dict()
            matches.append(matches_item)

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        reference_window_start: None | str | Unset
        if isinstance(self.reference_window_start, Unset):
            reference_window_start = UNSET
        elif isinstance(self.reference_window_start, datetime.datetime):
            reference_window_start = self.reference_window_start.isoformat()
        else:
            reference_window_start = self.reference_window_start

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "query": query,
                "maxDistance": max_distance,
                "matches": matches,
                "caveats": caveats,
            }
        )
        if reference_window_start is not UNSET:
            field_dict["referenceWindowStart"] = reference_window_start

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.media_match import MediaMatch
        from ..models.media_match_result_query import MediaMatchResultQuery

        d = dict(src_dict)
        query = MediaMatchResultQuery.from_dict(d.pop("query"))

        max_distance = d.pop("maxDistance")

        matches = []
        _matches = d.pop("matches")
        for matches_item_data in _matches:
            matches_item = MediaMatch.from_dict(matches_item_data)

            matches.append(matches_item)

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        def _parse_reference_window_start(
            data: object,
        ) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reference_window_start_type_0 = datetime.datetime.fromisoformat(data)

                return reference_window_start_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        reference_window_start = _parse_reference_window_start(
            d.pop("referenceWindowStart", UNSET)
        )

        media_match_result = cls(
            query=query,
            max_distance=max_distance,
            matches=matches,
            caveats=caveats,
            reference_window_start=reference_window_start,
        )

        media_match_result.additional_properties = d
        return media_match_result

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
