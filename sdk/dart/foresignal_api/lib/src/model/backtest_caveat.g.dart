// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backtest_caveat.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BacktestCaveatCWProxy {
  BacktestCaveat code(BacktestCaveatCodeEnum code);

  BacktestCaveat text(String text);

  BacktestCaveat data(Map<String, Object>? data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestCaveat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestCaveat(...).copyWith(id: 12, name: "My name")
  /// ```
  BacktestCaveat call({
    BacktestCaveatCodeEnum code,
    String text,
    Map<String, Object>? data,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBacktestCaveat.copyWith(...)` or call `instanceOfBacktestCaveat.copyWith.fieldName(value)` for a single field.
class _$BacktestCaveatCWProxyImpl implements _$BacktestCaveatCWProxy {
  const _$BacktestCaveatCWProxyImpl(this._value);

  final BacktestCaveat _value;

  @override
  BacktestCaveat code(BacktestCaveatCodeEnum code) => call(code: code);

  @override
  BacktestCaveat text(String text) => call(text: text);

  @override
  BacktestCaveat data(Map<String, Object>? data) => call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestCaveat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestCaveat(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BacktestCaveat call({
    Object? code = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return BacktestCaveat(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as BacktestCaveatCodeEnum,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>?,
    );
  }
}

extension $BacktestCaveatCopyWith on BacktestCaveat {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBacktestCaveat.copyWith(...)` or `instanceOfBacktestCaveat.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BacktestCaveatCWProxy get copyWith => _$BacktestCaveatCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BacktestCaveat _$BacktestCaveatFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BacktestCaveat', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'text']);
      final val = BacktestCaveat(
        code: $checkedConvert(
          'code',
          (v) => $enumDecode(_$BacktestCaveatCodeEnumEnumMap, v),
        ),
        text: $checkedConvert('text', (v) => v as String),
        data: $checkedConvert(
          'data',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BacktestCaveatToJson(BacktestCaveat instance) =>
    <String, dynamic>{
      'code': _$BacktestCaveatCodeEnumEnumMap[instance.code]!,
      'text': instance.text,
      'data': ?instance.data,
    };

const _$BacktestCaveatCodeEnumEnumMap = {
  BacktestCaveatCodeEnum.RANGE_CLAMPED_BY_RETENTION:
      'RANGE_CLAMPED_BY_RETENTION',
  BacktestCaveatCodeEnum.DOWNSAMPLED_RANGE: 'DOWNSAMPLED_RANGE',
  BacktestCaveatCodeEnum.SAMPLED_SOURCE: 'SAMPLED_SOURCE',
  BacktestCaveatCodeEnum.COVERAGE_GAPS: 'COVERAGE_GAPS',
  BacktestCaveatCodeEnum.DEFINITION_DIFFERS: 'DEFINITION_DIFFERS',
  BacktestCaveatCodeEnum.ENTITY_RESOLUTION_CHANGED: 'ENTITY_RESOLUTION_CHANGED',
  BacktestCaveatCodeEnum.WINDOW_APPROXIMATE: 'WINDOW_APPROXIMATE',
  BacktestCaveatCodeEnum.SMALL_SAMPLE: 'SMALL_SAMPLE',
  BacktestCaveatCodeEnum.NOT_GROUND_TRUTH: 'NOT_GROUND_TRUTH',
};
