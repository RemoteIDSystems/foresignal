from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.annotation_input_kind import AnnotationInputKind
from ..models.annotation_input_verdict import AnnotationInputVerdict
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnnotationInput")


@_attrs_define
class AnnotationInput:
    """A new annotation; verdicts need agent scope. There is no tag kind: label objects with 130 tags; a note may carry
    free tags.

        Attributes:
            kind (AnnotationInputKind):
            verdict (AnnotationInputVerdict | Unset):
            duplicate_of_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
                refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            body (str | Unset):
            tags (list[str] | Unset):
            private (bool | Unset):  Default: False.
            apply_status (bool | Unset): Agent scope: a `rejected` verdict also dismisses the finding or intel event
                Default: False.
    """

    kind: AnnotationInputKind
    verdict: AnnotationInputVerdict | Unset = UNSET
    duplicate_of_ref: str | Unset = UNSET
    body: str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    private: bool | Unset = False
    apply_status: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        verdict: str | Unset = UNSET
        if not isinstance(self.verdict, Unset):
            verdict = self.verdict.value

        duplicate_of_ref = self.duplicate_of_ref

        body = self.body

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        private = self.private

        apply_status = self.apply_status

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
            }
        )
        if verdict is not UNSET:
            field_dict["verdict"] = verdict
        if duplicate_of_ref is not UNSET:
            field_dict["duplicateOfRef"] = duplicate_of_ref
        if body is not UNSET:
            field_dict["body"] = body
        if tags is not UNSET:
            field_dict["tags"] = tags
        if private is not UNSET:
            field_dict["private"] = private
        if apply_status is not UNSET:
            field_dict["applyStatus"] = apply_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = AnnotationInputKind(d.pop("kind"))

        _verdict = d.pop("verdict", UNSET)
        verdict: AnnotationInputVerdict | Unset
        if isinstance(_verdict, Unset):
            verdict = UNSET
        else:
            verdict = AnnotationInputVerdict(_verdict)

        duplicate_of_ref = d.pop("duplicateOfRef", UNSET)

        body = d.pop("body", UNSET)

        tags = cast(list[str], d.pop("tags", UNSET))

        private = d.pop("private", UNSET)

        apply_status = d.pop("applyStatus", UNSET)

        annotation_input = cls(
            kind=kind,
            verdict=verdict,
            duplicate_of_ref=duplicate_of_ref,
            body=body,
            tags=tags,
            private=private,
            apply_status=apply_status,
        )

        annotation_input.additional_properties = d
        return annotation_input

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
