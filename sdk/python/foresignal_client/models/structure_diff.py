from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.structure_diff_format import StructureDiffFormat
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.structure_diff_content_type_changed_type_0 import (
        StructureDiffContentTypeChangedType0,
    )
    from ..models.structure_diff_delimiter_changed_type_0 import (
        StructureDiffDelimiterChangedType0,
    )
    from ..models.structure_diff_examples import StructureDiffExamples
    from ..models.structure_diff_presence_dropped_item import (
        StructureDiffPresenceDroppedItem,
    )
    from ..models.structure_diff_rename_candidates_item import (
        StructureDiffRenameCandidatesItem,
    )
    from ..models.structure_diff_type_changed_item import StructureDiffTypeChangedItem


T = TypeVar("T", bound="StructureDiff")


@_attrs_define
class StructureDiff:
    """Structural difference between the probe body and the recorded fixture (paths and types, never bodies)

    Attributes:
        format_ (StructureDiffFormat):
        added_paths (list[str]):
        removed_paths (list[str]):
        type_changed (list[StructureDiffTypeChangedItem]):
        fixture_file (None | str | Unset):
        presence_dropped (list[StructureDiffPresenceDroppedItem] | Unset):
        array_empty (list[str] | Unset):
        columns_reordered (bool | Unset):
        delimiter_changed (None | StructureDiffDelimiterChangedType0 | Unset):
        content_type_changed (None | StructureDiffContentTypeChangedType0 | Unset):
        size_ratio (float | None | Unset):
        rename_candidates (list[StructureDiffRenameCandidatesItem] | Unset):
        html_title (None | str | Unset): `<title>` when HTML arrived instead of data
        examples (StructureDiffExamples | Unset): Path → up to 3 capped example values
    """

    format_: StructureDiffFormat
    added_paths: list[str]
    removed_paths: list[str]
    type_changed: list[StructureDiffTypeChangedItem]
    fixture_file: None | str | Unset = UNSET
    presence_dropped: list[StructureDiffPresenceDroppedItem] | Unset = UNSET
    array_empty: list[str] | Unset = UNSET
    columns_reordered: bool | Unset = UNSET
    delimiter_changed: None | StructureDiffDelimiterChangedType0 | Unset = UNSET
    content_type_changed: None | StructureDiffContentTypeChangedType0 | Unset = UNSET
    size_ratio: float | None | Unset = UNSET
    rename_candidates: list[StructureDiffRenameCandidatesItem] | Unset = UNSET
    html_title: None | str | Unset = UNSET
    examples: StructureDiffExamples | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.structure_diff_content_type_changed_type_0 import (
            StructureDiffContentTypeChangedType0,
        )
        from ..models.structure_diff_delimiter_changed_type_0 import (
            StructureDiffDelimiterChangedType0,
        )

        format_ = self.format_.value

        added_paths = self.added_paths

        removed_paths = self.removed_paths

        type_changed = []
        for type_changed_item_data in self.type_changed:
            type_changed_item = type_changed_item_data.to_dict()
            type_changed.append(type_changed_item)

        fixture_file: None | str | Unset
        if isinstance(self.fixture_file, Unset):
            fixture_file = UNSET
        else:
            fixture_file = self.fixture_file

        presence_dropped: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.presence_dropped, Unset):
            presence_dropped = []
            for presence_dropped_item_data in self.presence_dropped:
                presence_dropped_item = presence_dropped_item_data.to_dict()
                presence_dropped.append(presence_dropped_item)

        array_empty: list[str] | Unset = UNSET
        if not isinstance(self.array_empty, Unset):
            array_empty = self.array_empty

        columns_reordered = self.columns_reordered

        delimiter_changed: dict[str, Any] | None | Unset
        if isinstance(self.delimiter_changed, Unset):
            delimiter_changed = UNSET
        elif isinstance(self.delimiter_changed, StructureDiffDelimiterChangedType0):
            delimiter_changed = self.delimiter_changed.to_dict()
        else:
            delimiter_changed = self.delimiter_changed

        content_type_changed: dict[str, Any] | None | Unset
        if isinstance(self.content_type_changed, Unset):
            content_type_changed = UNSET
        elif isinstance(
            self.content_type_changed, StructureDiffContentTypeChangedType0
        ):
            content_type_changed = self.content_type_changed.to_dict()
        else:
            content_type_changed = self.content_type_changed

        size_ratio: float | None | Unset
        if isinstance(self.size_ratio, Unset):
            size_ratio = UNSET
        else:
            size_ratio = self.size_ratio

        rename_candidates: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.rename_candidates, Unset):
            rename_candidates = []
            for rename_candidates_item_data in self.rename_candidates:
                rename_candidates_item = rename_candidates_item_data.to_dict()
                rename_candidates.append(rename_candidates_item)

        html_title: None | str | Unset
        if isinstance(self.html_title, Unset):
            html_title = UNSET
        else:
            html_title = self.html_title

        examples: dict[str, Any] | Unset = UNSET
        if not isinstance(self.examples, Unset):
            examples = self.examples.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "format": format_,
                "addedPaths": added_paths,
                "removedPaths": removed_paths,
                "typeChanged": type_changed,
            }
        )
        if fixture_file is not UNSET:
            field_dict["fixtureFile"] = fixture_file
        if presence_dropped is not UNSET:
            field_dict["presenceDropped"] = presence_dropped
        if array_empty is not UNSET:
            field_dict["arrayEmpty"] = array_empty
        if columns_reordered is not UNSET:
            field_dict["columnsReordered"] = columns_reordered
        if delimiter_changed is not UNSET:
            field_dict["delimiterChanged"] = delimiter_changed
        if content_type_changed is not UNSET:
            field_dict["contentTypeChanged"] = content_type_changed
        if size_ratio is not UNSET:
            field_dict["sizeRatio"] = size_ratio
        if rename_candidates is not UNSET:
            field_dict["renameCandidates"] = rename_candidates
        if html_title is not UNSET:
            field_dict["htmlTitle"] = html_title
        if examples is not UNSET:
            field_dict["examples"] = examples

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.structure_diff_content_type_changed_type_0 import (
            StructureDiffContentTypeChangedType0,
        )
        from ..models.structure_diff_delimiter_changed_type_0 import (
            StructureDiffDelimiterChangedType0,
        )
        from ..models.structure_diff_examples import StructureDiffExamples
        from ..models.structure_diff_presence_dropped_item import (
            StructureDiffPresenceDroppedItem,
        )
        from ..models.structure_diff_rename_candidates_item import (
            StructureDiffRenameCandidatesItem,
        )
        from ..models.structure_diff_type_changed_item import (
            StructureDiffTypeChangedItem,
        )

        d = dict(src_dict)
        format_ = StructureDiffFormat(d.pop("format"))

        added_paths = cast(list[str], d.pop("addedPaths"))

        removed_paths = cast(list[str], d.pop("removedPaths"))

        type_changed = []
        _type_changed = d.pop("typeChanged")
        for type_changed_item_data in _type_changed:
            type_changed_item = StructureDiffTypeChangedItem.from_dict(
                type_changed_item_data
            )

            type_changed.append(type_changed_item)

        def _parse_fixture_file(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        fixture_file = _parse_fixture_file(d.pop("fixtureFile", UNSET))

        _presence_dropped = d.pop("presenceDropped", UNSET)
        presence_dropped: list[StructureDiffPresenceDroppedItem] | Unset = UNSET
        if _presence_dropped is not UNSET:
            presence_dropped = []
            for presence_dropped_item_data in _presence_dropped:
                presence_dropped_item = StructureDiffPresenceDroppedItem.from_dict(
                    presence_dropped_item_data
                )

                presence_dropped.append(presence_dropped_item)

        array_empty = cast(list[str], d.pop("arrayEmpty", UNSET))

        columns_reordered = d.pop("columnsReordered", UNSET)

        def _parse_delimiter_changed(
            data: object,
        ) -> None | StructureDiffDelimiterChangedType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                delimiter_changed_type_0 = StructureDiffDelimiterChangedType0.from_dict(
                    data
                )

                return delimiter_changed_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StructureDiffDelimiterChangedType0 | Unset, data)

        delimiter_changed = _parse_delimiter_changed(d.pop("delimiterChanged", UNSET))

        def _parse_content_type_changed(
            data: object,
        ) -> None | StructureDiffContentTypeChangedType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                content_type_changed_type_0 = (
                    StructureDiffContentTypeChangedType0.from_dict(data)
                )

                return content_type_changed_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StructureDiffContentTypeChangedType0 | Unset, data)

        content_type_changed = _parse_content_type_changed(
            d.pop("contentTypeChanged", UNSET)
        )

        def _parse_size_ratio(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        size_ratio = _parse_size_ratio(d.pop("sizeRatio", UNSET))

        _rename_candidates = d.pop("renameCandidates", UNSET)
        rename_candidates: list[StructureDiffRenameCandidatesItem] | Unset = UNSET
        if _rename_candidates is not UNSET:
            rename_candidates = []
            for rename_candidates_item_data in _rename_candidates:
                rename_candidates_item = StructureDiffRenameCandidatesItem.from_dict(
                    rename_candidates_item_data
                )

                rename_candidates.append(rename_candidates_item)

        def _parse_html_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        html_title = _parse_html_title(d.pop("htmlTitle", UNSET))

        _examples = d.pop("examples", UNSET)
        examples: StructureDiffExamples | Unset
        if isinstance(_examples, Unset):
            examples = UNSET
        else:
            examples = StructureDiffExamples.from_dict(_examples)

        structure_diff = cls(
            format_=format_,
            added_paths=added_paths,
            removed_paths=removed_paths,
            type_changed=type_changed,
            fixture_file=fixture_file,
            presence_dropped=presence_dropped,
            array_empty=array_empty,
            columns_reordered=columns_reordered,
            delimiter_changed=delimiter_changed,
            content_type_changed=content_type_changed,
            size_ratio=size_ratio,
            rename_candidates=rename_candidates,
            html_title=html_title,
            examples=examples,
        )

        structure_diff.additional_properties = d
        return structure_diff

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
