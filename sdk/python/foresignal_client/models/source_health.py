from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.health_status import HealthStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.budget import Budget
    from ..models.coverage import Coverage
    from ..models.doctor_report_summary import DoctorReportSummary
    from ..models.source_health_details import SourceHealthDetails
    from ..models.source_storage import SourceStorage


T = TypeVar("T", bound="SourceHealth")


@_attrs_define
class SourceHealth:
    """
    Attributes:
        source_id (str):
        status (HealthStatus):
        consecutive_failures (int):
        status_reason (None | str | Unset): null while healthy
        last_attempt_at (datetime.datetime | None | Unset):
        last_success_at (datetime.datetime | None | Unset):
        last_error_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
        last_http_status (int | None | Unset):
        last_records_seen (int | Unset):
        last_records_changed (int | Unset):
        error_rate_last_hour (float | Unset):
        backoff_until (datetime.datetime | None | Unset):
        budget (Budget | Unset):
        budget_used_today (int | Unset):
        budget_remaining_today (int | Unset):
        refused_last_hour (int | Unset):
        requests_last_hour (int | Unset):
        bytes_last_hour (int | Unset):
        coverage (Coverage | None | Unset):
        storage (SourceStorage | Unset): Storage cost of a source (Core-Data-Model §15), from source_usage
        last_data_at (datetime.datetime | None | Unset): Last OK fetch or stream flush with records (a 304 never sets
            it)
        budget_hours_remaining (float | None | Unset): Hours until today's budget is exhausted at the last hour's rate;
            null when that rate is 0
        newest_frame_at (datetime.datetime | None | Unset): Product sources: newest frame time (frame-age health rule)
        frame_age_seconds (int | None | Unset): Product sources: age of the newest frame
        details (SourceHealthDetails | Unset): Adapter-specific health details; omitted when there are none. aisstream
            (063): `compressionEnabled` (from its SubscriptionConfirmation), `perMessageDeflate` (whether the handshake
            negotiated it; present while connected) and `vessels` (vessels held in the merged state)
        last_doctor_report (DoctorReportSummary | None | Unset):
        spent_usd_this_month (float | None | Unset): Paid adapters: money spent this UTC calendar month (135), against
            `budget.monthlyCapUsd`
    """

    source_id: str
    status: HealthStatus
    consecutive_failures: int
    status_reason: None | str | Unset = UNSET
    last_attempt_at: datetime.datetime | None | Unset = UNSET
    last_success_at: datetime.datetime | None | Unset = UNSET
    last_error_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    last_http_status: int | None | Unset = UNSET
    last_records_seen: int | Unset = UNSET
    last_records_changed: int | Unset = UNSET
    error_rate_last_hour: float | Unset = UNSET
    backoff_until: datetime.datetime | None | Unset = UNSET
    budget: Budget | Unset = UNSET
    budget_used_today: int | Unset = UNSET
    budget_remaining_today: int | Unset = UNSET
    refused_last_hour: int | Unset = UNSET
    requests_last_hour: int | Unset = UNSET
    bytes_last_hour: int | Unset = UNSET
    coverage: Coverage | None | Unset = UNSET
    storage: SourceStorage | Unset = UNSET
    last_data_at: datetime.datetime | None | Unset = UNSET
    budget_hours_remaining: float | None | Unset = UNSET
    newest_frame_at: datetime.datetime | None | Unset = UNSET
    frame_age_seconds: int | None | Unset = UNSET
    details: SourceHealthDetails | Unset = UNSET
    last_doctor_report: DoctorReportSummary | None | Unset = UNSET
    spent_usd_this_month: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.coverage import Coverage
        from ..models.doctor_report_summary import DoctorReportSummary

        source_id = self.source_id

        status = self.status.value

        consecutive_failures = self.consecutive_failures

        status_reason: None | str | Unset
        if isinstance(self.status_reason, Unset):
            status_reason = UNSET
        else:
            status_reason = self.status_reason

        last_attempt_at: None | str | Unset
        if isinstance(self.last_attempt_at, Unset):
            last_attempt_at = UNSET
        elif isinstance(self.last_attempt_at, datetime.datetime):
            last_attempt_at = self.last_attempt_at.isoformat()
        else:
            last_attempt_at = self.last_attempt_at

        last_success_at: None | str | Unset
        if isinstance(self.last_success_at, Unset):
            last_success_at = UNSET
        elif isinstance(self.last_success_at, datetime.datetime):
            last_success_at = self.last_success_at.isoformat()
        else:
            last_success_at = self.last_success_at

        last_error_at: None | str | Unset
        if isinstance(self.last_error_at, Unset):
            last_error_at = UNSET
        elif isinstance(self.last_error_at, datetime.datetime):
            last_error_at = self.last_error_at.isoformat()
        else:
            last_error_at = self.last_error_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        last_http_status: int | None | Unset
        if isinstance(self.last_http_status, Unset):
            last_http_status = UNSET
        else:
            last_http_status = self.last_http_status

        last_records_seen = self.last_records_seen

        last_records_changed = self.last_records_changed

        error_rate_last_hour = self.error_rate_last_hour

        backoff_until: None | str | Unset
        if isinstance(self.backoff_until, Unset):
            backoff_until = UNSET
        elif isinstance(self.backoff_until, datetime.datetime):
            backoff_until = self.backoff_until.isoformat()
        else:
            backoff_until = self.backoff_until

        budget: dict[str, Any] | Unset = UNSET
        if not isinstance(self.budget, Unset):
            budget = self.budget.to_dict()

        budget_used_today = self.budget_used_today

        budget_remaining_today = self.budget_remaining_today

        refused_last_hour = self.refused_last_hour

        requests_last_hour = self.requests_last_hour

        bytes_last_hour = self.bytes_last_hour

        coverage: dict[str, Any] | None | Unset
        if isinstance(self.coverage, Unset):
            coverage = UNSET
        elif isinstance(self.coverage, Coverage):
            coverage = self.coverage.to_dict()
        else:
            coverage = self.coverage

        storage: dict[str, Any] | Unset = UNSET
        if not isinstance(self.storage, Unset):
            storage = self.storage.to_dict()

        last_data_at: None | str | Unset
        if isinstance(self.last_data_at, Unset):
            last_data_at = UNSET
        elif isinstance(self.last_data_at, datetime.datetime):
            last_data_at = self.last_data_at.isoformat()
        else:
            last_data_at = self.last_data_at

        budget_hours_remaining: float | None | Unset
        if isinstance(self.budget_hours_remaining, Unset):
            budget_hours_remaining = UNSET
        else:
            budget_hours_remaining = self.budget_hours_remaining

        newest_frame_at: None | str | Unset
        if isinstance(self.newest_frame_at, Unset):
            newest_frame_at = UNSET
        elif isinstance(self.newest_frame_at, datetime.datetime):
            newest_frame_at = self.newest_frame_at.isoformat()
        else:
            newest_frame_at = self.newest_frame_at

        frame_age_seconds: int | None | Unset
        if isinstance(self.frame_age_seconds, Unset):
            frame_age_seconds = UNSET
        else:
            frame_age_seconds = self.frame_age_seconds

        details: dict[str, Any] | Unset = UNSET
        if not isinstance(self.details, Unset):
            details = self.details.to_dict()

        last_doctor_report: dict[str, Any] | None | Unset
        if isinstance(self.last_doctor_report, Unset):
            last_doctor_report = UNSET
        elif isinstance(self.last_doctor_report, DoctorReportSummary):
            last_doctor_report = self.last_doctor_report.to_dict()
        else:
            last_doctor_report = self.last_doctor_report

        spent_usd_this_month: float | None | Unset
        if isinstance(self.spent_usd_this_month, Unset):
            spent_usd_this_month = UNSET
        else:
            spent_usd_this_month = self.spent_usd_this_month

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "status": status,
                "consecutiveFailures": consecutive_failures,
            }
        )
        if status_reason is not UNSET:
            field_dict["statusReason"] = status_reason
        if last_attempt_at is not UNSET:
            field_dict["lastAttemptAt"] = last_attempt_at
        if last_success_at is not UNSET:
            field_dict["lastSuccessAt"] = last_success_at
        if last_error_at is not UNSET:
            field_dict["lastErrorAt"] = last_error_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if last_http_status is not UNSET:
            field_dict["lastHttpStatus"] = last_http_status
        if last_records_seen is not UNSET:
            field_dict["lastRecordsSeen"] = last_records_seen
        if last_records_changed is not UNSET:
            field_dict["lastRecordsChanged"] = last_records_changed
        if error_rate_last_hour is not UNSET:
            field_dict["errorRateLastHour"] = error_rate_last_hour
        if backoff_until is not UNSET:
            field_dict["backoffUntil"] = backoff_until
        if budget is not UNSET:
            field_dict["budget"] = budget
        if budget_used_today is not UNSET:
            field_dict["budgetUsedToday"] = budget_used_today
        if budget_remaining_today is not UNSET:
            field_dict["budgetRemainingToday"] = budget_remaining_today
        if refused_last_hour is not UNSET:
            field_dict["refusedLastHour"] = refused_last_hour
        if requests_last_hour is not UNSET:
            field_dict["requestsLastHour"] = requests_last_hour
        if bytes_last_hour is not UNSET:
            field_dict["bytesLastHour"] = bytes_last_hour
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if storage is not UNSET:
            field_dict["storage"] = storage
        if last_data_at is not UNSET:
            field_dict["lastDataAt"] = last_data_at
        if budget_hours_remaining is not UNSET:
            field_dict["budgetHoursRemaining"] = budget_hours_remaining
        if newest_frame_at is not UNSET:
            field_dict["newestFrameAt"] = newest_frame_at
        if frame_age_seconds is not UNSET:
            field_dict["frameAgeSeconds"] = frame_age_seconds
        if details is not UNSET:
            field_dict["details"] = details
        if last_doctor_report is not UNSET:
            field_dict["lastDoctorReport"] = last_doctor_report
        if spent_usd_this_month is not UNSET:
            field_dict["spentUsdThisMonth"] = spent_usd_this_month

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.budget import Budget
        from ..models.coverage import Coverage
        from ..models.doctor_report_summary import DoctorReportSummary
        from ..models.source_health_details import SourceHealthDetails
        from ..models.source_storage import SourceStorage

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        status = HealthStatus(d.pop("status"))

        consecutive_failures = d.pop("consecutiveFailures")

        def _parse_status_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_reason = _parse_status_reason(d.pop("statusReason", UNSET))

        def _parse_last_attempt_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_attempt_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_attempt_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_attempt_at = _parse_last_attempt_at(d.pop("lastAttemptAt", UNSET))

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

        def _parse_last_error_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_error_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_error_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_error_at = _parse_last_error_at(d.pop("lastErrorAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        def _parse_last_http_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_http_status = _parse_last_http_status(d.pop("lastHttpStatus", UNSET))

        last_records_seen = d.pop("lastRecordsSeen", UNSET)

        last_records_changed = d.pop("lastRecordsChanged", UNSET)

        error_rate_last_hour = d.pop("errorRateLastHour", UNSET)

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

        _budget = d.pop("budget", UNSET)
        budget: Budget | Unset
        if isinstance(_budget, Unset):
            budget = UNSET
        else:
            budget = Budget.from_dict(_budget)

        budget_used_today = d.pop("budgetUsedToday", UNSET)

        budget_remaining_today = d.pop("budgetRemainingToday", UNSET)

        refused_last_hour = d.pop("refusedLastHour", UNSET)

        requests_last_hour = d.pop("requestsLastHour", UNSET)

        bytes_last_hour = d.pop("bytesLastHour", UNSET)

        def _parse_coverage(data: object) -> Coverage | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coverage_type_0 = Coverage.from_dict(data)

                return coverage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Coverage | None | Unset, data)

        coverage = _parse_coverage(d.pop("coverage", UNSET))

        _storage = d.pop("storage", UNSET)
        storage: SourceStorage | Unset
        if isinstance(_storage, Unset):
            storage = UNSET
        else:
            storage = SourceStorage.from_dict(_storage)

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

        def _parse_budget_hours_remaining(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        budget_hours_remaining = _parse_budget_hours_remaining(
            d.pop("budgetHoursRemaining", UNSET)
        )

        def _parse_newest_frame_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                newest_frame_at_type_0 = datetime.datetime.fromisoformat(data)

                return newest_frame_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        newest_frame_at = _parse_newest_frame_at(d.pop("newestFrameAt", UNSET))

        def _parse_frame_age_seconds(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        frame_age_seconds = _parse_frame_age_seconds(d.pop("frameAgeSeconds", UNSET))

        _details = d.pop("details", UNSET)
        details: SourceHealthDetails | Unset
        if isinstance(_details, Unset):
            details = UNSET
        else:
            details = SourceHealthDetails.from_dict(_details)

        def _parse_last_doctor_report(
            data: object,
        ) -> DoctorReportSummary | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                last_doctor_report_type_0 = DoctorReportSummary.from_dict(data)

                return last_doctor_report_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DoctorReportSummary | None | Unset, data)

        last_doctor_report = _parse_last_doctor_report(d.pop("lastDoctorReport", UNSET))

        def _parse_spent_usd_this_month(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        spent_usd_this_month = _parse_spent_usd_this_month(
            d.pop("spentUsdThisMonth", UNSET)
        )

        source_health = cls(
            source_id=source_id,
            status=status,
            consecutive_failures=consecutive_failures,
            status_reason=status_reason,
            last_attempt_at=last_attempt_at,
            last_success_at=last_success_at,
            last_error_at=last_error_at,
            last_error=last_error,
            last_http_status=last_http_status,
            last_records_seen=last_records_seen,
            last_records_changed=last_records_changed,
            error_rate_last_hour=error_rate_last_hour,
            backoff_until=backoff_until,
            budget=budget,
            budget_used_today=budget_used_today,
            budget_remaining_today=budget_remaining_today,
            refused_last_hour=refused_last_hour,
            requests_last_hour=requests_last_hour,
            bytes_last_hour=bytes_last_hour,
            coverage=coverage,
            storage=storage,
            last_data_at=last_data_at,
            budget_hours_remaining=budget_hours_remaining,
            newest_frame_at=newest_frame_at,
            frame_age_seconds=frame_age_seconds,
            details=details,
            last_doctor_report=last_doctor_report,
            spent_usd_this_month=spent_usd_this_month,
        )

        source_health.additional_properties = d
        return source_health

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
