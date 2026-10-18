// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_quality.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingQualityCWProxy {
  RecordMappingQuality accuracyMeters(MappingValueSpec? accuracyMeters);

  RecordMappingQuality extra(Map<String, MappingValueSpec>? extra);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingQuality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingQuality(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingQuality call({
    MappingValueSpec? accuracyMeters,
    Map<String, MappingValueSpec>? extra,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingQuality.copyWith(...)` or call `instanceOfRecordMappingQuality.copyWith.fieldName(value)` for a single field.
class _$RecordMappingQualityCWProxyImpl
    implements _$RecordMappingQualityCWProxy {
  const _$RecordMappingQualityCWProxyImpl(this._value);

  final RecordMappingQuality _value;

  @override
  RecordMappingQuality accuracyMeters(MappingValueSpec? accuracyMeters) =>
      call(accuracyMeters: accuracyMeters);

  @override
  RecordMappingQuality extra(Map<String, MappingValueSpec>? extra) =>
      call(extra: extra);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingQuality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingQuality(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingQuality call({
    Object? accuracyMeters = const $CopyWithPlaceholder(),
    Object? extra = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingQuality(
      accuracyMeters: accuracyMeters == const $CopyWithPlaceholder()
          ? _value.accuracyMeters
          // ignore: cast_nullable_to_non_nullable
          : accuracyMeters as MappingValueSpec?,
      extra: extra == const $CopyWithPlaceholder()
          ? _value.extra
          // ignore: cast_nullable_to_non_nullable
          : extra as Map<String, MappingValueSpec>?,
    );
  }
}

extension $RecordMappingQualityCopyWith on RecordMappingQuality {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingQuality.copyWith(...)` or `instanceOfRecordMappingQuality.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingQualityCWProxy get copyWith =>
      _$RecordMappingQualityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingQuality _$RecordMappingQualityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingQuality', json, ($checkedConvert) {
  final val = RecordMappingQuality(
    accuracyMeters: $checkedConvert(
      'accuracyMeters',
      (v) => v == null
          ? null
          : MappingValueSpec.fromJson(v as Map<String, dynamic>),
    ),
    extra: $checkedConvert(
      'extra',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, MappingValueSpec.fromJson(e as Map<String, dynamic>)),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$RecordMappingQualityToJson(
  RecordMappingQuality instance,
) => <String, dynamic>{
  'accuracyMeters': ?instance.accuracyMeters?.toJson(),
  'extra': ?instance.extra?.map((k, e) => MapEntry(k, e.toJson())),
};
