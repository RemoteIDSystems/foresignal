// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_history.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastHistoryCWProxy {
  ForecastHistory questionId(String questionId);

  ForecastHistory type(ForecastHistoryTypeEnum type);

  ForecastHistory step(ForecastHistoryStepEnum step);

  ForecastHistory from(DateTime? from);

  ForecastHistory to(DateTime? to);

  ForecastHistory series(List<ForecastHistorySeriesInner> series);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastHistory(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastHistory(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastHistory call({
    String questionId,
    ForecastHistoryTypeEnum type,
    ForecastHistoryStepEnum step,
    DateTime? from,
    DateTime? to,
    List<ForecastHistorySeriesInner> series,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastHistory.copyWith(...)` or call `instanceOfForecastHistory.copyWith.fieldName(value)` for a single field.
class _$ForecastHistoryCWProxyImpl implements _$ForecastHistoryCWProxy {
  const _$ForecastHistoryCWProxyImpl(this._value);

  final ForecastHistory _value;

  @override
  ForecastHistory questionId(String questionId) => call(questionId: questionId);

  @override
  ForecastHistory type(ForecastHistoryTypeEnum type) => call(type: type);

  @override
  ForecastHistory step(ForecastHistoryStepEnum step) => call(step: step);

  @override
  ForecastHistory from(DateTime? from) => call(from: from);

  @override
  ForecastHistory to(DateTime? to) => call(to: to);

  @override
  ForecastHistory series(List<ForecastHistorySeriesInner> series) =>
      call(series: series);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastHistory(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastHistory(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastHistory call({
    Object? questionId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
  }) {
    return ForecastHistory(
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ForecastHistoryTypeEnum,
      step: step == const $CopyWithPlaceholder() || step == null
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as ForecastHistoryStepEnum,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      series: series == const $CopyWithPlaceholder() || series == null
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as List<ForecastHistorySeriesInner>,
    );
  }
}

extension $ForecastHistoryCopyWith on ForecastHistory {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastHistory.copyWith(...)` or `instanceOfForecastHistory.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastHistoryCWProxy get copyWith => _$ForecastHistoryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastHistory _$ForecastHistoryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastHistory', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['questionId', 'type', 'step', 'series'],
      );
      final val = ForecastHistory(
        questionId: $checkedConvert('questionId', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ForecastHistoryTypeEnumEnumMap, v),
        ),
        step: $checkedConvert(
          'step',
          (v) => $enumDecode(_$ForecastHistoryStepEnumEnumMap, v),
        ),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        series: $checkedConvert(
          'series',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ForecastHistorySeriesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ForecastHistoryToJson(ForecastHistory instance) =>
    <String, dynamic>{
      'questionId': instance.questionId,
      'type': _$ForecastHistoryTypeEnumEnumMap[instance.type]!,
      'step': _$ForecastHistoryStepEnumEnumMap[instance.step]!,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'series': instance.series.map((e) => e.toJson()).toList(),
    };

const _$ForecastHistoryTypeEnumEnumMap = {
  ForecastHistoryTypeEnum.binary: 'binary',
  ForecastHistoryTypeEnum.multiple: 'multiple',
  ForecastHistoryTypeEnum.ordered: 'ordered',
  ForecastHistoryTypeEnum.numeric: 'numeric',
};

const _$ForecastHistoryStepEnumEnumMap = {ForecastHistoryStepEnum.P1D: 'P1D'};
