from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.scorecard_entry import ScorecardEntry


T = TypeVar("T", bound="ForecastScorecard")


@_attrs_define
class ForecastScorecard:
    """Scorecard: per forecaster and category, computed by `ForecastScorer`

    Attributes:
        retrospective (bool): True for scorecards from retrospective evaluations
        entries (list[ScorecardEntry]):
        scorer_version (str):
        computed_at (datetime.datetime):
        forecaster_id (None | str | Unset):
        category (None | str | Unset):
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
    """

    retrospective: bool
    entries: list[ScorecardEntry]
    scorer_version: str
    computed_at: datetime.datetime
    forecaster_id: None | str | Unset = UNSET
    category: None | str | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        retrospective = self.retrospective

        entries = []
        for entries_item_data in self.entries:
            entries_item = entries_item_data.to_dict()
            entries.append(entries_item)

        scorer_version = self.scorer_version

        computed_at = self.computed_at.isoformat()

        forecaster_id: None | str | Unset
        if isinstance(self.forecaster_id, Unset):
            forecaster_id = UNSET
        else:
            forecaster_id = self.forecaster_id

        category: None | str | Unset
        if isinstance(self.category, Unset):
            category = UNSET
        else:
            category = self.category

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "retrospective": retrospective,
                "entries": entries,
                "scorerVersion": scorer_version,
                "computedAt": computed_at,
            }
        )
        if forecaster_id is not UNSET:
            field_dict["forecasterId"] = forecaster_id
        if category is not UNSET:
            field_dict["category"] = category
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.scorecard_entry import ScorecardEntry

        d = dict(src_dict)
        retrospective = d.pop("retrospective")

        entries = []
        _entries = d.pop("entries")
        for entries_item_data in _entries:
            entries_item = ScorecardEntry.from_dict(entries_item_data)

            entries.append(entries_item)

        scorer_version = d.pop("scorerVersion")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_forecaster_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        forecaster_id = _parse_forecaster_id(d.pop("forecasterId", UNSET))

        def _parse_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        category = _parse_category(d.pop("category", UNSET))

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        forecast_scorecard = cls(
            retrospective=retrospective,
            entries=entries,
            scorer_version=scorer_version,
            computed_at=computed_at,
            forecaster_id=forecaster_id,
            category=category,
            from_=from_,
            to=to,
        )

        forecast_scorecard.additional_properties = d
        return forecast_scorecard

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
