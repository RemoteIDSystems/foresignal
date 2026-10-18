from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.report_revision_status import ReportRevisionStatus
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary
    from ..models.report_revision_renditions import ReportRevisionRenditions


T = TypeVar("T", bound="ReportRevision")


@_attrs_define
class ReportRevision:
    """An immutable published revision with pinned refs, stored snapshots and renditions

    Attributes:
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        revision (int):
        title (str):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        status (ReportRevisionStatus):
        published_at (datetime.datetime):
        published_by (PrincipalSummary | Unset): Public view of a principal: handle, display name, kind and avatar only
        retracted_at (datetime.datetime | None | Unset):
        retraction_reason (None | str | Unset):
        manifest_id (None | str | Unset):
        pinned_refs (list[str] | Unset):
        bundle_sha_256 (str | Unset):
        renditions (ReportRevisionRenditions | Unset): Rendition links
    """

    report_id: str
    revision: int
    title: str
    tlp: Tlp
    status: ReportRevisionStatus
    published_at: datetime.datetime
    published_by: PrincipalSummary | Unset = UNSET
    retracted_at: datetime.datetime | None | Unset = UNSET
    retraction_reason: None | str | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    pinned_refs: list[str] | Unset = UNSET
    bundle_sha_256: str | Unset = UNSET
    renditions: ReportRevisionRenditions | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        report_id = self.report_id

        revision = self.revision

        title = self.title

        tlp = self.tlp.value

        status = self.status.value

        published_at = self.published_at.isoformat()

        published_by: dict[str, Any] | Unset = UNSET
        if not isinstance(self.published_by, Unset):
            published_by = self.published_by.to_dict()

        retracted_at: None | str | Unset
        if isinstance(self.retracted_at, Unset):
            retracted_at = UNSET
        elif isinstance(self.retracted_at, datetime.datetime):
            retracted_at = self.retracted_at.isoformat()
        else:
            retracted_at = self.retracted_at

        retraction_reason: None | str | Unset
        if isinstance(self.retraction_reason, Unset):
            retraction_reason = UNSET
        else:
            retraction_reason = self.retraction_reason

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        pinned_refs: list[str] | Unset = UNSET
        if not isinstance(self.pinned_refs, Unset):
            pinned_refs = self.pinned_refs

        bundle_sha_256 = self.bundle_sha_256

        renditions: dict[str, Any] | Unset = UNSET
        if not isinstance(self.renditions, Unset):
            renditions = self.renditions.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reportId": report_id,
                "revision": revision,
                "title": title,
                "tlp": tlp,
                "status": status,
                "publishedAt": published_at,
            }
        )
        if published_by is not UNSET:
            field_dict["publishedBy"] = published_by
        if retracted_at is not UNSET:
            field_dict["retractedAt"] = retracted_at
        if retraction_reason is not UNSET:
            field_dict["retractionReason"] = retraction_reason
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if pinned_refs is not UNSET:
            field_dict["pinnedRefs"] = pinned_refs
        if bundle_sha_256 is not UNSET:
            field_dict["bundleSha256"] = bundle_sha_256
        if renditions is not UNSET:
            field_dict["renditions"] = renditions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary
        from ..models.report_revision_renditions import ReportRevisionRenditions

        d = dict(src_dict)
        report_id = d.pop("reportId")

        revision = d.pop("revision")

        title = d.pop("title")

        tlp = Tlp(d.pop("tlp"))

        status = ReportRevisionStatus(d.pop("status"))

        published_at = datetime.datetime.fromisoformat(d.pop("publishedAt"))

        _published_by = d.pop("publishedBy", UNSET)
        published_by: PrincipalSummary | Unset
        if isinstance(_published_by, Unset):
            published_by = UNSET
        else:
            published_by = PrincipalSummary.from_dict(_published_by)

        def _parse_retracted_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                retracted_at_type_0 = datetime.datetime.fromisoformat(data)

                return retracted_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        retracted_at = _parse_retracted_at(d.pop("retractedAt", UNSET))

        def _parse_retraction_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        retraction_reason = _parse_retraction_reason(d.pop("retractionReason", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        pinned_refs = cast(list[str], d.pop("pinnedRefs", UNSET))

        bundle_sha_256 = d.pop("bundleSha256", UNSET)

        _renditions = d.pop("renditions", UNSET)
        renditions: ReportRevisionRenditions | Unset
        if isinstance(_renditions, Unset):
            renditions = UNSET
        else:
            renditions = ReportRevisionRenditions.from_dict(_renditions)

        report_revision = cls(
            report_id=report_id,
            revision=revision,
            title=title,
            tlp=tlp,
            status=status,
            published_at=published_at,
            published_by=published_by,
            retracted_at=retracted_at,
            retraction_reason=retraction_reason,
            manifest_id=manifest_id,
            pinned_refs=pinned_refs,
            bundle_sha_256=bundle_sha_256,
            renditions=renditions,
        )

        report_revision.additional_properties = d
        return report_revision

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
