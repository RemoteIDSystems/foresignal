from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.annotation_kind import AnnotationKind
from ..models.annotation_verdict_type_1 import AnnotationVerdictType1
from ..models.annotation_verdict_type_2_type_1 import AnnotationVerdictType2Type1
from ..models.annotation_verdict_type_3_type_1 import AnnotationVerdictType3Type1
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

T = TypeVar("T", bound="Annotation")


@_attrs_define
class Annotation:
    """A note or verdict on any object (labels use 130 tags)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        target_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (AnnotationKind):
        author (str): Principal id
        private (bool):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        verdict (AnnotationVerdictType1 | AnnotationVerdictType2Type1 | AnnotationVerdictType3Type1 | None | Unset):
        duplicate_of_ref (None | str | Unset):
        body (None | str | Unset):
        tags (list[str] | Unset):
        status_applied (bool | Unset): A rejected verdict with `applyStatus` dismissed the target
        deleted_at (datetime.datetime | None | Unset):
        parent_id (None | str | Unset): Root annotation for replies (threads)
        mentions (list[str] | Unset): Principal handles parsed from `@handle`
        dropped_mentions (list[str] | Unset): Mentions dropped because the principal cannot see the target (returned to
            the author only)
        resolved (bool | Unset): Thread resolved (root annotations)
        edited_at (datetime.datetime | None | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        visibility (Visibility | Unset):
    """

    id: str
    target_ref: str
    kind: AnnotationKind
    author: str
    private: bool
    created_at: datetime.datetime
    updated_at: datetime.datetime
    ref: str | Unset = UNSET
    verdict: (
        AnnotationVerdictType1
        | AnnotationVerdictType2Type1
        | AnnotationVerdictType3Type1
        | None
        | Unset
    ) = UNSET
    duplicate_of_ref: None | str | Unset = UNSET
    body: None | str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    status_applied: bool | Unset = UNSET
    deleted_at: datetime.datetime | None | Unset = UNSET
    parent_id: None | str | Unset = UNSET
    mentions: list[str] | Unset = UNSET
    dropped_mentions: list[str] | Unset = UNSET
    resolved: bool | Unset = UNSET
    edited_at: datetime.datetime | None | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        target_ref = self.target_ref

        kind = self.kind.value

        author = self.author

        private = self.private

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        ref = self.ref

        verdict: None | str | Unset
        if isinstance(self.verdict, Unset):
            verdict = UNSET
        elif (
            isinstance(self.verdict, AnnotationVerdictType1)
            or isinstance(self.verdict, AnnotationVerdictType2Type1)
            or isinstance(self.verdict, AnnotationVerdictType3Type1)
        ):
            verdict = self.verdict.value
        else:
            verdict = self.verdict

        duplicate_of_ref: None | str | Unset
        if isinstance(self.duplicate_of_ref, Unset):
            duplicate_of_ref = UNSET
        else:
            duplicate_of_ref = self.duplicate_of_ref

        body: None | str | Unset
        if isinstance(self.body, Unset):
            body = UNSET
        else:
            body = self.body

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        status_applied = self.status_applied

        deleted_at: None | str | Unset
        if isinstance(self.deleted_at, Unset):
            deleted_at = UNSET
        elif isinstance(self.deleted_at, datetime.datetime):
            deleted_at = self.deleted_at.isoformat()
        else:
            deleted_at = self.deleted_at

        parent_id: None | str | Unset
        if isinstance(self.parent_id, Unset):
            parent_id = UNSET
        else:
            parent_id = self.parent_id

        mentions: list[str] | Unset = UNSET
        if not isinstance(self.mentions, Unset):
            mentions = self.mentions

        dropped_mentions: list[str] | Unset = UNSET
        if not isinstance(self.dropped_mentions, Unset):
            dropped_mentions = self.dropped_mentions

        resolved = self.resolved

        edited_at: None | str | Unset
        if isinstance(self.edited_at, Unset):
            edited_at = UNSET
        elif isinstance(self.edited_at, datetime.datetime):
            edited_at = self.edited_at.isoformat()
        else:
            edited_at = self.edited_at

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "targetRef": target_ref,
                "kind": kind,
                "author": author,
                "private": private,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if verdict is not UNSET:
            field_dict["verdict"] = verdict
        if duplicate_of_ref is not UNSET:
            field_dict["duplicateOfRef"] = duplicate_of_ref
        if body is not UNSET:
            field_dict["body"] = body
        if tags is not UNSET:
            field_dict["tags"] = tags
        if status_applied is not UNSET:
            field_dict["statusApplied"] = status_applied
        if deleted_at is not UNSET:
            field_dict["deletedAt"] = deleted_at
        if parent_id is not UNSET:
            field_dict["parentId"] = parent_id
        if mentions is not UNSET:
            field_dict["mentions"] = mentions
        if dropped_mentions is not UNSET:
            field_dict["droppedMentions"] = dropped_mentions
        if resolved is not UNSET:
            field_dict["resolved"] = resolved
        if edited_at is not UNSET:
            field_dict["editedAt"] = edited_at
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if visibility is not UNSET:
            field_dict["visibility"] = visibility

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        target_ref = d.pop("targetRef")

        kind = AnnotationKind(d.pop("kind"))

        author = d.pop("author")

        private = d.pop("private")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        ref = d.pop("ref", UNSET)

        def _parse_verdict(
            data: object,
        ) -> (
            AnnotationVerdictType1
            | AnnotationVerdictType2Type1
            | AnnotationVerdictType3Type1
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
                verdict_type_1 = AnnotationVerdictType1(data)

                return verdict_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                verdict_type_2_type_1 = AnnotationVerdictType2Type1(data)

                return verdict_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                verdict_type_3_type_1 = AnnotationVerdictType3Type1(data)

                return verdict_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AnnotationVerdictType1
                | AnnotationVerdictType2Type1
                | AnnotationVerdictType3Type1
                | None
                | Unset,
                data,
            )

        verdict = _parse_verdict(d.pop("verdict", UNSET))

        def _parse_duplicate_of_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        duplicate_of_ref = _parse_duplicate_of_ref(d.pop("duplicateOfRef", UNSET))

        def _parse_body(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        body = _parse_body(d.pop("body", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        status_applied = d.pop("statusApplied", UNSET)

        def _parse_deleted_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                deleted_at_type_0 = datetime.datetime.fromisoformat(data)

                return deleted_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        deleted_at = _parse_deleted_at(d.pop("deletedAt", UNSET))

        def _parse_parent_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        parent_id = _parse_parent_id(d.pop("parentId", UNSET))

        mentions = cast(list[str], d.pop("mentions", UNSET))

        dropped_mentions = cast(list[str], d.pop("droppedMentions", UNSET))

        resolved = d.pop("resolved", UNSET)

        def _parse_edited_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                edited_at_type_0 = datetime.datetime.fromisoformat(data)

                return edited_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        edited_at = _parse_edited_at(d.pop("editedAt", UNSET))

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        annotation = cls(
            id=id,
            target_ref=target_ref,
            kind=kind,
            author=author,
            private=private,
            created_at=created_at,
            updated_at=updated_at,
            ref=ref,
            verdict=verdict,
            duplicate_of_ref=duplicate_of_ref,
            body=body,
            tags=tags,
            status_applied=status_applied,
            deleted_at=deleted_at,
            parent_id=parent_id,
            mentions=mentions,
            dropped_mentions=dropped_mentions,
            resolved=resolved,
            edited_at=edited_at,
            tlp=tlp,
            visibility=visibility,
        )

        annotation.additional_properties = d
        return annotation

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
