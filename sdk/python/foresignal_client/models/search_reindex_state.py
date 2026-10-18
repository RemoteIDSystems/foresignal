from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.search_reindex_state_state import SearchReindexStateState
from ..types import UNSET, Unset

T = TypeVar("T", bound="SearchReindexState")


@_attrs_define
class SearchReindexState:
    """Progress of a zero-downtime embedding reindex (027)

    Attributes:
        state (SearchReindexStateState):
        from_model (None | str | Unset): Outgoing model id `<providerId>:<model>:<dimensions>`
        to_model (None | str | Unset):
        total (int | None | Unset):
        done (int | None | Unset):
        percent (float | None | Unset):
        rate_per_minute (float | None | Unset): 10-minute moving average
        eta_seconds (int | None | Unset):
        started_at (datetime.datetime | None | Unset):
    """

    state: SearchReindexStateState
    from_model: None | str | Unset = UNSET
    to_model: None | str | Unset = UNSET
    total: int | None | Unset = UNSET
    done: int | None | Unset = UNSET
    percent: float | None | Unset = UNSET
    rate_per_minute: float | None | Unset = UNSET
    eta_seconds: int | None | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        state = self.state.value

        from_model: None | str | Unset
        if isinstance(self.from_model, Unset):
            from_model = UNSET
        else:
            from_model = self.from_model

        to_model: None | str | Unset
        if isinstance(self.to_model, Unset):
            to_model = UNSET
        else:
            to_model = self.to_model

        total: int | None | Unset
        if isinstance(self.total, Unset):
            total = UNSET
        else:
            total = self.total

        done: int | None | Unset
        if isinstance(self.done, Unset):
            done = UNSET
        else:
            done = self.done

        percent: float | None | Unset
        if isinstance(self.percent, Unset):
            percent = UNSET
        else:
            percent = self.percent

        rate_per_minute: float | None | Unset
        if isinstance(self.rate_per_minute, Unset):
            rate_per_minute = UNSET
        else:
            rate_per_minute = self.rate_per_minute

        eta_seconds: int | None | Unset
        if isinstance(self.eta_seconds, Unset):
            eta_seconds = UNSET
        else:
            eta_seconds = self.eta_seconds

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "state": state,
            }
        )
        if from_model is not UNSET:
            field_dict["fromModel"] = from_model
        if to_model is not UNSET:
            field_dict["toModel"] = to_model
        if total is not UNSET:
            field_dict["total"] = total
        if done is not UNSET:
            field_dict["done"] = done
        if percent is not UNSET:
            field_dict["percent"] = percent
        if rate_per_minute is not UNSET:
            field_dict["ratePerMinute"] = rate_per_minute
        if eta_seconds is not UNSET:
            field_dict["etaSeconds"] = eta_seconds
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        state = SearchReindexStateState(d.pop("state"))

        def _parse_from_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        from_model = _parse_from_model(d.pop("fromModel", UNSET))

        def _parse_to_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        to_model = _parse_to_model(d.pop("toModel", UNSET))

        def _parse_total(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        total = _parse_total(d.pop("total", UNSET))

        def _parse_done(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        done = _parse_done(d.pop("done", UNSET))

        def _parse_percent(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        percent = _parse_percent(d.pop("percent", UNSET))

        def _parse_rate_per_minute(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        rate_per_minute = _parse_rate_per_minute(d.pop("ratePerMinute", UNSET))

        def _parse_eta_seconds(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        eta_seconds = _parse_eta_seconds(d.pop("etaSeconds", UNSET))

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

        search_reindex_state = cls(
            state=state,
            from_model=from_model,
            to_model=to_model,
            total=total,
            done=done,
            percent=percent,
            rate_per_minute=rate_per_minute,
            eta_seconds=eta_seconds,
            started_at=started_at,
        )

        search_reindex_state.additional_properties = d
        return search_reindex_state

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
