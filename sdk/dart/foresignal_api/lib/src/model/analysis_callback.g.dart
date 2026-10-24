// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_callback.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisCallbackCWProxy {
  AnalysisCallback correlationId(String correlationId);

  AnalysisCallback probability(num? probability);

  AnalysisCallback quantiles(Map<String, num>? quantiles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCallback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCallback(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisCallback call({
    String correlationId,
    num? probability,
    Map<String, num>? quantiles,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisCallback.copyWith(...)` or call `instanceOfAnalysisCallback.copyWith.fieldName(value)` for a single field.
class _$AnalysisCallbackCWProxyImpl implements _$AnalysisCallbackCWProxy {
  const _$AnalysisCallbackCWProxyImpl(this._value);

  final AnalysisCallback _value;

  @override
  AnalysisCallback correlationId(String correlationId) =>
      call(correlationId: correlationId);

  @override
  AnalysisCallback probability(num? probability) =>
      call(probability: probability);

  @override
  AnalysisCallback quantiles(Map<String, num>? quantiles) =>
      call(quantiles: quantiles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCallback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCallback(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisCallback call({
    Object? correlationId = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
  }) {
    return AnalysisCallback(
      correlationId:
          correlationId == const $CopyWithPlaceholder() || correlationId == null
          ? _value.correlationId
          // ignore: cast_nullable_to_non_nullable
          : correlationId as String,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      quantiles: quantiles == const $CopyWithPlaceholder()
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Map<String, num>?,
    );
  }
}

extension $AnalysisCallbackCopyWith on AnalysisCallback {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisCallback.copyWith(...)` or `instanceOfAnalysisCallback.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisCallbackCWProxy get copyWith => _$AnalysisCallbackCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisCallback _$AnalysisCallbackFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalysisCallback', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['correlationId']);
      final val = AnalysisCallback(
        correlationId: $checkedConvert('correlationId', (v) => v as String),
        probability: $checkedConvert('probability', (v) => v as num?),
        quantiles: $checkedConvert(
          'quantiles',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as num),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalysisCallbackToJson(AnalysisCallback instance) =>
    <String, dynamic>{
      'correlationId': instance.correlationId,
      'probability': ?instance.probability,
      'quantiles': ?instance.quantiles,
    };
