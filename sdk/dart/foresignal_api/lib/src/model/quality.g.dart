// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'quality.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QualityCWProxy {
  Quality accuracyMeters(num? accuracyMeters);

  Quality confidence(num? confidence);

  Quality positionSource(String? positionSource);

  Quality extra(Map<String, Object>? extra);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Quality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Quality(...).copyWith(id: 12, name: "My name")
  /// ```
  Quality call({
    num? accuracyMeters,
    num? confidence,
    String? positionSource,
    Map<String, Object>? extra,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuality.copyWith(...)` or call `instanceOfQuality.copyWith.fieldName(value)` for a single field.
class _$QualityCWProxyImpl implements _$QualityCWProxy {
  const _$QualityCWProxyImpl(this._value);

  final Quality _value;

  @override
  Quality accuracyMeters(num? accuracyMeters) =>
      call(accuracyMeters: accuracyMeters);

  @override
  Quality confidence(num? confidence) => call(confidence: confidence);

  @override
  Quality positionSource(String? positionSource) =>
      call(positionSource: positionSource);

  @override
  Quality extra(Map<String, Object>? extra) => call(extra: extra);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Quality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Quality(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Quality call({
    Object? accuracyMeters = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? positionSource = const $CopyWithPlaceholder(),
    Object? extra = const $CopyWithPlaceholder(),
  }) {
    return Quality(
      accuracyMeters: accuracyMeters == const $CopyWithPlaceholder()
          ? _value.accuracyMeters
          // ignore: cast_nullable_to_non_nullable
          : accuracyMeters as num?,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      positionSource: positionSource == const $CopyWithPlaceholder()
          ? _value.positionSource
          // ignore: cast_nullable_to_non_nullable
          : positionSource as String?,
      extra: extra == const $CopyWithPlaceholder()
          ? _value.extra
          // ignore: cast_nullable_to_non_nullable
          : extra as Map<String, Object>?,
    );
  }
}

extension $QualityCopyWith on Quality {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuality.copyWith(...)` or `instanceOfQuality.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QualityCWProxy get copyWith => _$QualityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Quality _$QualityFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Quality', json, ($checkedConvert) {
      final val = Quality(
        accuracyMeters: $checkedConvert('accuracyMeters', (v) => v as num?),
        confidence: $checkedConvert('confidence', (v) => v as num?),
        positionSource: $checkedConvert('positionSource', (v) => v as String?),
        extra: $checkedConvert(
          'extra',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$QualityToJson(Quality instance) => <String, dynamic>{
  'accuracyMeters': ?instance.accuracyMeters,
  'confidence': ?instance.confidence,
  'positionSource': ?instance.positionSource,
  'extra': ?instance.extra,
};
