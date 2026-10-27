from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.egress_health_sources_item import EgressHealthSourcesItem


T = TypeVar("T", bound="EgressHealth")


@_attrs_define
class EgressHealth:
    """Reachability and exit identity of an egress route (C1/181)

    Attributes:
        reachable (bool):
        exit_ip (None | str | Unset): Public IP the route presents
        latency_ms (float | None | Unset):
        last_checked_at (datetime.datetime | None | Unset):
        consecutive_failures (int | Unset):  Default: 0.
        backoff_until (datetime.datetime | None | Unset): Skipped until this time after failures
        last_error (None | str | Unset): The last failure carrying a request through the route (R2-018)
        sources (list[EgressHealthSourcesItem] | Unset): Only on getEgressRouteHealth: the sources held off this route
            by a 429 or Retry-After received through it (R2-018)
    """

    reachable: bool
    exit_ip: None | str | Unset = UNSET
    latency_ms: float | None | Unset = UNSET
    last_checked_at: datetime.datetime | None | Unset = UNSET
    consecutive_failures: int | Unset = 0
    backoff_until: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    sources: list[EgressHealthSourcesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        reachable = self.reachable

        exit_ip: None | str | Unset
        if isinstance(self.exit_ip, Unset):
            exit_ip = UNSET
        else:
            exit_ip = self.exit_ip

        latency_ms: float | None | Unset
        if isinstance(self.latency_ms, Unset):
            latency_ms = UNSET
        else:
            latency_ms = self.latency_ms

        last_checked_at: None | str | Unset
        if isinstance(self.last_checked_at, Unset):
            last_checked_at = UNSET
        elif isinstance(self.last_checked_at, datetime.datetime):
            last_checked_at = self.last_checked_at.isoformat()
        else:
            last_checked_at = self.last_checked_at

        consecutive_failures = self.consecutive_failures

        backoff_until: None | str | Unset
        if isinstance(self.backoff_until, Unset):
            backoff_until = UNSET
        elif isinstance(self.backoff_until, datetime.datetime):
            backoff_until = self.backoff_until.isoformat()
        else:
            backoff_until = self.backoff_until

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        sources: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = []
            for sources_item_data in self.sources:
                sources_item = sources_item_data.to_dict()
                sources.append(sources_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reachable": reachable,
            }
        )
        if exit_ip is not UNSET:
            field_dict["exitIp"] = exit_ip
        if latency_ms is not UNSET:
            field_dict["latencyMs"] = latency_ms
        if last_checked_at is not UNSET:
            field_dict["lastCheckedAt"] = last_checked_at
        if consecutive_failures is not UNSET:
            field_dict["consecutiveFailures"] = consecutive_failures
        if backoff_until is not UNSET:
            field_dict["backoffUntil"] = backoff_until
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if sources is not UNSET:
            field_dict["sources"] = sources

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.egress_health_sources_item import EgressHealthSourcesItem

        d = dict(src_dict)
        reachable = d.pop("reachable")

        def _parse_exit_ip(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        exit_ip = _parse_exit_ip(d.pop("exitIp", UNSET))

        def _parse_latency_ms(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        latency_ms = _parse_latency_ms(d.pop("latencyMs", UNSET))

        def _parse_last_checked_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_checked_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_checked_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_checked_at = _parse_last_checked_at(d.pop("lastCheckedAt", UNSET))

        consecutive_failures = d.pop("consecutiveFailures", UNSET)

        def _parse_backoff_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                backoff_until_type_0 = datetime.datetime.fromisoformat(data)

                return backoff_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        backoff_until = _parse_backoff_until(d.pop("backoffUntil", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        _sources = d.pop("sources", UNSET)
        sources: list[EgressHealthSourcesItem] | Unset = UNSET
        if _sources is not UNSET:
            sources = []
            for sources_item_data in _sources:
                sources_item = EgressHealthSourcesItem.from_dict(sources_item_data)

                sources.append(sources_item)

        egress_health = cls(
            reachable=reachable,
            exit_ip=exit_ip,
            latency_ms=latency_ms,
            last_checked_at=last_checked_at,
            consecutive_failures=consecutive_failures,
            backoff_until=backoff_until,
            last_error=last_error,
            sources=sources,
        )

        egress_health.additional_properties = d
        return egress_health

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
