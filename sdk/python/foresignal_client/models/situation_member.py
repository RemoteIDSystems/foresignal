from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_member_corroboration import SituationMemberCorroboration
from ..models.situation_member_member_type import SituationMemberMemberType
from ..models.situation_member_role import SituationMemberRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_join_reason import SituationJoinReason


T = TypeVar("T", bound="SituationMember")


@_attrs_define
class SituationMember:
    """A pinned member of a situation with its join reason and corroboration status

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        member_type (SituationMemberMemberType): Member type
        ref (str): Member id, or `sourceId/externalId` for records
        object_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        role (SituationMemberRole): `core` members drive status and severity; `context` members do not
        corroboration (SituationMemberCorroboration): 116 evidence-group roll-up for this member
        added_at (datetime.datetime):
        added_by (str): Actor: token id, `linker:<version>` or `analyst:<profile>`
        seq (int | None | Unset): Pinned record version seq (records)
        version (int | None | Unset): Pinned record version number (records)
        kind (None | str | Unset): Record kind or member kind
        signal_family (None | str | Unset): Signal family from the 119 registry (`intel/signal-families.json`, operator-
            extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-
            measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`,
            `markets`, `sanctions`, `health`, `space`
        title (None | str | Unset): Display title of the referenced object
        severity (None | str | Unset): Severity of the referenced object as it reported it
        observed_at (datetime.datetime | None | Unset): When the member was observed
        join_reason (None | SituationJoinReason | Unset):
    """

    id: str
    situation_id: str
    member_type: SituationMemberMemberType
    ref: str
    object_ref: str
    role: SituationMemberRole
    corroboration: SituationMemberCorroboration
    added_at: datetime.datetime
    added_by: str
    seq: int | None | Unset = UNSET
    version: int | None | Unset = UNSET
    kind: None | str | Unset = UNSET
    signal_family: None | str | Unset = UNSET
    title: None | str | Unset = UNSET
    severity: None | str | Unset = UNSET
    observed_at: datetime.datetime | None | Unset = UNSET
    join_reason: None | SituationJoinReason | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.situation_join_reason import SituationJoinReason

        id = self.id

        situation_id = self.situation_id

        member_type = self.member_type.value

        ref = self.ref

        object_ref = self.object_ref

        role = self.role.value

        corroboration = self.corroboration.value

        added_at = self.added_at.isoformat()

        added_by = self.added_by

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        version: int | None | Unset
        if isinstance(self.version, Unset):
            version = UNSET
        else:
            version = self.version

        kind: None | str | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        else:
            kind = self.kind

        signal_family: None | str | Unset
        if isinstance(self.signal_family, Unset):
            signal_family = UNSET
        else:
            signal_family = self.signal_family

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        else:
            severity = self.severity

        observed_at: None | str | Unset
        if isinstance(self.observed_at, Unset):
            observed_at = UNSET
        elif isinstance(self.observed_at, datetime.datetime):
            observed_at = self.observed_at.isoformat()
        else:
            observed_at = self.observed_at

        join_reason: dict[str, Any] | None | Unset
        if isinstance(self.join_reason, Unset):
            join_reason = UNSET
        elif isinstance(self.join_reason, SituationJoinReason):
            join_reason = self.join_reason.to_dict()
        else:
            join_reason = self.join_reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "situationId": situation_id,
                "memberType": member_type,
                "ref": ref,
                "objectRef": object_ref,
                "role": role,
                "corroboration": corroboration,
                "addedAt": added_at,
                "addedBy": added_by,
            }
        )
        if seq is not UNSET:
            field_dict["seq"] = seq
        if version is not UNSET:
            field_dict["version"] = version
        if kind is not UNSET:
            field_dict["kind"] = kind
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if title is not UNSET:
            field_dict["title"] = title
        if severity is not UNSET:
            field_dict["severity"] = severity
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if join_reason is not UNSET:
            field_dict["joinReason"] = join_reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_join_reason import SituationJoinReason

        d = dict(src_dict)
        id = d.pop("id")

        situation_id = d.pop("situationId")

        member_type = SituationMemberMemberType(d.pop("memberType"))

        ref = d.pop("ref")

        object_ref = d.pop("objectRef")

        role = SituationMemberRole(d.pop("role"))

        corroboration = SituationMemberCorroboration(d.pop("corroboration"))

        added_at = datetime.datetime.fromisoformat(d.pop("addedAt"))

        added_by = d.pop("addedBy")

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_version(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        version = _parse_version(d.pop("version", UNSET))

        def _parse_kind(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind = _parse_kind(d.pop("kind", UNSET))

        def _parse_signal_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        signal_family = _parse_signal_family(d.pop("signalFamily", UNSET))

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_severity(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        def _parse_observed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                observed_at_type_0 = datetime.datetime.fromisoformat(data)

                return observed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        observed_at = _parse_observed_at(d.pop("observedAt", UNSET))

        def _parse_join_reason(data: object) -> None | SituationJoinReason | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                join_reason_type_0 = SituationJoinReason.from_dict(data)

                return join_reason_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SituationJoinReason | Unset, data)

        join_reason = _parse_join_reason(d.pop("joinReason", UNSET))

        situation_member = cls(
            id=id,
            situation_id=situation_id,
            member_type=member_type,
            ref=ref,
            object_ref=object_ref,
            role=role,
            corroboration=corroboration,
            added_at=added_at,
            added_by=added_by,
            seq=seq,
            version=version,
            kind=kind,
            signal_family=signal_family,
            title=title,
            severity=severity,
            observed_at=observed_at,
            join_reason=join_reason,
        )

        situation_member.additional_properties = d
        return situation_member

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
