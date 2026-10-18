// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_run_audio.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingRunAudioCWProxy {
  BriefingRunAudio status(BriefingRunAudioStatusEnum status);

  BriefingRunAudio mediaId(String? mediaId);

  BriefingRunAudio format(BriefingRunAudioFormatEnum? format);

  BriefingRunAudio voice(String? voice);

  BriefingRunAudio characters(int? characters);

  BriefingRunAudio truncated(bool? truncated);

  BriefingRunAudio error(String? error);

  BriefingRunAudio bytes(int? bytes);

  BriefingRunAudio contentType(BriefingRunAudioContentTypeEnum? contentType);

  BriefingRunAudio signature(String? signature);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunAudio(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunAudio(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingRunAudio call({
    BriefingRunAudioStatusEnum status,
    String? mediaId,
    BriefingRunAudioFormatEnum? format,
    String? voice,
    int? characters,
    bool? truncated,
    String? error,
    int? bytes,
    BriefingRunAudioContentTypeEnum? contentType,
    String? signature,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingRunAudio.copyWith(...)` or call `instanceOfBriefingRunAudio.copyWith.fieldName(value)` for a single field.
class _$BriefingRunAudioCWProxyImpl implements _$BriefingRunAudioCWProxy {
  const _$BriefingRunAudioCWProxyImpl(this._value);

  final BriefingRunAudio _value;

  @override
  BriefingRunAudio status(BriefingRunAudioStatusEnum status) =>
      call(status: status);

  @override
  BriefingRunAudio mediaId(String? mediaId) => call(mediaId: mediaId);

  @override
  BriefingRunAudio format(BriefingRunAudioFormatEnum? format) =>
      call(format: format);

  @override
  BriefingRunAudio voice(String? voice) => call(voice: voice);

  @override
  BriefingRunAudio characters(int? characters) => call(characters: characters);

  @override
  BriefingRunAudio truncated(bool? truncated) => call(truncated: truncated);

  @override
  BriefingRunAudio error(String? error) => call(error: error);

  @override
  BriefingRunAudio bytes(int? bytes) => call(bytes: bytes);

  @override
  BriefingRunAudio contentType(BriefingRunAudioContentTypeEnum? contentType) =>
      call(contentType: contentType);

  @override
  BriefingRunAudio signature(String? signature) => call(signature: signature);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunAudio(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunAudio(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingRunAudio call({
    Object? status = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? voice = const $CopyWithPlaceholder(),
    Object? characters = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? signature = const $CopyWithPlaceholder(),
  }) {
    return BriefingRunAudio(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BriefingRunAudioStatusEnum,
      mediaId: mediaId == const $CopyWithPlaceholder()
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String?,
      format: format == const $CopyWithPlaceholder()
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as BriefingRunAudioFormatEnum?,
      voice: voice == const $CopyWithPlaceholder()
          ? _value.voice
          // ignore: cast_nullable_to_non_nullable
          : voice as String?,
      characters: characters == const $CopyWithPlaceholder()
          ? _value.characters
          // ignore: cast_nullable_to_non_nullable
          : characters as int?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      contentType: contentType == const $CopyWithPlaceholder()
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as BriefingRunAudioContentTypeEnum?,
      signature: signature == const $CopyWithPlaceholder()
          ? _value.signature
          // ignore: cast_nullable_to_non_nullable
          : signature as String?,
    );
  }
}

extension $BriefingRunAudioCopyWith on BriefingRunAudio {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingRunAudio.copyWith(...)` or `instanceOfBriefingRunAudio.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingRunAudioCWProxy get copyWith => _$BriefingRunAudioCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingRunAudio _$BriefingRunAudioFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BriefingRunAudio', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = BriefingRunAudio(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$BriefingRunAudioStatusEnumEnumMap, v),
        ),
        mediaId: $checkedConvert('mediaId', (v) => v as String?),
        format: $checkedConvert(
          'format',
          (v) => $enumDecodeNullable(_$BriefingRunAudioFormatEnumEnumMap, v),
        ),
        voice: $checkedConvert('voice', (v) => v as String?),
        characters: $checkedConvert('characters', (v) => (v as num?)?.toInt()),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
        error: $checkedConvert('error', (v) => v as String?),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
        contentType: $checkedConvert(
          'contentType',
          (v) =>
              $enumDecodeNullable(_$BriefingRunAudioContentTypeEnumEnumMap, v),
        ),
        signature: $checkedConvert('signature', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BriefingRunAudioToJson(BriefingRunAudio instance) =>
    <String, dynamic>{
      'status': _$BriefingRunAudioStatusEnumEnumMap[instance.status]!,
      'mediaId': ?instance.mediaId,
      'format': ?_$BriefingRunAudioFormatEnumEnumMap[instance.format],
      'voice': ?instance.voice,
      'characters': ?instance.characters,
      'truncated': ?instance.truncated,
      'error': ?instance.error,
      'bytes': ?instance.bytes,
      'contentType':
          ?_$BriefingRunAudioContentTypeEnumEnumMap[instance.contentType],
      'signature': ?instance.signature,
    };

const _$BriefingRunAudioStatusEnumEnumMap = {
  BriefingRunAudioStatusEnum.ok: 'ok',
  BriefingRunAudioStatusEnum.failed: 'failed',
  BriefingRunAudioStatusEnum.skipped: 'skipped',
};

const _$BriefingRunAudioFormatEnumEnumMap = {
  BriefingRunAudioFormatEnum.mp3: 'mp3',
  BriefingRunAudioFormatEnum.opus: 'opus',
};

const _$BriefingRunAudioContentTypeEnumEnumMap = {
  BriefingRunAudioContentTypeEnum.audioSlashMpeg: 'audio/mpeg',
  BriefingRunAudioContentTypeEnum.audioSlashOgg: 'audio/ogg',
};
