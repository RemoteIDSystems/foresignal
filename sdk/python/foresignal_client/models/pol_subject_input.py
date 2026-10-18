from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.pol_subject_input_subject_type import PolSubjectInputSubjectType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.pol_subject_input_params import PolSubjectInputParams


T = TypeVar("T", bound="PolSubjectInput")


@_attrs_define
class PolSubjectInput:
    """Opt a subject into pattern-of-life analysis (114). Private sources need admin

    Attributes:
        subject_type (PolSubjectInputSubjectType):
        ref (str): record: `{sourceId}/{externalId}`; entity: 098 entity id; watchlist: 036 watchlist id (every member)
        enabled (bool | Unset):  Default: True.
        params (PolSubjectInputParams | Unset): Stay and clustering parameters
    """

    subject_type: PolSubjectInputSubjectType
    ref: str
    enabled: bool | Unset = True
    params: PolSubjectInputParams | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subject_type = self.subject_type.value

        ref = self.ref

        enabled = self.enabled

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subjectType": subject_type,
                "ref": ref,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.pol_subject_input_params import PolSubjectInputParams

        d = dict(src_dict)
        subject_type = PolSubjectInputSubjectType(d.pop("subjectType"))

        ref = d.pop("ref")

        enabled = d.pop("enabled", UNSET)

        _params = d.pop("params", UNSET)
        params: PolSubjectInputParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = PolSubjectInputParams.from_dict(_params)

        pol_subject_input = cls(
            subject_type=subject_type,
            ref=ref,
            enabled=enabled,
            params=params,
        )

        pol_subject_input.additional_properties = d
        return pol_subject_input

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
