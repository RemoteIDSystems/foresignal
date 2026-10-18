// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_match_result_query.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaMatchResultQueryCWProxy {
  MediaMatchResultQuery pdq(String pdq);

  MediaMatchResultQuery quality(int? quality);

  MediaMatchResultQuery source_(MediaMatchResultQuerySource_Enum source_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatchResultQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatchResultQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaMatchResultQuery call({
    String pdq,
    int? quality,
    MediaMatchResultQuerySource_Enum source_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaMatchResultQuery.copyWith(...)` or call `instanceOfMediaMatchResultQuery.copyWith.fieldName(value)` for a single field.
class _$MediaMatchResultQueryCWProxyImpl
    implements _$MediaMatchResultQueryCWProxy {
  const _$MediaMatchResultQueryCWProxyImpl(this._value);

  final MediaMatchResultQuery _value;

  @override
  MediaMatchResultQuery pdq(String pdq) => call(pdq: pdq);

  @override
  MediaMatchResultQuery quality(int? quality) => call(quality: quality);

  @override
  MediaMatchResultQuery source_(MediaMatchResultQuerySource_Enum source_) =>
      call(source_: source_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatchResultQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatchResultQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaMatchResultQuery call({
    Object? pdq = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
  }) {
    return MediaMatchResultQuery(
      pdq: pdq == const $CopyWithPlaceholder() || pdq == null
          ? _value.pdq
          // ignore: cast_nullable_to_non_nullable
          : pdq as String,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as int?,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as MediaMatchResultQuerySource_Enum,
    );
  }
}

extension $MediaMatchResultQueryCopyWith on MediaMatchResultQuery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaMatchResultQuery.copyWith(...)` or `instanceOfMediaMatchResultQuery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaMatchResultQueryCWProxy get copyWith =>
      _$MediaMatchResultQueryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaMatchResultQuery _$MediaMatchResultQueryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MediaMatchResultQuery', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['pdq', 'source']);
  final val = MediaMatchResultQuery(
    pdq: $checkedConvert('pdq', (v) => v as String),
    quality: $checkedConvert('quality', (v) => (v as num?)?.toInt()),
    source_: $checkedConvert(
      'source',
      (v) => $enumDecode(_$MediaMatchResultQuerySource_EnumEnumMap, v),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$MediaMatchResultQueryToJson(
  MediaMatchResultQuery instance,
) => <String, dynamic>{
  'pdq': instance.pdq,
  'quality': ?instance.quality,
  'source': _$MediaMatchResultQuerySource_EnumEnumMap[instance.source_]!,
};

const _$MediaMatchResultQuerySource_EnumEnumMap = {
  MediaMatchResultQuerySource_Enum.upload: 'upload',
  MediaMatchResultQuerySource_Enum.pdq: 'pdq',
};
