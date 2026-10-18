from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_report_probe_type_0_skip_reason_type_1 import (
    DoctorReportProbeType0SkipReasonType1,
)
from ..models.doctor_report_probe_type_0_skip_reason_type_2_type_1 import (
    DoctorReportProbeType0SkipReasonType2Type1,
)
from ..models.doctor_report_probe_type_0_skip_reason_type_3_type_1 import (
    DoctorReportProbeType0SkipReasonType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="DoctorReportProbeType0")


@_attrs_define
class DoctorReportProbeType0:
    """Probe summary

    Attributes:
        skipped (bool):
        status (None | str | Unset): ok, http-error, parse-error or failed (no response)
        http_status (int | None | Unset):
        content_type (None | str | Unset):
        bytes_ (int | None | Unset):
        url (None | str | Unset): Redacted
        duration_ms (int | None | Unset):
        skip_reason (DoctorReportProbeType0SkipReasonType1 | DoctorReportProbeType0SkipReasonType2Type1 |
            DoctorReportProbeType0SkipReasonType3Type1 | None | Unset): Why no probe was sent (budget and backoff give the
            finding PROBE_SKIPPED_BUDGET)
        records (int | None | Unset): Records the adapter parsed from the probe (at most 100 counted)
        requests (int | None | Unset): Upstream requests the probe made
        error (None | str | Unset): The request failure, redacted
    """

    skipped: bool
    status: None | str | Unset = UNSET
    http_status: int | None | Unset = UNSET
    content_type: None | str | Unset = UNSET
    bytes_: int | None | Unset = UNSET
    url: None | str | Unset = UNSET
    duration_ms: int | None | Unset = UNSET
    skip_reason: (
        DoctorReportProbeType0SkipReasonType1
        | DoctorReportProbeType0SkipReasonType2Type1
        | DoctorReportProbeType0SkipReasonType3Type1
        | None
        | Unset
    ) = UNSET
    records: int | None | Unset = UNSET
    requests: int | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        skipped = self.skipped

        status: None | str | Unset
        if isinstance(self.status, Unset):
            status = UNSET
        else:
            status = self.status

        http_status: int | None | Unset
        if isinstance(self.http_status, Unset):
            http_status = UNSET
        else:
            http_status = self.http_status

        content_type: None | str | Unset
        if isinstance(self.content_type, Unset):
            content_type = UNSET
        else:
            content_type = self.content_type

        bytes_: int | None | Unset
        if isinstance(self.bytes_, Unset):
            bytes_ = UNSET
        else:
            bytes_ = self.bytes_

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        duration_ms: int | None | Unset
        if isinstance(self.duration_ms, Unset):
            duration_ms = UNSET
        else:
            duration_ms = self.duration_ms

        skip_reason: None | str | Unset
        if isinstance(self.skip_reason, Unset):
            skip_reason = UNSET
        elif (
            isinstance(self.skip_reason, DoctorReportProbeType0SkipReasonType1)
            or isinstance(self.skip_reason, DoctorReportProbeType0SkipReasonType2Type1)
            or isinstance(self.skip_reason, DoctorReportProbeType0SkipReasonType3Type1)
        ):
            skip_reason = self.skip_reason.value
        else:
            skip_reason = self.skip_reason

        records: int | None | Unset
        if isinstance(self.records, Unset):
            records = UNSET
        else:
            records = self.records

        requests: int | None | Unset
        if isinstance(self.requests, Unset):
            requests = UNSET
        else:
            requests = self.requests

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "skipped": skipped,
            }
        )
        if status is not UNSET:
            field_dict["status"] = status
        if http_status is not UNSET:
            field_dict["httpStatus"] = http_status
        if content_type is not UNSET:
            field_dict["contentType"] = content_type
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if url is not UNSET:
            field_dict["url"] = url
        if duration_ms is not UNSET:
            field_dict["durationMs"] = duration_ms
        if skip_reason is not UNSET:
            field_dict["skipReason"] = skip_reason
        if records is not UNSET:
            field_dict["records"] = records
        if requests is not UNSET:
            field_dict["requests"] = requests
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        skipped = d.pop("skipped")

        def _parse_status(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status = _parse_status(d.pop("status", UNSET))

        def _parse_http_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        http_status = _parse_http_status(d.pop("httpStatus", UNSET))

        def _parse_content_type(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        content_type = _parse_content_type(d.pop("contentType", UNSET))

        def _parse_bytes_(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_ = _parse_bytes_(d.pop("bytes", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_duration_ms(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        duration_ms = _parse_duration_ms(d.pop("durationMs", UNSET))

        def _parse_skip_reason(
            data: object,
        ) -> (
            DoctorReportProbeType0SkipReasonType1
            | DoctorReportProbeType0SkipReasonType2Type1
            | DoctorReportProbeType0SkipReasonType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                skip_reason_type_1 = DoctorReportProbeType0SkipReasonType1(data)

                return skip_reason_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                skip_reason_type_2_type_1 = DoctorReportProbeType0SkipReasonType2Type1(
                    data
                )

                return skip_reason_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                skip_reason_type_3_type_1 = DoctorReportProbeType0SkipReasonType3Type1(
                    data
                )

                return skip_reason_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                DoctorReportProbeType0SkipReasonType1
                | DoctorReportProbeType0SkipReasonType2Type1
                | DoctorReportProbeType0SkipReasonType3Type1
                | None
                | Unset,
                data,
            )

        skip_reason = _parse_skip_reason(d.pop("skipReason", UNSET))

        def _parse_records(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        records = _parse_records(d.pop("records", UNSET))

        def _parse_requests(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        requests = _parse_requests(d.pop("requests", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        doctor_report_probe_type_0 = cls(
            skipped=skipped,
            status=status,
            http_status=http_status,
            content_type=content_type,
            bytes_=bytes_,
            url=url,
            duration_ms=duration_ms,
            skip_reason=skip_reason,
            records=records,
            requests=requests,
            error=error,
        )

        doctor_report_probe_type_0.additional_properties = d
        return doctor_report_probe_type_0

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
