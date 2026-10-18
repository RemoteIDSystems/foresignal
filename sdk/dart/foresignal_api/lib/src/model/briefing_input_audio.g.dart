// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_input_audio.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingInputAudioCWProxy {
  BriefingInputAudio voice(String voice);

  BriefingInputAudio format(BriefingInputAudioFormatEnum? format);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingInputAudio(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingInputAudio(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingInputAudio call({String voice, BriefingInputAudioFormatEnum? format});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingInputAudio.copyWith(...)` or call `instanceOfBriefingInputAudio.copyWith.fieldName(value)` for a single field.
class _$BriefingInputAudioCWProxyImpl implements _$BriefingInputAudioCWProxy {
  const _$BriefingInputAudioCWProxyImpl(this._value);

  final BriefingInputAudio _value;

  @override
  BriefingInputAudio voice(String voice) => call(voice: voice);

  @override
  BriefingInputAudio format(BriefingInputAudioFormatEnum? format) =>
      call(format: format);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingInputAudio(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingInputAudio(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingInputAudio call({
    Object? voice = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
  }) {
    return BriefingInputAudio(
      voice: voice == const $CopyWithPlaceholder() || voice == null
          ? _value.voice
          // ignore: cast_nullable_to_non_nullable
          : voice as String,
      format: format == const $CopyWithPlaceholder()
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as BriefingInputAudioFormatEnum?,
    );
  }
}

extension $BriefingInputAudioCopyWith on BriefingInputAudio {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingInputAudio.copyWith(...)` or `instanceOfBriefingInputAudio.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingInputAudioCWProxy get copyWith =>
      _$BriefingInputAudioCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingInputAudio _$BriefingInputAudioFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BriefingInputAudio', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['voice']);
      final val = BriefingInputAudio(
        voice: $checkedConvert('voice', (v) => v as String),
        format: $checkedConvert(
          'format',
          (v) => $enumDecodeNullable(_$BriefingInputAudioFormatEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BriefingInputAudioToJson(BriefingInputAudio instance) =>
    <String, dynamic>{
      'voice': instance.voice,
      'format': ?_$BriefingInputAudioFormatEnumEnumMap[instance.format],
    };

const _$BriefingInputAudioFormatEnumEnumMap = {
  BriefingInputAudioFormatEnum.mp3: 'mp3',
  BriefingInputAudioFormatEnum.opus: 'opus',
};
