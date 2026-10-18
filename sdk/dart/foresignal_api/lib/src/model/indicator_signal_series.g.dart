// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_signal_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorSignalSeriesCWProxy {
  IndicatorSignalSeries ref(String ref);

  IndicatorSignalSeries condition(IndicatorSignalSeriesCondition? condition);

  IndicatorSignalSeries changePoint(bool? changePoint);

  IndicatorSignalSeries direction(
    IndicatorSignalSeriesDirectionEnum? direction,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorSignalSeries call({
    String ref,
    IndicatorSignalSeriesCondition? condition,
    bool? changePoint,
    IndicatorSignalSeriesDirectionEnum? direction,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorSignalSeries.copyWith(...)` or call `instanceOfIndicatorSignalSeries.copyWith.fieldName(value)` for a single field.
class _$IndicatorSignalSeriesCWProxyImpl
    implements _$IndicatorSignalSeriesCWProxy {
  const _$IndicatorSignalSeriesCWProxyImpl(this._value);

  final IndicatorSignalSeries _value;

  @override
  IndicatorSignalSeries ref(String ref) => call(ref: ref);

  @override
  IndicatorSignalSeries condition(IndicatorSignalSeriesCondition? condition) =>
      call(condition: condition);

  @override
  IndicatorSignalSeries changePoint(bool? changePoint) =>
      call(changePoint: changePoint);

  @override
  IndicatorSignalSeries direction(
    IndicatorSignalSeriesDirectionEnum? direction,
  ) => call(direction: direction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorSignalSeries call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? condition = const $CopyWithPlaceholder(),
    Object? changePoint = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
  }) {
    return IndicatorSignalSeries(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      condition: condition == const $CopyWithPlaceholder()
          ? _value.condition
          // ignore: cast_nullable_to_non_nullable
          : condition as IndicatorSignalSeriesCondition?,
      changePoint: changePoint == const $CopyWithPlaceholder()
          ? _value.changePoint
          // ignore: cast_nullable_to_non_nullable
          : changePoint as bool?,
      direction: direction == const $CopyWithPlaceholder()
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as IndicatorSignalSeriesDirectionEnum?,
    );
  }
}

extension $IndicatorSignalSeriesCopyWith on IndicatorSignalSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorSignalSeries.copyWith(...)` or `instanceOfIndicatorSignalSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorSignalSeriesCWProxy get copyWith =>
      _$IndicatorSignalSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorSignalSeries _$IndicatorSignalSeriesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndicatorSignalSeries', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref']);
  final val = IndicatorSignalSeries(
    ref: $checkedConvert('ref', (v) => v as String),
    condition: $checkedConvert(
      'condition',
      (v) => v == null
          ? null
          : IndicatorSignalSeriesCondition.fromJson(v as Map<String, dynamic>),
    ),
    changePoint: $checkedConvert('changePoint', (v) => v as bool? ?? false),
    direction: $checkedConvert(
      'direction',
      (v) =>
          $enumDecodeNullable(_$IndicatorSignalSeriesDirectionEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$IndicatorSignalSeriesToJson(
  IndicatorSignalSeries instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'condition': ?instance.condition?.toJson(),
  'changePoint': ?instance.changePoint,
  'direction': ?_$IndicatorSignalSeriesDirectionEnumEnumMap[instance.direction],
};

const _$IndicatorSignalSeriesDirectionEnumEnumMap = {
  IndicatorSignalSeriesDirectionEnum.up: 'up',
  IndicatorSignalSeriesDirectionEnum.down: 'down',
  IndicatorSignalSeriesDirectionEnum.any: 'any',
};
