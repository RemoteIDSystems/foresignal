// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_request_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisRequestResultCWProxy {
  AnalysisRequestResult correlationId(String correlationId);

  AnalysisRequestResult recorded(bool recorded);

  AnalysisRequestResult forecastId(String? forecastId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisRequestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisRequestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisRequestResult call({
    String correlationId,
    bool recorded,
    String? forecastId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisRequestResult.copyWith(...)` or call `instanceOfAnalysisRequestResult.copyWith.fieldName(value)` for a single field.
class _$AnalysisRequestResultCWProxyImpl
    implements _$AnalysisRequestResultCWProxy {
  const _$AnalysisRequestResultCWProxyImpl(this._value);

  final AnalysisRequestResult _value;

  @override
  AnalysisRequestResult correlationId(String correlationId) =>
      call(correlationId: correlationId);

  @override
  AnalysisRequestResult recorded(bool recorded) => call(recorded: recorded);

  @override
  AnalysisRequestResult forecastId(String? forecastId) =>
      call(forecastId: forecastId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisRequestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisRequestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisRequestResult call({
    Object? correlationId = const $CopyWithPlaceholder(),
    Object? recorded = const $CopyWithPlaceholder(),
    Object? forecastId = const $CopyWithPlaceholder(),
  }) {
    return AnalysisRequestResult(
      correlationId:
          correlationId == const $CopyWithPlaceholder() || correlationId == null
          ? _value.correlationId
          // ignore: cast_nullable_to_non_nullable
          : correlationId as String,
      recorded: recorded == const $CopyWithPlaceholder() || recorded == null
          ? _value.recorded
          // ignore: cast_nullable_to_non_nullable
          : recorded as bool,
      forecastId: forecastId == const $CopyWithPlaceholder()
          ? _value.forecastId
          // ignore: cast_nullable_to_non_nullable
          : forecastId as String?,
    );
  }
}

extension $AnalysisRequestResultCopyWith on AnalysisRequestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisRequestResult.copyWith(...)` or `instanceOfAnalysisRequestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisRequestResultCWProxy get copyWith =>
      _$AnalysisRequestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisRequestResult _$AnalysisRequestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisRequestResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['correlationId', 'recorded']);
  final val = AnalysisRequestResult(
    correlationId: $checkedConvert('correlationId', (v) => v as String),
    recorded: $checkedConvert('recorded', (v) => v as bool),
    forecastId: $checkedConvert('forecastId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnalysisRequestResultToJson(
  AnalysisRequestResult instance,
) => <String, dynamic>{
  'correlationId': instance.correlationId,
  'recorded': instance.recorded,
  'forecastId': ?instance.forecastId,
};
