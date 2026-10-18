from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.annotation_patch_verdict import AnnotationPatchVerdict
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnnotationPatch")


@_attrs_define
class AnnotationPatch:
    """JSON merge patch for an annotation (author or admin)

    Attributes:
        verdict (AnnotationPatchVerdict | Unset):
        duplicate_of_ref (None | str | Unset):
        body (None | str | Unset):
        tags (list[str] | Unset):
        private (bool | Unset):
    """

    verdict: AnnotationPatchVerdict | Unset = UNSET
    duplicate_of_ref: None | str | Unset = UNSET
    body: None | str | Unset = UNSET
    tags: list[str] | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        verdict: str | Unset = UNSET
        if not isinstance(self.verdict, Unset):
            verdict = self.verdict.value

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

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
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

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _verdict = d.pop("verdict", UNSET)
        verdict: AnnotationPatchVerdict | Unset
        if isinstance(_verdict, Unset):
            verdict = UNSET
        else:
            verdict = AnnotationPatchVerdict(_verdict)

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

        private = d.pop("private", UNSET)

        annotation_patch = cls(
            verdict=verdict,
            duplicate_of_ref=duplicate_of_ref,
            body=body,
            tags=tags,
            private=private,
        )

        annotation_patch.additional_properties = d
        return annotation_patch

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
