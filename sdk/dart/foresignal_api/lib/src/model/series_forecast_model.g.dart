// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_forecast_model.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesForecastModelCWProxy {
  SeriesForecastModel ref(String ref);

  SeriesForecastModel method(SeriesForecastModelMethodEnum method);

  SeriesForecastModel params(Map<String, Object>? params);

  SeriesForecastModel backtest(SeriesForecastModelBacktest backtest);

  SeriesForecastModel selectedAt(DateTime selectedAt);

  SeriesForecastModel nextSelectionAt(DateTime? nextSelectionAt);

  SeriesForecastModel version(String version);

  SeriesForecastModel manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastModel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastModel(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesForecastModel call({
    String ref,
    SeriesForecastModelMethodEnum method,
    Map<String, Object>? params,
    SeriesForecastModelBacktest backtest,
    DateTime selectedAt,
    DateTime? nextSelectionAt,
    String version,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesForecastModel.copyWith(...)` or call `instanceOfSeriesForecastModel.copyWith.fieldName(value)` for a single field.
class _$SeriesForecastModelCWProxyImpl implements _$SeriesForecastModelCWProxy {
  const _$SeriesForecastModelCWProxyImpl(this._value);

  final SeriesForecastModel _value;

  @override
  SeriesForecastModel ref(String ref) => call(ref: ref);

  @override
  SeriesForecastModel method(SeriesForecastModelMethodEnum method) =>
      call(method: method);

  @override
  SeriesForecastModel params(Map<String, Object>? params) =>
      call(params: params);

  @override
  SeriesForecastModel backtest(SeriesForecastModelBacktest backtest) =>
      call(backtest: backtest);

  @override
  SeriesForecastModel selectedAt(DateTime selectedAt) =>
      call(selectedAt: selectedAt);

  @override
  SeriesForecastModel nextSelectionAt(DateTime? nextSelectionAt) =>
      call(nextSelectionAt: nextSelectionAt);

  @override
  SeriesForecastModel version(String version) => call(version: version);

  @override
  SeriesForecastModel manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastModel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastModel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesForecastModel call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? backtest = const $CopyWithPlaceholder(),
    Object? selectedAt = const $CopyWithPlaceholder(),
    Object? nextSelectionAt = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return SeriesForecastModel(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as SeriesForecastModelMethodEnum,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      backtest: backtest == const $CopyWithPlaceholder() || backtest == null
          ? _value.backtest
          // ignore: cast_nullable_to_non_nullable
          : backtest as SeriesForecastModelBacktest,
      selectedAt:
          selectedAt == const $CopyWithPlaceholder() || selectedAt == null
          ? _value.selectedAt
          // ignore: cast_nullable_to_non_nullable
          : selectedAt as DateTime,
      nextSelectionAt: nextSelectionAt == const $CopyWithPlaceholder()
          ? _value.nextSelectionAt
          // ignore: cast_nullable_to_non_nullable
          : nextSelectionAt as DateTime?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $SeriesForecastModelCopyWith on SeriesForecastModel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesForecastModel.copyWith(...)` or `instanceOfSeriesForecastModel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesForecastModelCWProxy get copyWith =>
      _$SeriesForecastModelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesForecastModel _$SeriesForecastModelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SeriesForecastModel', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['ref', 'method', 'backtest', 'selectedAt', 'version'],
  );
  final val = SeriesForecastModel(
    ref: $checkedConvert('ref', (v) => v as String),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$SeriesForecastModelMethodEnumEnumMap, v),
    ),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    backtest: $checkedConvert(
      'backtest',
      (v) => SeriesForecastModelBacktest.fromJson(v as Map<String, dynamic>),
    ),
    selectedAt: $checkedConvert(
      'selectedAt',
      (v) => DateTime.parse(v as String),
    ),
    nextSelectionAt: $checkedConvert(
      'nextSelectionAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    version: $checkedConvert('version', (v) => v as String),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SeriesForecastModelToJson(
  SeriesForecastModel instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'method': _$SeriesForecastModelMethodEnumEnumMap[instance.method]!,
  'params': ?instance.params,
  'backtest': instance.backtest.toJson(),
  'selectedAt': instance.selectedAt.toIso8601String(),
  'nextSelectionAt': ?instance.nextSelectionAt?.toIso8601String(),
  'version': instance.version,
  'manifestId': ?instance.manifestId,
};

const _$SeriesForecastModelMethodEnumEnumMap = {
  SeriesForecastModelMethodEnum.seasonalNaive: 'seasonal-naive',
  SeriesForecastModelMethodEnum.etsAnn: 'ets-ann',
  SeriesForecastModelMethodEnum.etsAaa: 'ets-aaa',
  SeriesForecastModelMethodEnum.nbGlm: 'nb-glm',
  SeriesForecastModelMethodEnum.dampedTrend: 'damped-trend',
};
