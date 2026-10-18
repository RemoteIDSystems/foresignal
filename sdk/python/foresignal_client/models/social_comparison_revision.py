from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_comparison_revision_previous_status_type_1 import (
    SocialComparisonRevisionPreviousStatusType1,
)
from ..models.social_comparison_revision_previous_status_type_2_type_1 import (
    SocialComparisonRevisionPreviousStatusType2Type1,
)
from ..models.social_comparison_revision_previous_status_type_3_type_1 import (
    SocialComparisonRevisionPreviousStatusType3Type1,
)
from ..models.social_comparison_revision_status import SocialComparisonRevisionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialComparisonRevision")


@_attrs_define
class SocialComparisonRevision:
    """One revision of a comparison as late evidence arrives

    Attributes:
        revision (int):
        at (datetime.datetime):
        status (SocialComparisonRevisionStatus):
        reason (str):
        previous_status (None | SocialComparisonRevisionPreviousStatusType1 |
            SocialComparisonRevisionPreviousStatusType2Type1 | SocialComparisonRevisionPreviousStatusType3Type1 | Unset):
        physical_evidence_refs (list[str] | Unset):
        manifest_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
    """

    revision: int
    at: datetime.datetime
    status: SocialComparisonRevisionStatus
    reason: str
    previous_status: (
        None
        | SocialComparisonRevisionPreviousStatusType1
        | SocialComparisonRevisionPreviousStatusType2Type1
        | SocialComparisonRevisionPreviousStatusType3Type1
        | Unset
    ) = UNSET
    physical_evidence_refs: list[str] | Unset = UNSET
    manifest_id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        revision = self.revision

        at = self.at.isoformat()

        status = self.status.value

        reason = self.reason

        previous_status: None | str | Unset
        if isinstance(self.previous_status, Unset):
            previous_status = UNSET
        elif (
            isinstance(
                self.previous_status, SocialComparisonRevisionPreviousStatusType1
            )
            or isinstance(
                self.previous_status, SocialComparisonRevisionPreviousStatusType2Type1
            )
            or isinstance(
                self.previous_status, SocialComparisonRevisionPreviousStatusType3Type1
            )
        ):
            previous_status = self.previous_status.value
        else:
            previous_status = self.previous_status

        physical_evidence_refs: list[str] | Unset = UNSET
        if not isinstance(self.physical_evidence_refs, Unset):
            physical_evidence_refs = self.physical_evidence_refs

        manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "revision": revision,
                "at": at,
                "status": status,
                "reason": reason,
            }
        )
        if previous_status is not UNSET:
            field_dict["previousStatus"] = previous_status
        if physical_evidence_refs is not UNSET:
            field_dict["physicalEvidenceRefs"] = physical_evidence_refs
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        revision = d.pop("revision")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        status = SocialComparisonRevisionStatus(d.pop("status"))

        reason = d.pop("reason")

        def _parse_previous_status(
            data: object,
        ) -> (
            None
            | SocialComparisonRevisionPreviousStatusType1
            | SocialComparisonRevisionPreviousStatusType2Type1
            | SocialComparisonRevisionPreviousStatusType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_1 = SocialComparisonRevisionPreviousStatusType1(
                    data
                )

                return previous_status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_2_type_1 = (
                    SocialComparisonRevisionPreviousStatusType2Type1(data)
                )

                return previous_status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_3_type_1 = (
                    SocialComparisonRevisionPreviousStatusType3Type1(data)
                )

                return previous_status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | SocialComparisonRevisionPreviousStatusType1
                | SocialComparisonRevisionPreviousStatusType2Type1
                | SocialComparisonRevisionPreviousStatusType3Type1
                | Unset,
                data,
            )

        previous_status = _parse_previous_status(d.pop("previousStatus", UNSET))

        physical_evidence_refs = cast(list[str], d.pop("physicalEvidenceRefs", UNSET))

        manifest_id = d.pop("manifestId", UNSET)

        social_comparison_revision = cls(
            revision=revision,
            at=at,
            status=status,
            reason=reason,
            previous_status=previous_status,
            physical_evidence_refs=physical_evidence_refs,
            manifest_id=manifest_id,
        )

        social_comparison_revision.additional_properties = d
        return social_comparison_revision

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
