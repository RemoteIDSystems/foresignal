from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.reference_import_request_dataset import ReferenceImportRequestDataset
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.reference_import_request_params import ReferenceImportRequestParams


T = TypeVar("T", bound="ReferenceImportRequest")


@_attrs_define
class ReferenceImportRequest:
    """Start a reference-history import (counts only, licence kept)

    Attributes:
        dataset (ReferenceImportRequestDataset):
        params (ReferenceImportRequestParams | Unset): Dataset parameters
    """

    dataset: ReferenceImportRequestDataset
    params: ReferenceImportRequestParams | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        dataset = self.dataset.value

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "dataset": dataset,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.reference_import_request_params import (
            ReferenceImportRequestParams,
        )

        d = dict(src_dict)
        dataset = ReferenceImportRequestDataset(d.pop("dataset"))

        _params = d.pop("params", UNSET)
        params: ReferenceImportRequestParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = ReferenceImportRequestParams.from_dict(_params)

        reference_import_request = cls(
            dataset=dataset,
            params=params,
        )

        reference_import_request.additional_properties = d
        return reference_import_request

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
