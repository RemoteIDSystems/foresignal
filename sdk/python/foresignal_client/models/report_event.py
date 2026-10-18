from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="ReportEvent")


@_attrs_define
class ReportEvent:
    """SSE `report.received` and `report.retracted` on the `me` topic

    Attributes:
        report_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        at (datetime.datetime):
        revision (int | None | Unset):
        from_ (None | PrincipalSummary | Unset):
        message (None | str | Unset):
        reason (None | str | Unset): Retraction reason
    """

    report_ref: str
    title: str
    tlp: Tlp
    at: datetime.datetime
    revision: int | None | Unset = UNSET
    from_: None | PrincipalSummary | Unset = UNSET
    message: None | str | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.principal_summary import PrincipalSummary

        report_ref = self.report_ref

        title = self.title

        tlp = self.tlp.value

        at = self.at.isoformat()

        revision: int | None | Unset
        if isinstance(self.revision, Unset):
            revision = UNSET
        else:
            revision = self.revision

        from_: dict[str, Any] | None | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, PrincipalSummary):
            from_ = self.from_.to_dict()
        else:
            from_ = self.from_

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reportRef": report_ref,
                "title": title,
                "tlp": tlp,
                "at": at,
            }
        )
        if revision is not UNSET:
            field_dict["revision"] = revision
        if from_ is not UNSET:
            field_dict["from"] = from_
        if message is not UNSET:
            field_dict["message"] = message
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        report_ref = d.pop("reportRef")

        title = d.pop("title")

        tlp = Tlp(d.pop("tlp"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_revision(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        revision = _parse_revision(d.pop("revision", UNSET))

        def _parse_from_(data: object) -> None | PrincipalSummary | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                from_type_0 = PrincipalSummary.from_dict(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrincipalSummary | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        report_event = cls(
            report_ref=report_ref,
            title=title,
            tlp=tlp,
            at=at,
            revision=revision,
            from_=from_,
            message=message,
            reason=reason,
        )

        report_event.additional_properties = d
        return report_event

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
