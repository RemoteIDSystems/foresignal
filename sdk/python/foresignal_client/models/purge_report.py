from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.purge_report_citing_documents import PurgeReportCitingDocuments


T = TypeVar("T", bound="PurgeReport")


@_attrs_define
class PurgeReport:
    """Outcome of an entity purge (039). Messages already delivered to external channels cannot be recalled.

    Attributes:
        tombstone_seq (int):
        versions_deleted (int):
        alerts_redacted (int | Unset):
        alerts_deleted (int | Unset):
        embeddings_deleted (int | Unset):
        notifications_redacted (int | Unset):
        citing_documents (PurgeReportCitingDocuments | Unset): Documents citing the entity (counted, not edited)
    """

    tombstone_seq: int
    versions_deleted: int
    alerts_redacted: int | Unset = UNSET
    alerts_deleted: int | Unset = UNSET
    embeddings_deleted: int | Unset = UNSET
    notifications_redacted: int | Unset = UNSET
    citing_documents: PurgeReportCitingDocuments | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tombstone_seq = self.tombstone_seq

        versions_deleted = self.versions_deleted

        alerts_redacted = self.alerts_redacted

        alerts_deleted = self.alerts_deleted

        embeddings_deleted = self.embeddings_deleted

        notifications_redacted = self.notifications_redacted

        citing_documents: dict[str, Any] | Unset = UNSET
        if not isinstance(self.citing_documents, Unset):
            citing_documents = self.citing_documents.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tombstoneSeq": tombstone_seq,
                "versionsDeleted": versions_deleted,
            }
        )
        if alerts_redacted is not UNSET:
            field_dict["alertsRedacted"] = alerts_redacted
        if alerts_deleted is not UNSET:
            field_dict["alertsDeleted"] = alerts_deleted
        if embeddings_deleted is not UNSET:
            field_dict["embeddingsDeleted"] = embeddings_deleted
        if notifications_redacted is not UNSET:
            field_dict["notificationsRedacted"] = notifications_redacted
        if citing_documents is not UNSET:
            field_dict["citingDocuments"] = citing_documents

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.purge_report_citing_documents import PurgeReportCitingDocuments

        d = dict(src_dict)
        tombstone_seq = d.pop("tombstoneSeq")

        versions_deleted = d.pop("versionsDeleted")

        alerts_redacted = d.pop("alertsRedacted", UNSET)

        alerts_deleted = d.pop("alertsDeleted", UNSET)

        embeddings_deleted = d.pop("embeddingsDeleted", UNSET)

        notifications_redacted = d.pop("notificationsRedacted", UNSET)

        _citing_documents = d.pop("citingDocuments", UNSET)
        citing_documents: PurgeReportCitingDocuments | Unset
        if isinstance(_citing_documents, Unset):
            citing_documents = UNSET
        else:
            citing_documents = PurgeReportCitingDocuments.from_dict(_citing_documents)

        purge_report = cls(
            tombstone_seq=tombstone_seq,
            versions_deleted=versions_deleted,
            alerts_redacted=alerts_redacted,
            alerts_deleted=alerts_deleted,
            embeddings_deleted=embeddings_deleted,
            notifications_redacted=notifications_redacted,
            citing_documents=citing_documents,
        )

        purge_report.additional_properties = d
        return purge_report

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
