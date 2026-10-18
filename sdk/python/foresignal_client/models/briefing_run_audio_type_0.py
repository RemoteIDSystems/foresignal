from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.briefing_run_audio_type_0_content_type_type_1 import (
    BriefingRunAudioType0ContentTypeType1,
)
from ..models.briefing_run_audio_type_0_content_type_type_2_type_1 import (
    BriefingRunAudioType0ContentTypeType2Type1,
)
from ..models.briefing_run_audio_type_0_content_type_type_3_type_1 import (
    BriefingRunAudioType0ContentTypeType3Type1,
)
from ..models.briefing_run_audio_type_0_format_type_1 import (
    BriefingRunAudioType0FormatType1,
)
from ..models.briefing_run_audio_type_0_format_type_2_type_1 import (
    BriefingRunAudioType0FormatType2Type1,
)
from ..models.briefing_run_audio_type_0_format_type_3_type_1 import (
    BriefingRunAudioType0FormatType3Type1,
)
from ..models.briefing_run_audio_type_0_status import BriefingRunAudioType0Status
from ..types import UNSET, Unset

T = TypeVar("T", bound="BriefingRunAudioType0")


@_attrs_define
class BriefingRunAudioType0:
    """Synthesized audio of the run (141); null when the briefing has no `audio` option

    Attributes:
        status (BriefingRunAudioType0Status): `ok` stored; `failed` provider, budget or storage failure (the text was
            delivered anyway); `skipped` no `tts` provider or no text
        media_id (None | str | Unset): 043 media (role `audio`), signed with 109
        format_ (BriefingRunAudioType0FormatType1 | BriefingRunAudioType0FormatType2Type1 |
            BriefingRunAudioType0FormatType3Type1 | None | Unset):
        voice (None | str | Unset):
        characters (int | None | Unset): Characters synthesized (the `tts` budget unit)
        truncated (bool | Unset): True when the text was cut to fit (always for long `opus`; MP3 beyond 25 requests)
        error (None | str | Unset):
        bytes_ (int | None | Unset): Size of the stored audio
        content_type (BriefingRunAudioType0ContentTypeType1 | BriefingRunAudioType0ContentTypeType2Type1 |
            BriefingRunAudioType0ContentTypeType3Type1 | None | Unset): Media type of the stored audio
        signature (None | str | Unset): Detached JWS (`header..signature`, Ed25519, b64:false, `typ` the audio media
            type) over the audio bytes; verify against /.well-known/jwks.json (109)
    """

    status: BriefingRunAudioType0Status
    media_id: None | str | Unset = UNSET
    format_: (
        BriefingRunAudioType0FormatType1
        | BriefingRunAudioType0FormatType2Type1
        | BriefingRunAudioType0FormatType3Type1
        | None
        | Unset
    ) = UNSET
    voice: None | str | Unset = UNSET
    characters: int | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    error: None | str | Unset = UNSET
    bytes_: int | None | Unset = UNSET
    content_type: (
        BriefingRunAudioType0ContentTypeType1
        | BriefingRunAudioType0ContentTypeType2Type1
        | BriefingRunAudioType0ContentTypeType3Type1
        | None
        | Unset
    ) = UNSET
    signature: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        media_id: None | str | Unset
        if isinstance(self.media_id, Unset):
            media_id = UNSET
        else:
            media_id = self.media_id

        format_: None | str | Unset
        if isinstance(self.format_, Unset):
            format_ = UNSET
        elif (
            isinstance(self.format_, BriefingRunAudioType0FormatType1)
            or isinstance(self.format_, BriefingRunAudioType0FormatType2Type1)
            or isinstance(self.format_, BriefingRunAudioType0FormatType3Type1)
        ):
            format_ = self.format_.value
        else:
            format_ = self.format_

        voice: None | str | Unset
        if isinstance(self.voice, Unset):
            voice = UNSET
        else:
            voice = self.voice

        characters: int | None | Unset
        if isinstance(self.characters, Unset):
            characters = UNSET
        else:
            characters = self.characters

        truncated = self.truncated

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        bytes_: int | None | Unset
        if isinstance(self.bytes_, Unset):
            bytes_ = UNSET
        else:
            bytes_ = self.bytes_

        content_type: None | str | Unset
        if isinstance(self.content_type, Unset):
            content_type = UNSET
        elif (
            isinstance(self.content_type, BriefingRunAudioType0ContentTypeType1)
            or isinstance(self.content_type, BriefingRunAudioType0ContentTypeType2Type1)
            or isinstance(self.content_type, BriefingRunAudioType0ContentTypeType3Type1)
        ):
            content_type = self.content_type.value
        else:
            content_type = self.content_type

        signature: None | str | Unset
        if isinstance(self.signature, Unset):
            signature = UNSET
        else:
            signature = self.signature

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if media_id is not UNSET:
            field_dict["mediaId"] = media_id
        if format_ is not UNSET:
            field_dict["format"] = format_
        if voice is not UNSET:
            field_dict["voice"] = voice
        if characters is not UNSET:
            field_dict["characters"] = characters
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if error is not UNSET:
            field_dict["error"] = error
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if content_type is not UNSET:
            field_dict["contentType"] = content_type
        if signature is not UNSET:
            field_dict["signature"] = signature

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        status = BriefingRunAudioType0Status(d.pop("status"))

        def _parse_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        media_id = _parse_media_id(d.pop("mediaId", UNSET))

        def _parse_format_(
            data: object,
        ) -> (
            BriefingRunAudioType0FormatType1
            | BriefingRunAudioType0FormatType2Type1
            | BriefingRunAudioType0FormatType3Type1
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
                format_type_1 = BriefingRunAudioType0FormatType1(data)

                return format_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                format_type_2_type_1 = BriefingRunAudioType0FormatType2Type1(data)

                return format_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                format_type_3_type_1 = BriefingRunAudioType0FormatType3Type1(data)

                return format_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                BriefingRunAudioType0FormatType1
                | BriefingRunAudioType0FormatType2Type1
                | BriefingRunAudioType0FormatType3Type1
                | None
                | Unset,
                data,
            )

        format_ = _parse_format_(d.pop("format", UNSET))

        def _parse_voice(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        voice = _parse_voice(d.pop("voice", UNSET))

        def _parse_characters(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        characters = _parse_characters(d.pop("characters", UNSET))

        truncated = d.pop("truncated", UNSET)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_bytes_(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_ = _parse_bytes_(d.pop("bytes", UNSET))

        def _parse_content_type(
            data: object,
        ) -> (
            BriefingRunAudioType0ContentTypeType1
            | BriefingRunAudioType0ContentTypeType2Type1
            | BriefingRunAudioType0ContentTypeType3Type1
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
                content_type_type_1 = BriefingRunAudioType0ContentTypeType1(data)

                return content_type_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                content_type_type_2_type_1 = BriefingRunAudioType0ContentTypeType2Type1(
                    data
                )

                return content_type_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                content_type_type_3_type_1 = BriefingRunAudioType0ContentTypeType3Type1(
                    data
                )

                return content_type_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                BriefingRunAudioType0ContentTypeType1
                | BriefingRunAudioType0ContentTypeType2Type1
                | BriefingRunAudioType0ContentTypeType3Type1
                | None
                | Unset,
                data,
            )

        content_type = _parse_content_type(d.pop("contentType", UNSET))

        def _parse_signature(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        signature = _parse_signature(d.pop("signature", UNSET))

        briefing_run_audio_type_0 = cls(
            status=status,
            media_id=media_id,
            format_=format_,
            voice=voice,
            characters=characters,
            truncated=truncated,
            error=error,
            bytes_=bytes_,
            content_type=content_type,
            signature=signature,
        )

        briefing_run_audio_type_0.additional_properties = d
        return briefing_run_audio_type_0

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
