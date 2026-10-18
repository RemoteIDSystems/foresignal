from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sink_test_result_phase import SinkTestResultPhase
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_test_result_details import SinkTestResultDetails


T = TypeVar("T", bound="SinkTestResult")


@_attrs_define
class SinkTestResult:
    """Result of validating, connecting or binding once and sending one synthetic record (110)

    Attributes:
        ok (bool):
        phase (SinkTestResultPhase): Last phase reached
        message (str):
        latency_ms (int | None | Unset):
        details (SinkTestResultDetails | Unset):
    """

    ok: bool
    phase: SinkTestResultPhase
    message: str
    latency_ms: int | None | Unset = UNSET
    details: SinkTestResultDetails | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ok = self.ok

        phase = self.phase.value

        message = self.message

        latency_ms: int | None | Unset
        if isinstance(self.latency_ms, Unset):
            latency_ms = UNSET
        else:
            latency_ms = self.latency_ms

        details: dict[str, Any] | Unset = UNSET
        if not isinstance(self.details, Unset):
            details = self.details.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ok": ok,
                "phase": phase,
                "message": message,
            }
        )
        if latency_ms is not UNSET:
            field_dict["latencyMs"] = latency_ms
        if details is not UNSET:
            field_dict["details"] = details

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_test_result_details import SinkTestResultDetails

        d = dict(src_dict)
        ok = d.pop("ok")

        phase = SinkTestResultPhase(d.pop("phase"))

        message = d.pop("message")

        def _parse_latency_ms(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        latency_ms = _parse_latency_ms(d.pop("latencyMs", UNSET))

        _details = d.pop("details", UNSET)
        details: SinkTestResultDetails | Unset
        if isinstance(_details, Unset):
            details = UNSET
        else:
            details = SinkTestResultDetails.from_dict(_details)

        sink_test_result = cls(
            ok=ok,
            phase=phase,
            message=message,
            latency_ms=latency_ms,
            details=details,
        )

        sink_test_result.additional_properties = d
        return sink_test_result

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
