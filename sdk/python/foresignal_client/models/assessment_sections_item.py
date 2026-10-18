from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.assessment_sections_item_key import AssessmentSectionsItemKey
from ..models.assessment_sections_item_text_origin import (
    AssessmentSectionsItemTextOrigin,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.assessment_sections_item_data_type_0 import (
        AssessmentSectionsItemDataType0,
    )


T = TypeVar("T", bound="AssessmentSectionsItem")


@_attrs_define
class AssessmentSectionsItem:
    """Rendered section

    Attributes:
        key (AssessmentSectionsItemKey):
        title (str):
        markdown (str):
        text_origin (AssessmentSectionsItemTextOrigin):
        data (AssessmentSectionsItemDataType0 | None | Unset):
    """

    key: AssessmentSectionsItemKey
    title: str
    markdown: str
    text_origin: AssessmentSectionsItemTextOrigin
    data: AssessmentSectionsItemDataType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.assessment_sections_item_data_type_0 import (
            AssessmentSectionsItemDataType0,
        )

        key = self.key.value

        title = self.title

        markdown = self.markdown

        text_origin = self.text_origin.value

        data: dict[str, Any] | None | Unset
        if isinstance(self.data, Unset):
            data = UNSET
        elif isinstance(self.data, AssessmentSectionsItemDataType0):
            data = self.data.to_dict()
        else:
            data = self.data

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "key": key,
                "title": title,
                "markdown": markdown,
                "textOrigin": text_origin,
            }
        )
        if data is not UNSET:
            field_dict["data"] = data

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.assessment_sections_item_data_type_0 import (
            AssessmentSectionsItemDataType0,
        )

        d = dict(src_dict)
        key = AssessmentSectionsItemKey(d.pop("key"))

        title = d.pop("title")

        markdown = d.pop("markdown")

        text_origin = AssessmentSectionsItemTextOrigin(d.pop("textOrigin"))

        def _parse_data(data: object) -> AssessmentSectionsItemDataType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                data_type_0 = AssessmentSectionsItemDataType0.from_dict(data)

                return data_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AssessmentSectionsItemDataType0 | None | Unset, data)

        data = _parse_data(d.pop("data", UNSET))

        assessment_sections_item = cls(
            key=key,
            title=title,
            markdown=markdown,
            text_origin=text_origin,
            data=data,
        )

        assessment_sections_item.additional_properties = d
        return assessment_sections_item

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
