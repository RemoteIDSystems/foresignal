from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_options_detail import DoctorOptionsDetail
from ..models.doctor_options_llm import DoctorOptionsLlm
from ..types import UNSET, Unset

T = TypeVar("T", bound="DoctorOptions")


@_attrs_define
class DoctorOptions:
    """Options for a doctor run

    Attributes:
        probe (bool | Unset): Probe the live upstream through the budgeted client Default: True.
        llm (DoctorOptionsLlm | Unset): LLM explanation and patch proposal (026 `chat` role, metered as `doctor`)
            Default: DoctorOptionsLlm.AUTO.
        detail (DoctorOptionsDetail | Unset): Context size: compact ≤ 6k tokens, full ≤ 30k Default:
            DoctorOptionsDetail.COMPACT.
    """

    probe: bool | Unset = True
    llm: DoctorOptionsLlm | Unset = DoctorOptionsLlm.AUTO
    detail: DoctorOptionsDetail | Unset = DoctorOptionsDetail.COMPACT
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        probe = self.probe

        llm: str | Unset = UNSET
        if not isinstance(self.llm, Unset):
            llm = self.llm.value

        detail: str | Unset = UNSET
        if not isinstance(self.detail, Unset):
            detail = self.detail.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if probe is not UNSET:
            field_dict["probe"] = probe
        if llm is not UNSET:
            field_dict["llm"] = llm
        if detail is not UNSET:
            field_dict["detail"] = detail

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        probe = d.pop("probe", UNSET)

        _llm = d.pop("llm", UNSET)
        llm: DoctorOptionsLlm | Unset
        if isinstance(_llm, Unset):
            llm = UNSET
        else:
            llm = DoctorOptionsLlm(_llm)

        _detail = d.pop("detail", UNSET)
        detail: DoctorOptionsDetail | Unset
        if isinstance(_detail, Unset):
            detail = UNSET
        else:
            detail = DoctorOptionsDetail(_detail)

        doctor_options = cls(
            probe=probe,
            llm=llm,
            detail=detail,
        )

        doctor_options.additional_properties = d
        return doctor_options

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
