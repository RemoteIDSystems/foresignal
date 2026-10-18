from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.media_provider_egress import MediaProviderEgress
from ..models.media_provider_id import MediaProviderId
from ..models.media_provider_last_import_status_type_1 import (
    MediaProviderLastImportStatusType1,
)
from ..models.media_provider_last_import_status_type_2_type_1 import (
    MediaProviderLastImportStatusType2Type1,
)
from ..models.media_provider_last_import_status_type_3_type_1 import (
    MediaProviderLastImportStatusType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaProvider")


@_attrs_define
class MediaProvider:
    """A media-rating provider with its licence, egress and import state

    Attributes:
        id (MediaProviderId):
        name (str):
        enabled (bool):
        licence (str):  Example: CC BY 4.0.
        licence_acknowledged (bool):
        egress (MediaProviderEgress):
        default_enabled (bool | Unset):
        licence_url (None | str | Unset):
        attribution (None | str | Unset):
        acknowledged_by (None | str | Unset):
        acknowledged_at (datetime.datetime | None | Unset):
        permission_recorded (bool | Unset): Operator recorded redistribution permission (lifts LOCAL_ONLY where terms
            allow)
        credential_set (bool | Unset): MBFC: operator API key stored
        last_import_at (datetime.datetime | None | Unset):
        last_import_status (MediaProviderLastImportStatusType1 | MediaProviderLastImportStatusType2Type1 |
            MediaProviderLastImportStatusType3Type1 | None | Unset):
        ratings_count (int | Unset):
    """

    id: MediaProviderId
    name: str
    enabled: bool
    licence: str
    licence_acknowledged: bool
    egress: MediaProviderEgress
    default_enabled: bool | Unset = UNSET
    licence_url: None | str | Unset = UNSET
    attribution: None | str | Unset = UNSET
    acknowledged_by: None | str | Unset = UNSET
    acknowledged_at: datetime.datetime | None | Unset = UNSET
    permission_recorded: bool | Unset = UNSET
    credential_set: bool | Unset = UNSET
    last_import_at: datetime.datetime | None | Unset = UNSET
    last_import_status: (
        MediaProviderLastImportStatusType1
        | MediaProviderLastImportStatusType2Type1
        | MediaProviderLastImportStatusType3Type1
        | None
        | Unset
    ) = UNSET
    ratings_count: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id.value

        name = self.name

        enabled = self.enabled

        licence = self.licence

        licence_acknowledged = self.licence_acknowledged

        egress = self.egress.value

        default_enabled = self.default_enabled

        licence_url: None | str | Unset
        if isinstance(self.licence_url, Unset):
            licence_url = UNSET
        else:
            licence_url = self.licence_url

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        acknowledged_by: None | str | Unset
        if isinstance(self.acknowledged_by, Unset):
            acknowledged_by = UNSET
        else:
            acknowledged_by = self.acknowledged_by

        acknowledged_at: None | str | Unset
        if isinstance(self.acknowledged_at, Unset):
            acknowledged_at = UNSET
        elif isinstance(self.acknowledged_at, datetime.datetime):
            acknowledged_at = self.acknowledged_at.isoformat()
        else:
            acknowledged_at = self.acknowledged_at

        permission_recorded = self.permission_recorded

        credential_set = self.credential_set

        last_import_at: None | str | Unset
        if isinstance(self.last_import_at, Unset):
            last_import_at = UNSET
        elif isinstance(self.last_import_at, datetime.datetime):
            last_import_at = self.last_import_at.isoformat()
        else:
            last_import_at = self.last_import_at

        last_import_status: None | str | Unset
        if isinstance(self.last_import_status, Unset):
            last_import_status = UNSET
        elif (
            isinstance(self.last_import_status, MediaProviderLastImportStatusType1)
            or isinstance(
                self.last_import_status, MediaProviderLastImportStatusType2Type1
            )
            or isinstance(
                self.last_import_status, MediaProviderLastImportStatusType3Type1
            )
        ):
            last_import_status = self.last_import_status.value
        else:
            last_import_status = self.last_import_status

        ratings_count = self.ratings_count

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "enabled": enabled,
                "licence": licence,
                "licenceAcknowledged": licence_acknowledged,
                "egress": egress,
            }
        )
        if default_enabled is not UNSET:
            field_dict["defaultEnabled"] = default_enabled
        if licence_url is not UNSET:
            field_dict["licenceUrl"] = licence_url
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if acknowledged_by is not UNSET:
            field_dict["acknowledgedBy"] = acknowledged_by
        if acknowledged_at is not UNSET:
            field_dict["acknowledgedAt"] = acknowledged_at
        if permission_recorded is not UNSET:
            field_dict["permissionRecorded"] = permission_recorded
        if credential_set is not UNSET:
            field_dict["credentialSet"] = credential_set
        if last_import_at is not UNSET:
            field_dict["lastImportAt"] = last_import_at
        if last_import_status is not UNSET:
            field_dict["lastImportStatus"] = last_import_status
        if ratings_count is not UNSET:
            field_dict["ratingsCount"] = ratings_count

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = MediaProviderId(d.pop("id"))

        name = d.pop("name")

        enabled = d.pop("enabled")

        licence = d.pop("licence")

        licence_acknowledged = d.pop("licenceAcknowledged")

        egress = MediaProviderEgress(d.pop("egress"))

        default_enabled = d.pop("defaultEnabled", UNSET)

        def _parse_licence_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence_url = _parse_licence_url(d.pop("licenceUrl", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        def _parse_acknowledged_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        acknowledged_by = _parse_acknowledged_by(d.pop("acknowledgedBy", UNSET))

        def _parse_acknowledged_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                acknowledged_at_type_0 = datetime.datetime.fromisoformat(data)

                return acknowledged_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        acknowledged_at = _parse_acknowledged_at(d.pop("acknowledgedAt", UNSET))

        permission_recorded = d.pop("permissionRecorded", UNSET)

        credential_set = d.pop("credentialSet", UNSET)

        def _parse_last_import_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_import_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_import_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_import_at = _parse_last_import_at(d.pop("lastImportAt", UNSET))

        def _parse_last_import_status(
            data: object,
        ) -> (
            MediaProviderLastImportStatusType1
            | MediaProviderLastImportStatusType2Type1
            | MediaProviderLastImportStatusType3Type1
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
                last_import_status_type_1 = MediaProviderLastImportStatusType1(data)

                return last_import_status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_import_status_type_2_type_1 = (
                    MediaProviderLastImportStatusType2Type1(data)
                )

                return last_import_status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_import_status_type_3_type_1 = (
                    MediaProviderLastImportStatusType3Type1(data)
                )

                return last_import_status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                MediaProviderLastImportStatusType1
                | MediaProviderLastImportStatusType2Type1
                | MediaProviderLastImportStatusType3Type1
                | None
                | Unset,
                data,
            )

        last_import_status = _parse_last_import_status(d.pop("lastImportStatus", UNSET))

        ratings_count = d.pop("ratingsCount", UNSET)

        media_provider = cls(
            id=id,
            name=name,
            enabled=enabled,
            licence=licence,
            licence_acknowledged=licence_acknowledged,
            egress=egress,
            default_enabled=default_enabled,
            licence_url=licence_url,
            attribution=attribution,
            acknowledged_by=acknowledged_by,
            acknowledged_at=acknowledged_at,
            permission_recorded=permission_recorded,
            credential_set=credential_set,
            last_import_at=last_import_at,
            last_import_status=last_import_status,
            ratings_count=ratings_count,
        )

        media_provider.additional_properties = d
        return media_provider

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
