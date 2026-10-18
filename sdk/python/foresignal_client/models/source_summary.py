from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.health_status import HealthStatus
from ..models.history_tier import HistoryTier
from ..models.source_summary_mode import SourceSummaryMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.budget import Budget
    from ..models.source_access import SourceAccess


T = TypeVar("T", bound="SourceSummary")


@_attrs_define
class SourceSummary:
    """
    Attributes:
        id (str):
        name (str):
        kinds (list[str]):
        mode (SourceSummaryMode): Collection mode; `derived` sources are fed by ChangeBus or frame events and never
            polled
        enabled (bool):
        status (HealthStatus):
        custom (bool | Unset):
        credentials_required (bool | Unset): True if any credential field is required
        credentials_set (bool | Unset):
        interval (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        budget (Budget | Unset):
        budget_used_today (int | Unset):
        last_success_at (datetime.datetime | None | Unset):
        history_tier (HistoryTier | Unset): History tier: STANDARD (per-source windows, chunked deletes) or TRACK (one
            `trackWindow`, partition drops)
        licence (None | str | Unset):
        terms_url (None | str | Unset):
        last_data_at (datetime.datetime | None | Unset): Last fetch or flush that returned records (never a 304)
        access (SourceAccess | Unset): How a source reaches its data and what enabling it involves
    """

    id: str
    name: str
    kinds: list[str]
    mode: SourceSummaryMode
    enabled: bool
    status: HealthStatus
    custom: bool | Unset = UNSET
    credentials_required: bool | Unset = UNSET
    credentials_set: bool | Unset = UNSET
    interval: str | Unset = UNSET
    budget: Budget | Unset = UNSET
    budget_used_today: int | Unset = UNSET
    last_success_at: datetime.datetime | None | Unset = UNSET
    history_tier: HistoryTier | Unset = UNSET
    licence: None | str | Unset = UNSET
    terms_url: None | str | Unset = UNSET
    last_data_at: datetime.datetime | None | Unset = UNSET
    access: SourceAccess | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        kinds = self.kinds

        mode = self.mode.value

        enabled = self.enabled

        status = self.status.value

        custom = self.custom

        credentials_required = self.credentials_required

        credentials_set = self.credentials_set

        interval = self.interval

        budget: dict[str, Any] | Unset = UNSET
        if not isinstance(self.budget, Unset):
            budget = self.budget.to_dict()

        budget_used_today = self.budget_used_today

        last_success_at: None | str | Unset
        if isinstance(self.last_success_at, Unset):
            last_success_at = UNSET
        elif isinstance(self.last_success_at, datetime.datetime):
            last_success_at = self.last_success_at.isoformat()
        else:
            last_success_at = self.last_success_at

        history_tier: str | Unset = UNSET
        if not isinstance(self.history_tier, Unset):
            history_tier = self.history_tier.value

        licence: None | str | Unset
        if isinstance(self.licence, Unset):
            licence = UNSET
        else:
            licence = self.licence

        terms_url: None | str | Unset
        if isinstance(self.terms_url, Unset):
            terms_url = UNSET
        else:
            terms_url = self.terms_url

        last_data_at: None | str | Unset
        if isinstance(self.last_data_at, Unset):
            last_data_at = UNSET
        elif isinstance(self.last_data_at, datetime.datetime):
            last_data_at = self.last_data_at.isoformat()
        else:
            last_data_at = self.last_data_at

        access: dict[str, Any] | Unset = UNSET
        if not isinstance(self.access, Unset):
            access = self.access.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "kinds": kinds,
                "mode": mode,
                "enabled": enabled,
                "status": status,
            }
        )
        if custom is not UNSET:
            field_dict["custom"] = custom
        if credentials_required is not UNSET:
            field_dict["credentialsRequired"] = credentials_required
        if credentials_set is not UNSET:
            field_dict["credentialsSet"] = credentials_set
        if interval is not UNSET:
            field_dict["interval"] = interval
        if budget is not UNSET:
            field_dict["budget"] = budget
        if budget_used_today is not UNSET:
            field_dict["budgetUsedToday"] = budget_used_today
        if last_success_at is not UNSET:
            field_dict["lastSuccessAt"] = last_success_at
        if history_tier is not UNSET:
            field_dict["historyTier"] = history_tier
        if licence is not UNSET:
            field_dict["licence"] = licence
        if terms_url is not UNSET:
            field_dict["termsUrl"] = terms_url
        if last_data_at is not UNSET:
            field_dict["lastDataAt"] = last_data_at
        if access is not UNSET:
            field_dict["access"] = access

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.budget import Budget
        from ..models.source_access import SourceAccess

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        kinds = cast(list[str], d.pop("kinds"))

        mode = SourceSummaryMode(d.pop("mode"))

        enabled = d.pop("enabled")

        status = HealthStatus(d.pop("status"))

        custom = d.pop("custom", UNSET)

        credentials_required = d.pop("credentialsRequired", UNSET)

        credentials_set = d.pop("credentialsSet", UNSET)

        interval = d.pop("interval", UNSET)

        _budget = d.pop("budget", UNSET)
        budget: Budget | Unset
        if isinstance(_budget, Unset):
            budget = UNSET
        else:
            budget = Budget.from_dict(_budget)

        budget_used_today = d.pop("budgetUsedToday", UNSET)

        def _parse_last_success_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_success_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_success_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_success_at = _parse_last_success_at(d.pop("lastSuccessAt", UNSET))

        _history_tier = d.pop("historyTier", UNSET)
        history_tier: HistoryTier | Unset
        if isinstance(_history_tier, Unset):
            history_tier = UNSET
        else:
            history_tier = HistoryTier(_history_tier)

        def _parse_licence(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence = _parse_licence(d.pop("licence", UNSET))

        def _parse_terms_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        terms_url = _parse_terms_url(d.pop("termsUrl", UNSET))

        def _parse_last_data_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_data_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_data_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_data_at = _parse_last_data_at(d.pop("lastDataAt", UNSET))

        _access = d.pop("access", UNSET)
        access: SourceAccess | Unset
        if isinstance(_access, Unset):
            access = UNSET
        else:
            access = SourceAccess.from_dict(_access)

        source_summary = cls(
            id=id,
            name=name,
            kinds=kinds,
            mode=mode,
            enabled=enabled,
            status=status,
            custom=custom,
            credentials_required=credentials_required,
            credentials_set=credentials_set,
            interval=interval,
            budget=budget,
            budget_used_today=budget_used_today,
            last_success_at=last_success_at,
            history_tier=history_tier,
            licence=licence,
            terms_url=terms_url,
            last_data_at=last_data_at,
            access=access,
        )

        source_summary.additional_properties = d
        return source_summary

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
