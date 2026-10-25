from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_model_info import LlmModelInfo
    from ..models.llm_test_result import LlmTestResult


T = TypeVar("T", bound="LlmProbeResult")


@_attrs_define
class LlmProbeResult:
    """The result of probeLlmProvider (162): the connection test and the provider's models from one round trip.

    Attributes:
        test (LlmTestResult):
        models (list[LlmModelInfo]): The provider's models (Anthropic Models API, OpenAI-compatible /models, Ollama
            /api/tags); empty when it could not list them
        models_error (None | str | Unset): Why the models could not be listed, or null; a provider that cannot enumerate
            its models answers an empty list with this set
    """

    test: LlmTestResult
    models: list[LlmModelInfo]
    models_error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        test = self.test.to_dict()

        models = []
        for models_item_data in self.models:
            models_item = models_item_data.to_dict()
            models.append(models_item)

        models_error: None | str | Unset
        if isinstance(self.models_error, Unset):
            models_error = UNSET
        else:
            models_error = self.models_error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "test": test,
                "models": models,
            }
        )
        if models_error is not UNSET:
            field_dict["modelsError"] = models_error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_model_info import LlmModelInfo
        from ..models.llm_test_result import LlmTestResult

        d = dict(src_dict)
        test = LlmTestResult.from_dict(d.pop("test"))

        models = []
        _models = d.pop("models")
        for models_item_data in _models:
            models_item = LlmModelInfo.from_dict(models_item_data)

            models.append(models_item)

        def _parse_models_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        models_error = _parse_models_error(d.pop("modelsError", UNSET))

        llm_probe_result = cls(
            test=test,
            models=models,
            models_error=models_error,
        )

        llm_probe_result.additional_properties = d
        return llm_probe_result

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
